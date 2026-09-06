package com.tpo.adivinaquien.modelo;

/**
 * Una pregunta de si/no que se le puede hacer al rival.
 *
 * Es abstracta porque cada tipo de pregunta se responde mirando un atributo
 * distinto del personaje, pero la maquina no necesita saber cual: le alcanza
 * con poder llamar a evaluar(personaje) y recibir true o false. Gracias a esto
 * el algoritmo greedy recorre todos los filtros con un solo bucle, sin un 'if'
 * por cada caracteristica.
 *
 * Cada Filtro representa UNA pregunta binaria. No creamos el filtro
 * complementario (por ejemplo "es mujer?" ademas de "es hombre?") porque
 * responder que NO a uno equivale a responder que SI al otro: aportan
 * exactamente la misma informacion y solo inflarian la lista de candidatos
 * del greedy con opciones redundantes.
 */
public abstract class Filtro {

    /** Responde si este personaje cumple o no la condicion del filtro. */
    public abstract boolean evaluar(Personaje personaje);

    /** La pregunta tal como se le muestra al jugador. */
    public abstract String getDescripcion();

    /** Identificador estable, se usa para saber si un filtro ya fue preguntado. */
    public abstract String getClave();

    @Override
    public String toString() {
        return getDescripcion();
    }
}
