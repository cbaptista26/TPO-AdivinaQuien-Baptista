package com.tpo.adivinaquien.modelo;

/**
 * Genero del personaje.
 *
 * Usamos un enum (una lista cerrada de valores posibles) en lugar de un String
 * para que el compilador impida que se cargue un valor invalido.
 */
public enum Genero {

    FEMENINO("Mujer"),
    MASCULINO("Hombre");

    private final String etiqueta;

    Genero(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto lindo para mostrar en consola o en la interfaz. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
