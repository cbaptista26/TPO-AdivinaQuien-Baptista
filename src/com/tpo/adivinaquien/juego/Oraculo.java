package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

/**
 * Lo unico que un jugador puede hacer contra el personaje secreto del rival.
 *
 * Resuelve un requisito explicito del enunciado: "la maquina no sabe, no puede
 * acceder directamente a la variable del personaje elegido por el jugador
 * humano". La maquina recibe esta interfaz, no el Personaje. No hay ningun
 * getter del secreto, asi que ni siquiera por error se puede espiar: no
 * compilaria.
 */
public interface Oraculo {

    /** Responde si el personaje secreto cumple o no el filtro. */
    boolean responder(Filtro filtro);

    /** Responde si el candidato propuesto es el personaje secreto. */
    boolean esElPersonaje(Personaje candidato);
}
