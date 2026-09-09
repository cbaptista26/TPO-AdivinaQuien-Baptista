package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.jugador.Jugada;
import com.tpo.adivinaquien.jugador.Jugador;
import com.tpo.adivinaquien.jugador.PreguntaFiltro;
import com.tpo.adivinaquien.jugador.Suposicion;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

/**
 * El motor: coordina los turnos entre dos jugadores.
 *
 * No sabe nada de consola ni de Swing. Todo lo que informa lo manda por el
 * RegistroRazonamiento, y lo que devuelve es un ResultadoTurno.
 *
 * ESTO NO ES UN BUCLE, ES UNA MAQUINA DE ESTADOS, y me costo llegar aca. Al
 * principio tenia un while adentro y en consola andaba, pero al armar la ventana
 * Swing se congelaba: el hilo que dibuja la interfaz quedaba atrapado en el
 * bucle esperando que la persona escribiera algo. Entonces lo di vuelta: la
 * Partida expone "a quien le toca", "aplica esta jugada" y "termino?", y es la
 * vista la que decide cuando pedir la proxima jugada. En consola desde un while,
 * en Swing desde el clic de un boton.
 *
 * Cada jugador tiene su propio Oraculo, que guarda el personaje que ELIGIO y que
 * el rival tiene que adivinar. Cuando juega A, se consulta el oraculo de B.
 *
 * Regla: si arriesgas y erras, perdes. Es la regla clasica y ademas le da sentido
 * a la decision de arriesgar; si equivocarse no costara nada, lo mejor seria
 * adivinar en el turno 1.
 */
public class Partida {

    private final Jugador jugadorA;
    private final Jugador jugadorB;
    private final OraculoPersonaje oraculoA;   // guarda el secreto de A
    private final OraculoPersonaje oraculoB;   // guarda el secreto de B
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

    public Jugador getEnTurno()  { return enTurno; }
    public Jugador getGanador()  { return ganador; }
    public int getNumeroTurno()  { return numeroTurno; }
    public boolean haTerminado() { return ganador != null; }

    private Oraculo oraculoDelRival() {
        return enTurno == jugadorA ? oraculoB : oraculoA;
    }

    /** Revela los secretos. Solo se puede llamar con la partida terminada. */
    public Personaje getSecretoDe(Jugador j) {
        if (!haTerminado()) {
            throw new IllegalStateException("No se revela el secreto con la partida en curso.");
        }
        return j == jugadorA ? oraculoA.revelarAlFinalizar() : oraculoB.revelarAlFinalizar();
    }

    /**
     * Anuncia de quien es el turno. La vista lo llama ANTES de pedir la jugada,
     * para que el encabezado aparezca antes del razonamiento de la maquina.
     */
    public void anunciarTurno() {
        registro.registrarTitulo(String.format("TURNO %d - juega %s (%d candidatos)",
                numeroTurno + 1, enTurno.getNombre(), enTurno.getCantidadCandidatos()));
    }

    /** Aplica la jugada del jugador en turno. Unico metodo que cambia el estado. */
    public ResultadoTurno aplicarJugada(Jugada jugada) {

        if (haTerminado()) throw new IllegalStateException("La partida ya termino.");

        Jugador actor = enTurno;

        ResultadoTurno resultado = switch (jugada) {
            case PreguntaFiltro p -> aplicarPregunta(actor, p.filtro());
            case Suposicion s     -> aplicarSuposicion(actor, s.candidato());
        };

        if (!haTerminado()) {
            enTurno = (enTurno == jugadorA) ? jugadorB : jugadorA;
        }
        return resultado;
    }

    /** Consulta el oraculo del rival y achica el conjunto (paso "combinar"). */
    private ResultadoTurno aplicarPregunta(Jugador actor, Filtro filtro) {

        numeroTurno++;
        boolean respuesta = oraculoDelRival().responder(filtro);
        int antes = actor.getCantidadCandidatos();

        actor.recibirRespuesta(filtro, respuesta);

        return new ResultadoTurno(numeroTurno, actor, filtro, respuesta, null, false, ganador,
                String.format("%s pregunta \"%s\" -> %s. Candidatos: %d -> %d",
                        actor.getNombre(), filtro.getDescripcion(),
                        respuesta ? "SI" : "NO", antes, actor.getCantidadCandidatos()));
    }

    /** Si acierta gana; si falla pierde la partida. */
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
                acerto ? "CORRECTO, gana la partida" : "INCORRECTO, pierde la partida");

        registro.registrar("    " + mensaje);

        return new ResultadoTurno(numeroTurno, actor, null, null, candidato, true, ganador, mensaje);
    }
}
