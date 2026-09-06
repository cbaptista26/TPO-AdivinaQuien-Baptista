package com.tpo.adivinaquien.juego;

/** Muestra el razonamiento en la terminal. */
public class RegistroConsola implements RegistroRazonamiento {

    @Override
    public void registrar(String mensaje) {
        System.out.println(mensaje);
    }
}
