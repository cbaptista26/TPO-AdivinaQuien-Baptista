package com.tpo.adivinaquien.vista;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.juego.Partida;
import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.juego.ResultadoTurno;
import com.tpo.adivinaquien.juego.SelectorDePersonaje;
import com.tpo.adivinaquien.jugador.*;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.Optional;

/**
 * El puente entre la ventana y el motor.
 *
 * La ventana solo dibuja y escucha clics; toda la coordinacion de la partida
 * esta aca. Hace falta porque en Swing no hay bucle de turnos: cada clic dispara
 * un turno y despues la ventana vuelve a quedar esperando. Este controlador se
 * encarga de que apenas termina el turno de la persona, la maquina juegue el
 * suyo.
 *
 * No tiene ninguna decision de juego adentro: solo llama en orden a las clases
 * que ya existen.
 */
public class ControladorPartida {

    /** Los dos modos de juego que pide el enunciado. */
    public enum Modo { JUGADOR_VS_MAQUINA, MAQUINA_VS_MAQUINA }

    public interface Observador {
        void actualizar();
        void mostrarMensaje(String mensaje);
        void mostrarFinal(String texto);
    }

    private final RegistroRazonamiento registro;
    private final Observador observador;

    private Modo modo;
    private Jugador jugadorA;   // el humano, o la maquina greedy en modo MvM
    private Jugador jugadorB;   // siempre una maquina
    private Partida partida;

    public ControladorPartida(RegistroRazonamiento registro, Observador observador) {
        this.registro = registro;
        this.observador = observador;
    }

    // ---------------- ARRANQUE ----------------

    /** Modo jugador contra maquina. */
    public void nuevaPartida(Personaje secretoDelHumano) {
        modo = Modo.JUGADOR_VS_MAQUINA;
        List<Personaje> universo = CatalogoPersonajes.getInstancia().getOrdenados();
        jugadorA = new JugadorHumano("VOS", universo, registro);
        jugadorB = new MaquinaGreedy("MAQUINA", universo, registro);

        registro.registrar("Nueva partida. Elegiste a " + secretoDelHumano.getNombre() + ".");
        registro.registrar("Criterio de la maquina: " + jugadorB.getCriterio());

        iniciar(secretoDelHumano, selector().elegir());
    }

    /**
     * Modo maquina contra maquina: greedy contra secuencial, la persona observa.
     * Es lo que pide el enunciado cuando dice que se tienen que poder presenciar
     * todos los procesos que hace la maquina para acortar la busqueda.
     */
    public void nuevaPartidaEntreMaquinas() {
        modo = Modo.MAQUINA_VS_MAQUINA;
        List<Personaje> universo = CatalogoPersonajes.getInstancia().getOrdenados();
        jugadorA = new MaquinaGreedy("GREEDY", universo, registro);
        jugadorB = new MaquinaSecuencial("SECUENCIAL", universo, registro, false);

        registro.registrar("GREEDY: " + jugadorA.getCriterio());
        registro.registrar("SECUENCIAL: " + jugadorB.getCriterio());

        SelectorDePersonaje selector = selector();
        iniciar(selector.elegir(), selector.elegir());
    }

    /** El selector sobre el catalogo actual. */
    private SelectorDePersonaje selector() {
        return new SelectorDePersonaje(CatalogoPersonajes.getInstancia().getOrdenDeCarga());
    }

    private void iniciar(Personaje secretoA, Personaje secretoB) {
        partida = new Partida(jugadorA, secretoA, jugadorB, secretoB, registro);
        partida.anunciarTurno();
        observador.actualizar();
    }

    // ---------------- JUGADAS ----------------

    public void preguntar(Filtro filtro) { jugar(new PreguntaFiltro(filtro)); }

    public void arriesgar(Personaje candidato) { jugar(new Suposicion(candidato)); }

    /** Aplica la jugada del humano y, si sigue la partida, juega la maquina. */
    private void jugar(Jugada jugada) {
        if (partida == null || partida.haTerminado()) return;

        observador.mostrarMensaje(partida.aplicarJugada(jugada).mensaje());

        if (partida.haTerminado()) { terminar(); return; }

        // Turno automatico de la maquina rival. No hace falta castear: todos los
        // jugadores responden decidirJugada() y la maquina siempre trae una.
        partida.anunciarTurno();
        jugadorB.decidirJugada().ifPresent(
                j -> observador.mostrarMensaje(partida.aplicarJugada(j).mensaje()));

        if (partida.haTerminado()) {
            terminar();
        } else {
            partida.anunciarTurno();
            observador.actualizar();
        }
    }

    /**
     * Juega un turno del modo maquina contra maquina.
     *
     * @return true si la partida sigue, false si termino
     */
    public boolean avanzarTurnoDeMaquinas() {
        if (partida == null || partida.haTerminado()) return false;

        Optional<Jugada> jugada = partida.getEnTurno().decidirJugada();
        if (jugada.isEmpty()) return false;      // no deberia pasar: ambas son maquinas

        observador.mostrarMensaje(partida.aplicarJugada(jugada.get()).mensaje());

        if (partida.haTerminado()) { terminar(); return false; }

        partida.anunciarTurno();
        observador.actualizar();
        return true;
    }

    private void terminar() {
        observador.actualizar();

        String texto = "GANA: " + partida.getGanador().getNombre()
                + "\nTurnos jugados: " + partida.getNumeroTurno()
                + "\n\n" + jugadorA.getNombre() + " habia elegido a "
                + partida.getSecretoDe(jugadorA).getNombre()
                + "\n" + jugadorB.getNombre() + " habia elegido a "
                + partida.getSecretoDe(jugadorB).getNombre();

        if (esModoMaquinas()) {
            texto += "\n\nPreguntas usadas:"
                   + "\n  " + jugadorA.getNombre() + ": " + jugadorA.getPreguntasHechas()
                   + "  (le quedaban " + jugadorA.getCantidadCandidatos() + " candidatos)"
                   + "\n  " + jugadorB.getNombre() + ": " + jugadorB.getPreguntasHechas()
                   + "  (le quedaban " + jugadorB.getCantidadCandidatos() + " candidatos)";
        }

        registro.registrar("\n=== FIN DE LA PARTIDA ===");
        registro.registrar(texto.replace("\n", " | "));
        observador.mostrarFinal(texto);
    }

    // ---------------- CONSULTAS PARA DIBUJAR ----------------

    public boolean hayPartida() { return partida != null; }

    public boolean termino() { return partida != null && partida.haTerminado(); }

    public boolean esModoMaquinas() { return modo == Modo.MAQUINA_VS_MAQUINA; }

    /** True si le toca jugar a la persona. */
    public boolean esTurnoDelHumano() {
        return modo == Modo.JUGADOR_VS_MAQUINA && partida != null
                && !partida.haTerminado() && partida.getEnTurno() == jugadorA;
    }

    /** Los candidatos del tablero que se dibuja (los del humano, o los de greedy). */
    public List<Personaje> candidatosDelTablero() {
        return jugadorA == null ? List.of() : jugadorA.getCandidatos();
    }

    /** Cuantos candidatos le quedan al rival. */
    public int candidatosDelRival() {
        return jugadorB == null ? 0 : jugadorB.getCantidadCandidatos();
    }

    public String nombreJugadorA() { return jugadorA == null ? "" : jugadorA.getNombre(); }
    public String nombreJugadorB() { return jugadorB == null ? "" : jugadorB.getNombre(); }

    public String nombreEnTurno() {
        return (partida == null || partida.haTerminado()) ? "" : partida.getEnTurno().getNombre();
    }

    /**
     * Los filtros que el jugador del tablero todavia no pregunto.
     * Ya no hace falta preguntar de que tipo es: lo responde cualquier Jugador.
     */
    public List<Filtro> filtrosDisponibles() {
        return jugadorA == null ? List.of() : jugadorA.filtrosDisponibles();
    }

    /** Evaluacion greedy sobre el tablero mostrado, de mejor a peor. */
    public List<EvaluacionFiltro> sugerencias() {
        return jugadorA == null ? List.of() : jugadorA.sugerencias();
    }

    public int numeroTurno() { return partida == null ? 0 : partida.getNumeroTurno(); }

    public String nombreDelGanador() {
        return (partida == null || partida.getGanador() == null)
                ? "" : partida.getGanador().getNombre();
    }
}
