package com.tpo.adivinaquien.modelo;

/** Pregunta: el personaje es de este genero? */
public class FiltroGenero extends Filtro {

    private final Genero genero;

    public FiltroGenero(Genero genero) {
        this.genero = genero;
    }

    public Genero getGenero() { return genero; }

    @Override
    public boolean evaluar(Personaje personaje) {
        return personaje.getGenero() == genero;
    }

    @Override
    public String getDescripcion() {
        return genero == Genero.MASCULINO ? "Es un hombre?" : "Es una mujer?";
    }

    @Override
    public String getClave() { return "GENERO"; }
}
