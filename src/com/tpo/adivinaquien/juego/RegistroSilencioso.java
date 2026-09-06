package com.tpo.adivinaquien.juego;

/** Descarta todo. Se usa para correr miles de partidas sin llenar la pantalla. */
public class RegistroSilencioso implements RegistroRazonamiento {

    @Override
    public void registrar(String mensaje) {
        // no hace nada a proposito
    }
}
