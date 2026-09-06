package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.jugador.Jugador;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

/**
 * Que paso en un turno. Es lo que la Partida le devuelve a la vista para que
 * la muestre como quiera (texto en consola, actualizar el tablero en Swing).
 *
 * Los campos que no aplican quedan en null: si el turno fue una pregunta,
 * 'suposicion' es null; si fue una suposicion, 'filtro' y 'respuesta' son null.
 */
public record ResultadoTurno(
        int numeroTurno,
        Jugador actor,
        Filtro filtro,
        Boolean respuesta,
        Personaje suposicion,
        boolean fueSuposicion,
        Jugador ganador,
        String mensaje) {

    public boolean terminoLaPartida() {
        return ganador != null;
    }
}
