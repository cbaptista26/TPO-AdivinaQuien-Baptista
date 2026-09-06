package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.Random;

/**
 * Elige el personaje secreto de una maquina.
 *
 * ACLARACION IMPORTANTE PARA LA DEFENSA
 * Aca SI se usa azar, y es correcto que asi sea. La regla de la catedra es que
 * las DECISIONES DE JUEGO (que preguntar, cuando arriesgar) tengan un criterio
 * explicable y no sean al azar. Elegir el propio personaje secreto no es una
 * decision de juego: es la condicion inicial del problema.
 *
 * De hecho, elegirlo con un criterio fijo seria un error: si la maquina eligiera
 * siempre el primero de la lista, el rival lo sabria y ganaria en un turno. El
 * azar aca es lo que garantiza que el problema a resolver sea el problema
 * completo (uno entre 23) y no uno degenerado.
 */
public class SelectorDePersonaje {

    private final Random random;

    public SelectorDePersonaje() {
        this.random = new Random();
    }

    /** Constructor con semilla fija: hace las partidas reproducibles para probar. */
    public SelectorDePersonaje(long semilla) {
        this.random = new Random(semilla);
    }

    public Personaje elegir() {
        List<Personaje> todos = CatalogoPersonajes.getInstancia().getOrdenDeCarga();
        return todos.get(random.nextInt(todos.size()));
    }
}
