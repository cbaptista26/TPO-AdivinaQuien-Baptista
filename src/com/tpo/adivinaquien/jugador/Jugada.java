package com.tpo.adivinaquien.jugador;

/**
 * Lo que un jugador decide hacer en su turno.
 *
 * Solo hay dos opciones posibles, por eso es 'sealed': el compilador garantiza
 * que nadie pueda inventar una tercera y que todos los switch sobre Jugada
 * esten completos.
 */
public sealed interface Jugada permits PreguntaFiltro, Suposicion { }
