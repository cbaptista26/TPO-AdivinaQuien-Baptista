package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.modelo.Filtro;

/** El jugador decide preguntar por una caracteristica. */
public record PreguntaFiltro(Filtro filtro) implements Jugada {

    @Override
    public String toString() {
        return "Pregunta: " + filtro.getDescripcion();
    }
}
