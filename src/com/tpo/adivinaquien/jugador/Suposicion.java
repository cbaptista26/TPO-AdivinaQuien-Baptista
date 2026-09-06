package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.modelo.Personaje;

/** El jugador decide arriesgar directamente quien es el personaje secreto. */
public record Suposicion(Personaje candidato) implements Jugada {

    @Override
    public String toString() {
        return "Suposicion: es " + candidato.getNombre() + "?";
    }
}
