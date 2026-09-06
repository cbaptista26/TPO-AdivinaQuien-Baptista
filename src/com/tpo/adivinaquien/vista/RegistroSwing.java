package com.tpo.adivinaquien.vista;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * La implementacion Swing del RegistroRazonamiento: escribe en un JTextArea en
 * lugar de imprimir en la terminal. El motor no cambia ni una linea.
 *
 * Todo lo que toca el componente se envuelve en SwingUtilities.invokeLater
 * porque solo el Event Dispatch Thread puede modificar componentes Swing.
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
            destino.setCaretPosition(destino.getDocument().getLength());
        });
    }

    /** Vacia el panel. Se llama al empezar una partida nueva. */
    public void limpiar() {
        SwingUtilities.invokeLater(() -> destino.setText(""));
    }
}
