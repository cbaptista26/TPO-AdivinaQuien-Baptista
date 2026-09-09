package com.tpo.adivinaquien.catalogo;

import com.tpo.adivinaquien.modelo.Personaje;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chequea que el catalogo cumpla las condiciones que el resto del programa da
 * por sentadas.
 *
 * Antes estas verificaciones estaban dentro de CatalogoPersonajes, que ya
 * cargaba y ordenaba. Las saque aparte por el principio de responsabilidad
 * unica: si maniana agrego una validacion nueva toco esta clase y no la de
 * carga.
 *
 * Las dos corren al arrancar el programa y tiran excepcion si fallan. Prefiero
 * que reviente al iniciar antes que enterarme en medio de la defensa.
 */
public class VerificadorCatalogo {

    private VerificadorCatalogo() { }

    /**
     * Verifica que no haya dos personajes con la misma combinacion de
     * atributos. Si los hubiera, la maquina podria quedar con candidatos
     * empatados y sin ninguna pregunta capaz de separarlos.
     *
     * Usa un HashSet aprovechando que add() devuelve false si el elemento ya
     * estaba: detecta duplicados en una sola pasada, Theta(n).
     */
    public static void verificarUnicidad(List<Personaje> personajes, int cantidadEsperada) {

        if (personajes.size() != cantidadEsperada) {
            throw new IllegalStateException("El catalogo debe tener " + cantidadEsperada
                    + " personajes y tiene " + personajes.size());
        }

        Set<String> vistas = new HashSet<>();
        for (Personaje p : personajes) {
            if (!vistas.add(p.claveAtributos())) {
                throw new IllegalStateException("Personaje duplicado: " + p.getNombre());
            }
        }
    }

    /**
     * Verifica que ordenar el lote con MergeSort y construir la lista
     * insertando de a uno con busqueda binaria den el mismo resultado.
     *
     * Es una verificacion cruzada: si dos algoritmos distintos coinciden sobre
     * los 23 personajes, es muy poco probable que alguno este mal implementado.
     */
    public static void verificarQueCoincidan(List<Personaje> porMergeSort,
                                             List<Personaje> porInsercionBinaria) {

        if (porMergeSort.size() != porInsercionBinaria.size()) {
            throw new IllegalStateException("Las dos listas ordenadas tienen distinto tamanio.");
        }
        for (int i = 0; i < porInsercionBinaria.size(); i++) {
            if (porMergeSort.get(i).getId() != porInsercionBinaria.get(i).getId()) {
                throw new IllegalStateException(
                        "MergeSort y la insercion binaria difieren en la posicion " + i);
            }
        }
    }
}
