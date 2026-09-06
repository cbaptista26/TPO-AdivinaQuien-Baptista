package com.tpo.adivinaquien.modelo;

/** Pregunta: el personaje tiene el pelo de este color? */
public class FiltroColorPelo extends Filtro {

    private final ColorPelo color;

    public FiltroColorPelo(ColorPelo color) {
        this.color = color;
    }

    @Override
    public boolean evaluar(Personaje personaje) {
        return personaje.getColorPelo() == color;
    }

    @Override
    public String getDescripcion() {
        return "Tiene el pelo " + color.getEtiqueta().toLowerCase() + "?";
    }

    @Override
    public String getClave() { return "PELO_" + color.name(); }
}
