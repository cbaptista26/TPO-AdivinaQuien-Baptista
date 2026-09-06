package com.tpo.adivinaquien.modelo;

/** Pregunta: el personaje es calvo? */
public class FiltroCalvo extends Filtro {

    @Override
    public boolean evaluar(Personaje personaje) {
        return personaje.isCalvo();
    }

    @Override
    public String getDescripcion() { return "Es calvo?"; }

    @Override
    public String getClave() { return "CALVO"; }
}
