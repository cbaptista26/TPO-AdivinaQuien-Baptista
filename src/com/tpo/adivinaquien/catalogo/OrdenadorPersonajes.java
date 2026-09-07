package com.tpo.adivinaquien.catalogo;

import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Los tres algoritmos de ordenamiento que se comparan en la documentacion.
 *
 * MergeSort es el que usa el catalogo para ordenar el lote inicial de 23
 * personajes. Burbujeo e insercion estan solo para la comparacion de tiempos:
 * no se usan en el juego.
 *
 * Ver BenchmarkOrdenamiento para la medicion.
 */
public class OrdenadorPersonajes {

    private OrdenadorPersonajes() { }

    // ==================================================================
    // MERGESORT  -  Divide and Conquer  -  Theta(n log n) siempre
    // ==================================================================

    /**
     * MergeSort: divide la lista en dos mitades, ordena cada una
     * recursivamente y mezcla las dos mitades ya ordenadas.
     *
     * Esquema de la catedra:
     *   CasoBase(x)        -> lista de 0 o 1 elemento, ya esta ordenada
     *   SolucionDirecta(x) -> devolverla tal cual
     *   descomponer(x)     -> partir al medio en dos sublistas
     *   combinar           -> mezclar (merge) las dos mitades ordenadas
     *
     * Recurrencia: T(n) = 2T(n/2) + Theta(n), porque el merge recorre los n
     * elementos. Caso de division con a=2, b=2, k=1. Como a = b^k (2 = 2^1),
     * queda Theta(n^k log n) = Theta(n log n), y esto vale SIEMPRE, sin
     * importar como vengan los datos de entrada.
     *
     * Devuelve una lista nueva; no modifica la original.
     */
    public static List<Personaje> mergeSort(List<Personaje> lista,
                                            Comparator<Personaje> criterio) {

        // CasoBase(x): una lista de 0 o 1 elemento ya esta ordenada.
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        }

        // descomponer(x): dos mitades.
        int medio = lista.size() / 2;
        List<Personaje> izquierda = mergeSort(new ArrayList<>(lista.subList(0, medio)), criterio);
        List<Personaje> derecha   = mergeSort(new ArrayList<>(lista.subList(medio, lista.size())), criterio);

        // combinar: mezclar las dos mitades ya ordenadas.
        return mezclar(izquierda, derecha, criterio);
    }

    /**
     * El "merge": recorre las dos mitades en paralelo tomando siempre el menor
     * de los dos frentes. Cuesta Theta(n) porque cada elemento se mira una vez.
     */
    private static List<Personaje> mezclar(List<Personaje> a, List<Personaje> b,
                                           Comparator<Personaje> criterio) {

        List<Personaje> resultado = new ArrayList<>(a.size() + b.size());
        int i = 0, j = 0;

        while (i < a.size() && j < b.size()) {
            if (criterio.compare(a.get(i), b.get(j)) <= 0) {
                resultado.add(a.get(i++));
            } else {
                resultado.add(b.get(j++));
            }
        }
        while (i < a.size()) resultado.add(a.get(i++));
        while (j < b.size()) resultado.add(b.get(j++));

        return resultado;
    }

    // ==================================================================
    // ALGORITMOS CUADRATICOS  -  solo para comparar tiempos
    // ==================================================================

    /**
     * Burbujeo: compara pares adyacentes y los intercambia si estan al reves,
     * repitiendo hasta que no haya intercambios. Dos ciclos anidados -> O(n^2).
     * No se usa en el juego.
     */
    public static List<Personaje> burbujeo(List<Personaje> lista,
                                           Comparator<Personaje> criterio) {

        List<Personaje> r = new ArrayList<>(lista);
        boolean huboCambios = true;

        for (int fin = r.size() - 1; fin > 0 && huboCambios; fin--) {
            huboCambios = false;
            for (int i = 0; i < fin; i++) {
                if (criterio.compare(r.get(i), r.get(i + 1)) > 0) {
                    Personaje tmp = r.get(i);
                    r.set(i, r.get(i + 1));
                    r.set(i + 1, tmp);
                    huboCambios = true;
                }
            }
        }
        return r;
    }

    /**
     * Insercion simple: toma cada elemento y lo corre hacia atras hasta su
     * lugar. O(n^2) en el peor caso, O(n) si la entrada ya viene ordenada.
     * No se usa en el juego.
     */
    public static List<Personaje> insercionSimple(List<Personaje> lista,
                                                  Comparator<Personaje> criterio) {

        List<Personaje> r = new ArrayList<>(lista);

        for (int i = 1; i < r.size(); i++) {
            Personaje actual = r.get(i);
            int j = i - 1;
            while (j >= 0 && criterio.compare(r.get(j), actual) > 0) {
                r.set(j + 1, r.get(j));
                j--;
            }
            r.set(j + 1, actual);
        }
        return r;
    }
}
