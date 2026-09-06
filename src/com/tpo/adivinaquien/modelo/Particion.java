package com.tpo.adivinaquien.modelo;

import java.util.Collections;
import java.util.List;

/**
 * El resultado de partir un conjunto de candidatos en dos con un filtro.
 *
 * Es lo que devuelve descomponer(x) en el esquema Divide and Conquer de la
 * catedra: el conjunto original queda separado en dos subconjuntos disjuntos
 * (ningun personaje puede estar en los dos) cuya union es el conjunto original.
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
     * El grupo mas grande de los dos: cuantos candidatos quedarian si la
     * respuesta fuera la peor posible.
     *
     * Esta es la funcion que usa el greedy para comparar filtros. Como la
     * maquina no sabe que va a responder el rival, no puede optimizar el caso
     * favorable; lo unico que puede controlar es que tan mal le puede ir.
     */
    public int peorCaso() {
        return Math.max(cumplen.size(), noCumplen.size());
    }

    /**
     * combinar(): se queda con el subconjunto que corresponde a la respuesta
     * que efectivamente dio el rival, y descarta el otro.
     *
     * En el esquema generico de D&C aca iria una mezcla de las soluciones de
     * todas las ramas. En este problema no hace falta recorrer las dos ramas:
     * la respuesta del rival nos dice cual de las dos contiene al personaje
     * secreto, asi que la otra se descarta entera. Es la misma simplificacion
     * que hace la busqueda binaria del apunte.
     */
    public List<Personaje> combinar(boolean respuesta) {
        return respuesta ? cumplen : noCumplen;
    }

    /** True si el filtro no separo nada: los candidatos quedaron todos de un lado. */
    public boolean esInutil() {
        return cumplen.isEmpty() || noCumplen.isEmpty();
    }
}
