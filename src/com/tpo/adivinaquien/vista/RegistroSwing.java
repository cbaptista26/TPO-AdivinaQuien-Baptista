package com.tpo.adivinaquien.vista;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * La implementacion Swing del RegistroRazonamiento: en vez de imprimir en la
 * terminal, escribe en un JTextArea de la ventana.
 *
 * ESTA CLASE ES LA PRUEBA DE QUE LAS DOS VISTAS ESTAN SEPARADAS
 * El motor del juego (Partida, JugadorMaquina, MaquinaGreedy) no cambia ni una
 * linea para funcionar con Swing. Lo unico que cambia es que objeto recibe los
 * mensajes: RegistroConsola imprime, RegistroSwing escribe en pantalla,
 * RegistroSilencioso descarta. Si se borra la interfaz grafica entera, el juego
 * sigue andando por consola exactamente igual.
 *
 * DETALLE TECNICO IMPORTANTE
 * Swing tiene una unica hebra que puede tocar los componentes, el Event
 * Dispatch Thread (EDT). Si otra hebra modifica un JTextArea directamente, la
 * interfaz puede quedar corrupta o congelarse. Por eso todo lo que toca el
 * componente se envuelve en SwingUtilities.invokeLater, que encola el trabajo
 * para que lo haga el EDT.
 */
public class RegistroSwing implements RegistroRazonamiento {

    private final JTextArea destino;

    public RegistroSwing(JTextArea destino) {
        this.destino = destino;
    }

    @Override
    public void registrar(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            destino.append(mensaje + "\n");
            // Auto-scroll: deja siempre visible la ultima linea escrita.
            destino.setCaretPosition(destino.getDocument().getLength());
        });
    }

    /** Vacia el panel de razonamiento. Se llama al empezar una partida nueva. */
    public void limpiar() {
        SwingUtilities.invokeLater(() -> destino.setText(""));
    }
}