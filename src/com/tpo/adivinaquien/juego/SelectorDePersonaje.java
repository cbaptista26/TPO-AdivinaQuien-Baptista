package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.Random;

/**
 * Elige el personaje secreto de una maquina.
 *
 * Aca SI se usa azar y es correcto: la regla es que las DECISIONES DE JUEGO
 * (que preguntar, cuando arriesgar) tengan un criterio explicable. Elegir el
 * propio personaje no es una decision de juego, es la condicion inicial. De
 * hecho, elegirlo con un criterio fijo seria un error: el rival lo sabria y
 * ganaria en un turno.
 */
public class SelectorDePersonaje {

    private final Random random = new Random();

    public Personaje elegir() {
        List<Personaje> todos = CatalogoPersonajes.getInstancia().getOrdenDeCarga();
        return todos.get(random.nextInt(todos.size()));
    }
}
