package com.tpo.adivinaquien.modelo;

import java.util.Collections;
import java.util.List;

/**
 * El resultado de partir un conjunto de candidatos con un filtro: lo que
 * devuelve descomponer(x) en el esquema D&C. Los dos subconjuntos son disjuntos
 * y su union es el conjunto original.
 */
public class Particion {

    private final List<Personaje> cumplen;
    private final List<Personaje> noCumplen;

    public Particion(List<Personaje> cumplen, List<Personaje> noCumplen) {
        this.cumplen = cumplen;
        this.noCumplen = noCumplen;
    }

    public List<Personaje> getCumplen()   { return Collections.unmodifiableList(cumplen); }
    public List<Personaje> getNoCumplen() { return Collections.unmodifiableList(noCumplen); }

    public int cantidadCumplen()   { return cumplen.size(); }
    public int cantidadNoCumplen() { return noCumplen.size(); }
    public int total()             { return cumplen.size() + noCumplen.size(); }

    /**
     * El grupo mas grande: cuantos candidatos quedarian si la respuesta fuera la
     * peor posible. Es la funcion que usa el greedy para comparar filtros.
     */
    public int peorCaso() {
        return Math.max(cumplen.size(), noCumplen.size());
    }

    /**
     * combinar(): se queda con el subconjunto de la respuesta real y descarta el
     * otro entero. No hace falta recorrer las dos ramas porque la respuesta dice
     * cual contiene al secreto (misma simplificacion que la busqueda binaria).
     */
    public List<Personaje> combinar(boolean respuesta) {
        return respuesta ? cumplen : noCumplen;
    }

    /** True si el filtro no separo nada: todos los candidatos de un lado. */
    public boolean esInutil() {
        return cumplen.isEmpty() || noCumplen.isEmpty();
    }
}
