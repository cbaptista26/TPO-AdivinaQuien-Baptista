package com.tpo.adivinaquien.juego;

/**
 * Canal por donde la maquina cuenta que esta pensando.
 *
 * La logica del juego NUNCA llama a System.out directamente: le pasa el texto
 * a un RegistroRazonamiento. Esto es lo que mantiene separadas las dos vistas
 * que pide la catedra:
 *
 *   - RegistroConsola      -> imprime en la terminal (la defensa oral)
 *   - RegistroSwing        -> escribe en un JTextArea de la interfaz
 *   - RegistroSilencioso   -> descarta todo (para simulaciones masivas)
 *
 * Si se cierra la consola, el juego sigue funcionando igual: solo cambia el
 * objeto que recibe los mensajes.
 */
public interface RegistroRazonamiento {

    /** Un paso del razonamiento. */
    void registrar(String mensaje);

    /** Un encabezado de seccion (turno, fase de carga, etc). */
    default void registrarTitulo(String titulo) {
        registrar("");
        registrar("--- " + titulo + " ---");
    }
}
