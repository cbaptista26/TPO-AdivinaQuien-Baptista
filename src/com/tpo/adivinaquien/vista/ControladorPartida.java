package com.tpo.adivinaquien.vista;

import com.tpo.adivinaquien.juego.Partida;
import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.juego.ResultadoTurno;
import com.tpo.adivinaquien.juego.SelectorDePersonaje;
import com.tpo.adivinaquien.jugador.Jugada;
import com.tpo.adivinaquien.jugador.JugadorHumano;
import com.tpo.adivinaquien.jugador.MaquinaGreedy;
import com.tpo.adivinaquien.jugador.PreguntaFiltro;
import com.tpo.adivinaquien.jugador.Suposicion;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;

/**
 * ===========================================================================
 * EL PUENTE ENTRE LA VENTANA Y EL MOTOR
 * ===========================================================================
 *
 * La ventana de Swing (VentanaJuego) solo dibuja y escucha clics. Toda la
 * coordinacion de la partida vive aca. Asi la clase generada por el GUI
 * Designer queda limpia y la logica queda en una clase normal, testeable y
 * facil de mostrar en la defensa.
 *
 * POR QUE HACE FALTA ESTA CLASE Y NO ALCANZA CON PARTIDA
 * En consola el bucle de turnos lo maneja el while del menu. En Swing no hay
 * bucle: cada clic dispara un turno y despues la ventana vuelve a quedar
 * esperando. Este controlador es el que se encarga de que, apenas termina el
 * turno del humano, la maquina juegue el suyo automaticamente, y de avisarle a
 * la ventana que se tiene que redibujar.
 *
 * NO CONTIENE NADA DE ALGORITMICA
 * No hay ni una decision de juego aca: preguntar que preguntar es de
 * MaquinaGreedy, achicar candidatos es de Jugador, resolver el turno es de
 * Partida. Esta clase solo los llama en orden.
 */
public class ControladorPartida {

    /** Lo que la ventana tiene que hacer cuando cambia el estado del juego. */
    public interface Observador {
        /** Redibujar tablero, contadores y botones. */
        void actualizar();

        /** Mostrar el resultado de un turno. */
        void mostrarMensaje(String mensaje);

        /** La partida termino: nombre del ganador y personajes revelados. */
        void mostrarFinal(String texto);
    }

    private final RegistroRazonamiento registro;
    private final Observador observador;

    private JugadorHumano humano;
    private MaquinaGreedy maquina;
    private Partida partida;

    public ControladorPartida(RegistroRazonamiento registro, Observador observador) {
        this.registro = registro;
        this.observador = observador;
    }

    // ------------------------------------------------------------------
    // ARRANQUE
    // ------------------------------------------------------------------

    /**
     * Empieza una partida nueva.
     *
     * @param secretoDelHumano el personaje que eligio la persona y que la
     *                         maquina tiene que adivinar
     */
    public void nuevaPartida(Personaje secretoDelHumano) {
        humano = new JugadorHumano("VOS", registro);
        maquina = new MaquinaGreedy("MAQUINA", registro);

        Personaje secretoDeLaMaquina = new SelectorDePersonaje().elegir();

        partida = new Partida(humano, secretoDelHumano,
                maquina, secretoDeLaMaquina, registro);

        registro.registrar("Nueva partida. Elegiste a " + secretoDelHumano.getNombre() + ".");
        registro.registrar("Criterio de la maquina: " + maquina.getCriterio());

        partida.anunciarTurno();
        observador.actualizar();
    }

    // ------------------------------------------------------------------
    // JUGADAS DEL HUMANO
    // ------------------------------------------------------------------

    /** La persona hace una pregunta. */
    public void preguntar(Filtro filtro) {
        jugar(new PreguntaFiltro(filtro));
    }

    /** La persona arriesga quien es el personaje de la maquina. */
    public void arriesgar(Personaje candidato) {
        jugar(new Suposicion(candidato));
    }

    /**
     * Aplica la jugada del humano y, si la partida sigue, deja que la maquina
     * juegue su turno.
     */
    private void jugar(Jugada jugada) {
        if (partida == null || partida.haTerminado()) {
            return;
        }

        ResultadoTurno r = partida.aplicarJugada(jugada);
        observador.mostrarMensaje(r.mensaje());

        if (partida.haTerminado()) {
            terminar();
            return;
        }

        turnoDeLaMaquina();
    }

    /** La maquina decide sola y juega. */
    private void turnoDeLaMaquina() {
        partida.anunciarTurno();

        ResultadoTurno r = partida.aplicarJugada(maquina.jugarTurno());
        observador.mostrarMensaje(r.mensaje());

        if (partida.haTerminado()) {
            terminar();
        } else {
            partida.anunciarTurno();
            observador.actualizar();
        }
    }

    /** Arma el texto del final y avisa a la ventana. */
    private void terminar() {
        observador.actualizar();

        String texto = "GANA: " + partida.getGanador().getNombre()
                + "\nTurnos jugados: " + partida.getNumeroTurno()
                + "\n\nVos habias elegido a " + partida.getSecretoDe(humano).getNombre()
                + "\nLa maquina habia elegido a " + partida.getSecretoDe(maquina).getNombre();

        registro.registrar("");
        registro.registrar("=== FIN DE LA PARTIDA ===");
        registro.registrar(texto.replace("\n", " | "));

        observador.mostrarFinal(texto);
    }

    // ------------------------------------------------------------------
    // CONSULTAS QUE HACE LA VENTANA PARA DIBUJARSE
    // ------------------------------------------------------------------

    public boolean hayPartida() {
        return partida != null;
    }

    public boolean termino() {
        return partida != null && partida.haTerminado();
    }

    /** True si le toca jugar a la persona. */
    public boolean esTurnoDelHumano() {
        return partida != null && !partida.haTerminado() && partida.getEnTurno() == humano;
    }

    /** Los personajes que la persona todavia no descarto. */
    public List<Personaje> candidatosDelHumano() {
        return humano == null ? List.of() : humano.getCandidatos();
    }

    /** Los filtros que la persona todavia no pregunto. */
    public List<Filtro> filtrosDisponibles() {
        return humano == null ? List.of() : humano.filtrosDisponibles();
    }

    /**
     * La evaluacion greedy aplicada al tablero de la persona, ordenada de mejor
     * a peor. No juega por ella: solo le muestra el mismo calculo que hace la
     * maquina para que pueda comparar su decision con la del algoritmo.
     */
    public List<EvaluacionFiltro> sugerencias() {
        return humano == null ? List.of() : humano.sugerencias();
    }

    public int candidatosDeLaMaquina() {
        return maquina == null ? 0 : maquina.getCantidadCandidatos();
    }

    public int numeroTurno() {
        return partida == null ? 0 : partida.getNumeroTurno();
    }

    public String nombreDelGanador() {
        return (partida == null || partida.getGanador() == null)
                ? "" : partida.getGanador().getNombre();
    }
}