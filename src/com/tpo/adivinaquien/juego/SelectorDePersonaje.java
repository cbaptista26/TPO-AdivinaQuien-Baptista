package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.Random;

/**
 * Elige al azar el personaje secreto de una maquina.
 *
 * Este es el unico Random de todo el proyecto y quiero dejar claro por que esta
 * bien que este. La regla que nos dieron es que las DECISIONES DE JUEGO (que
 * preguntar, cuando arriesgar) tengan un criterio explicable. Elegir tu propio
 * personaje no es una decision de juego: es la condicion inicial del problema.
 *
 * De hecho seria un error hacerlo con un criterio fijo. Si la maquina eligiera
 * siempre el primero de la lista, el rival lo sabria y ganaria en un turno.
 *
 * Recibe la lista por constructor en vez de ir a buscar el catalogo sola, para
 * no depender de una clase concreta.
 */
public class SelectorDePersonaje {

    private final List<Personaje> universo;
    private final Random random;

    public SelectorDePersonaje(List<Personaje> universo) {
        this(universo, new Random());
    }

    /** Con semilla fija: hace las partidas reproducibles al probar. */
    public SelectorDePersonaje(List<Personaje> universo, Random random) {
        if (universo == null || universo.isEmpty()) {
            throw new IllegalArgumentException("No hay personajes para elegir.");
        }
        this.universo = universo;
        this.random = random;
    }

    public Personaje elegir() {
        return universo.get(random.nextInt(universo.size()));
    }
}
