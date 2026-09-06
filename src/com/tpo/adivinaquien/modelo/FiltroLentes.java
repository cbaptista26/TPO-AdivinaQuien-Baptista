package com.tpo.adivinaquien.modelo;

/** Pregunta: el personaje usa lentes? */
public class FiltroLentes extends Filtro {

    @Override
    public boolean evaluar(Personaje personaje) {
        return personaje.isUsaLentes();
    }

    @Override
    public String getDescripcion() { return "Usa lentes?"; }

    @Override
    public String getClave() { return "LENTES"; }
}
