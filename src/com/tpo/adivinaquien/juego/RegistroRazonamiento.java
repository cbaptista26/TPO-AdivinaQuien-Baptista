package com.tpo.adivinaquien.juego;

/**
 * Canal por donde la maquina cuenta que esta pensando.
 *
 * El motor NUNCA llama a System.out: le pasa el texto a un
 * RegistroRazonamiento. Eso es lo que mantiene separadas las dos vistas:
 * RegistroConsola imprime, RegistroSwing escribe en un JTextArea,
 * RegistroSilencioso descarta. Si se cierra la consola, el juego sigue.
 */
public interface RegistroRazonamiento {

    void registrar(String mensaje);

    default void registrarTitulo(String titulo) {
        registrar("");
        registrar("--- " + titulo + " ---");
    }
}
