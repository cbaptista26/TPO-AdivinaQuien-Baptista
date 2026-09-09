package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

/**
 * Lo unico que un jugador puede hacer contra el personaje secreto del rival.
 *
 * Esto resuelve un pedido explicito del enunciado: "la maquina no sabe, no puede
 * acceder directamente a la variable del personaje elegido por el jugador
 * humano".
 *
 * La forma que encontre fue no darle nunca el Personaje a la maquina, sino esta
 * interfaz con dos operaciones: preguntar por un filtro y arriesgar un nombre.
 * El personaje secreto queda en un campo privado sin getter, asi que no es que
 * la maquina "no lo hace": no compilaria si lo intentara.
 */
public interface Oraculo {

    /** Responde si el personaje secreto cumple o no el filtro. */
    boolean responder(Filtro filtro);

    /** Responde si el candidato propuesto es el personaje secreto. */
    boolean esElPersonaje(Personaje candidato);
}
