package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.Optional;

/**
 * El jugador humano. La jugada la decide la persona y se la pasa a la Partida
 * desde la vista (un Scanner en consola, un boton en Swing).
 *
 * Hereda de Jugador toda la reduccion de candidatos, asi que su tablero se
 * achica automaticamente con el mismo Divide and Conquer que usa la maquina: la
 * persona no tiene que ir tachando personajes a mano.
 */
public class JugadorHumano extends Jugador {

    public JugadorHumano(String nombre, List<Personaje> candidatosIniciales,
                         RegistroRazonamiento registro) {
        super(nombre, candidatosIniciales, registro);
    }

    @Override
    public boolean esMaquina() { return false; }

    @Override
    public String getCriterio() { return "Decide la persona."; }

    /**
     * Vacio: este jugador no decide solo. La vista interpreta el Optional vacio
     * como "pedile la jugada a la persona".
     */
    @Override
    public Optional<Jugada> decidirJugada() {
        return Optional.empty();
    }
}
