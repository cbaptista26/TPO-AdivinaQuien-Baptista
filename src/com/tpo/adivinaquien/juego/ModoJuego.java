package com.tpo.adivinaquien.juego;

/** Los modos de juego que pide el enunciado. */
public enum ModoJuego {

    JUGADOR_VS_MAQUINA("Jugador vs Maquina",
            "La persona juega contra la maquina greedy."),

    MAQUINA_VS_MAQUINA("Maquina vs Maquina",
            "Greedy contra secuencial, mostrando en consola todos los procesos "
            + "que hace cada una para acortar la busqueda."),

    SIMULACION("Simulacion estadistica",
            "Corre las 23 partidas posibles con cada estrategia y compara resultados.");

    private final String titulo;
    private final String descripcion;

    ModoJuego(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public String getTitulo()      { return titulo; }
    public String getDescripcion() { return descripcion; }
}
