package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.jugador.Jugada;
import com.tpo.adivinaquien.jugador.Jugador;
import com.tpo.adivinaquien.jugador.PreguntaFiltro;
import com.tpo.adivinaquien.jugador.Suposicion;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

/**
 * ===========================================================================
 * EL MOTOR DE LA PARTIDA
 * ===========================================================================
 *
 * Coordina los turnos entre dos jugadores. No sabe nada de consola ni de Swing:
 * todo lo que informa lo manda por el RegistroRazonamiento y todo lo que
 * devuelve es un ResultadoTurno. Por eso la misma Partida sirve para los tres
 * modos de juego y para las dos vistas.
 *
 * NO ES UN BUCLE, ES UNA MAQUINA DE ESTADOS
 * La Partida no tiene un while que corra hasta el final. Expone:
 *
 *   getEnTurno()          -> a quien le toca
 *   aplicarJugada(jugada) -> aplica una jugada y devuelve que paso
 *   haTerminado()         -> si ya hay ganador
 *
 * La vista es la que decide cuando pedir la proxima jugada. En consola se llama
 * en un bucle; en Swing se llama desde el listener de un boton. Si estuviera
 * como un while adentro del motor, Swing se congelaria esperando input y no
 * podria redibujar el tablero.
 *
 * QUIEN GUARDA QUE
 * Cada jugador tiene su propio Oraculo, que guarda el personaje que ELIGIO y
 * que el rival debe adivinar. Cuando el jugador A pregunta, la Partida consulta
 * el oraculo de B. En ningun momento un jugador recibe una referencia al
 * personaje del otro: solo recibe el booleano de respuesta.
 *
 * REGLA DE LA SUPOSICION
 * Una suposicion incorrecta hace perder la partida. Es la regla clasica del
 * juego y es la que le da sentido a la decision de arriesgar: si equivocarse no
 * costara nada, la estrategia optima seria adivinar en el turno 1. Con esta
 * regla, arriesgar antes de llegar al caso base es un riesgo real, y por eso la
 * maquina greedy solo arriesga cuando le queda un unico candidato: en ese punto
 * la probabilidad de acertar es 1.
 */
public class Partida {

    private final Jugador jugadorA;
    private final Jugador jugadorB;

    /** Guarda el personaje que eligio A. Lo consulta B cuando pregunta. */
    private final OraculoPersonaje oraculoA;

    /** Guarda el personaje que eligio B. Lo consulta A cuando pregunta. */
    private final OraculoPersonaje oraculoB;

    private final RegistroRazonamiento registro;

    private Jugador enTurno;
    private Jugador ganador;
    private int numeroTurno = 0;

    public Partida(Jugador jugadorA, Personaje secretoA,
                   Jugador jugadorB, Personaje secretoB,
                   RegistroRazonamiento registro) {

        this.jugadorA = jugadorA;
        this.jugadorB = jugadorB;
        this.oraculoA = new OraculoPersonaje(secretoA);
        this.oraculoB = new OraculoPersonaje(secretoB);
        this.registro = registro;
        this.enTurno = jugadorA;
    }

    // ------------------------------------------------------------------
    // ESTADO
    // ------------------------------------------------------------------

    public Jugador getEnTurno()   { return enTurno; }
    public Jugador getJugadorA()  { return jugadorA; }
    public Jugador getJugadorB()  { return jugadorB; }
    public Jugador getGanador()   { return ganador; }
    public int getNumeroTurno()   { return numeroTurno; }
    public boolean haTerminado()  { return ganador != null; }

    /** El rival del jugador que esta en turno. */
    public Jugador getRival() {
        return enTurno == jugadorA ? jugadorB : jugadorA;
    }

    /** El oraculo que hay que consultar cuando pregunta el jugador en turno. */
    private Oraculo oraculoDelRival() {
        return enTurno == jugadorA ? oraculoB : oraculoA;
    }

    /**
     * Revela los personajes secretos. Solo se llama cuando la partida termino,
     * para mostrar el resultado.
     */
    public Personaje getSecretoDe(Jugador j) {
        if (!haTerminado()) {
            throw new IllegalStateException(
                    "No se puede revelar el personaje secreto con la partida en curso.");
        }
        return j == jugadorA ? oraculoA.revelarAlFinalizar() : oraculoB.revelarAlFinalizar();
    }

    // ------------------------------------------------------------------
    // TURNO
    // ------------------------------------------------------------------

    /**
     * Anuncia de quien es el turno. La vista lo llama ANTES de pedirle la
     * jugada al jugador, para que el encabezado del turno aparezca antes del
     * razonamiento de la maquina y no despues.
     */
    public void anunciarTurno() {
        registro.registrarTitulo(String.format("TURNO %d - juega %s (%d candidatos)",
                numeroTurno + 1, enTurno.getNombre(), enTurno.getCantidadCandidatos()));
    }

    /**
     * Aplica la jugada del jugador que esta en turno y devuelve que paso.
     *
     * Es el unico metodo que modifica el estado de la partida.
     */
    public ResultadoTurno aplicarJugada(Jugada jugada) {

        if (haTerminado()) {
            throw new IllegalStateException("La partida ya termino.");
        }

        Jugador actor = enTurno;

        ResultadoTurno resultado = switch (jugada) {
            case PreguntaFiltro p -> aplicarPregunta(actor, p.filtro());
            case Suposicion s     -> aplicarSuposicion(actor, s.candidato());
        };

        if (!haTerminado()) {
            cambiarTurno();
        }
        return resultado;
    }

    /**
     * El jugador pregunta por un filtro: se consulta el oraculo del rival y con
     * la respuesta se achica el conjunto de candidatos (paso "combinar" del D&C).
     */
    private ResultadoTurno aplicarPregunta(Jugador actor, Filtro filtro) {

        numeroTurno++;
        boolean respuesta = oraculoDelRival().responder(filtro);
        int antes = actor.getCantidadCandidatos();

        actor.recibirRespuesta(filtro, respuesta);

        int despues = actor.getCantidadCandidatos();

        return new ResultadoTurno(
                numeroTurno, actor, filtro, respuesta, null, false, ganador,
                String.format("%s pregunta \"%s\" -> %s. Candidatos: %d -> %d",
                        actor.getNombre(), filtro.getDescripcion(),
                        respuesta ? "SI" : "NO", antes, despues));
    }

    /**
     * El jugador arriesga quien es el personaje del rival.
     *
     * Si acierta, gana. Si falla, pierde la partida (ver la regla explicada
     * arriba de la clase).
     */
    private ResultadoTurno aplicarSuposicion(Jugador actor, Personaje candidato) {

        numeroTurno++;
        boolean acerto = oraculoDelRival().esElPersonaje(candidato);

        if (acerto) {
            ganador = actor;
        } else {
            actor.descartarCandidato(candidato);
            ganador = (actor == jugadorA) ? jugadorB : jugadorA;
        }

        String mensaje = String.format("%s arriesga: es %s? -> %s",
                actor.getNombre(), candidato.getNombre(),
                acerto ? "CORRECTO, gana la partida"
                       : "INCORRECTO, pierde la partida");

        registro.registrar("    " + mensaje);

        return new ResultadoTurno(
                numeroTurno, actor, null, null, candidato, true, ganador, mensaje);
    }

    private void cambiarTurno() {
        enTurno = (enTurno == jugadorA) ? jugadorB : jugadorA;
    }
}
