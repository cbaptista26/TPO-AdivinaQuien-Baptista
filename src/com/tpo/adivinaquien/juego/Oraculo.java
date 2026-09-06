package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

/**
 * Lo unico que un jugador puede hacer contra el personaje secreto del rival.
 *
 * ESTO RESUELVE UN REQUISITO EXPLICITO DEL ENUNCIADO:
 * "la maquina no sabe, no puede acceder directamente a la variable del
 *  personaje elegido por el jugador humano".
 *
 * La maquina nunca recibe una referencia al Personaje secreto. Recibe un
 * Oraculo, que solo expone dos operaciones: preguntar por un filtro y arriesgar
 * una suposicion. No hay ningun getter que devuelva el personaje, asi que ni
 * siquiera por error se puede espiar: no compilaria.
 */
public interface Oraculo {

    /** Responde si el personaje secreto cumple o no el filtro. */
    boolean responder(Filtro filtro);

    /** Responde si el candidato propuesto es el personaje secreto. */
    boolean esElPersonaje(Personaje candidato);

    /** Cuantas preguntas se le hicieron. Sirve para las estadisticas. */
    int getCantidadPreguntas();
}
