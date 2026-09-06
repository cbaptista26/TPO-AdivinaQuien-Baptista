package com.tpo.adivinaquien.juego;

import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

/**
 * Implementacion del Oraculo que guarda el personaje secreto.
 *
 * El campo 'secreto' es private y NO tiene getter. La unica clase que puede
 * leerlo es esta misma, y solo para responder preguntas.
 */
public class OraculoPersonaje implements Oraculo {

    private final Personaje secreto;

    public OraculoPersonaje(Personaje secreto) {
        if (secreto == null) {
            throw new IllegalArgumentException("El personaje secreto no puede ser null");
        }
        this.secreto = secreto;
    }

    @Override
    public boolean responder(Filtro filtro) {
        return filtro.evaluar(secreto);
    }

    @Override
    public boolean esElPersonaje(Personaje candidato) {
        return candidato != null && candidato.getId() == secreto.getId();
    }

    /**
     * Revela el personaje secreto. SOLO para mostrar el resultado cuando la
     * partida ya termino. No lo usa ningun jugador durante el juego.
     */
    public Personaje revelarAlFinalizar() {
        return secreto;
    }
}
