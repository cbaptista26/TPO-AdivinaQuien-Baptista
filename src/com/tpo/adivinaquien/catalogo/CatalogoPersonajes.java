package com.tpo.adivinaquien.catalogo;

import com.tpo.adivinaquien.modelo.ColorPelo;
import com.tpo.adivinaquien.modelo.Genero;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Carga los 23 personajes y arma la lista ordenada de la maquina.
 *
 * DOS SITUACIONES DISTINTAS, DOS ALGORITMOS
 *
 * 1) ORDENAMIENTO INICIAL DEL LOTE: los 23 personajes se declaran juntos y
 *    llegan agrupados solo por genero. Como se tiene el conjunto completo de
 *    entrada, se ordena con MERGESORT: Theta(n log n) garantizado, sin
 *    depender de como vengan los datos. Ver OrdenadorPersonajes.mergeSort().
 *
 * 2) ALTA INDIVIDUAL POSTERIOR: si despues se agrega un personaje suelto, no
 *    conviene reordenar todo de nuevo. Se lo INSERTA en su posicion usando
 *    BUSQUEDA BINARIA: Theta(log n) para ubicar el lugar mas Theta(n) para
 *    desplazar. Ver buscarPosicion().
 *
 * Los dos son Divide and Conquer, aplicados a situaciones distintas. El
 * programa verifica al arrancar que ambos caminos produzcan exactamente la
 * misma lista ordenada.
 */
public class CatalogoPersonajes {

    public static final int CANTIDAD_PERSONAJES = 23;

    private static final CatalogoPersonajes INSTANCIA = new CatalogoPersonajes();

    private int siguienteId = 1;

    /** Como llegan: agrupados solo por genero. Es el tablero inicial. */
    private final List<Personaje> ordenDeCarga = new ArrayList<>();

    /** La lista que la maquina mantiene ordenada por atributos, alta por alta. */
    private final List<Personaje> ordenados = new ArrayList<>();

    /** Traza de la insercion binaria, para mostrarla en consola. */
    private final List<String> trazaDeCarga = new ArrayList<>();

    /** La lista ordenada con MergeSort sobre el lote completo. */
    private final List<Personaje> ordenadosPorMergeSort = new ArrayList<>();

    private CatalogoPersonajes() {
        cargarPersonajes();
        ordenarLoteInicial();
        verificarUnicidad();
        verificarQueAmbosCaminosCoincidan();
    }

    /**
     * Ordenamiento inicial del lote completo con MergeSort.
     *
     * Es el caso para el que MergeSort sirve: se tienen los 23 personajes de
     * entrada al mismo tiempo, asi que se puede partir el conjunto a la mitad.
     */
    private void ordenarLoteInicial() {
        ordenadosPorMergeSort.addAll(
                OrdenadorPersonajes.mergeSort(ordenDeCarga, Personaje.POR_ATRIBUTOS));
    }

    /**
     * Comprueba que ordenar el lote con MergeSort y construir la lista
     * insertando de a uno con busqueda binaria den el mismo resultado.
     *
     * Sirve como verificacion cruzada: si los dos algoritmos coinciden sobre
     * los 23 personajes, es muy poco probable que alguno este mal implementado.
     */
    private void verificarQueAmbosCaminosCoincidan() {
        if (ordenadosPorMergeSort.size() != ordenados.size()) {
            throw new IllegalStateException("Las dos listas ordenadas tienen distinto tamanio.");
        }
        for (int i = 0; i < ordenados.size(); i++) {
            if (ordenadosPorMergeSort.get(i).getId() != ordenados.get(i).getId()) {
                throw new IllegalStateException(
                        "MergeSort y la insercion binaria difieren en la posicion " + i);
            }
        }
    }

    public static CatalogoPersonajes getInstancia() { return INSTANCIA; }

    /**
     * Los 23 personajes, agrupados solo por genero.
     *
     * El espacio de combinaciones es 2 generos x 2 (calvo) x 2 (lentes) x 3
     * colores = 24. Usamos 23 de esas 24, una sola vez cada una, asi que NO HAY
     * DOS PERSONAJES IGUALES y la maquina nunca tiene que desempatar al azar.
     * Queda afuera (femenino, calva, con lentes, amarillo).
     */
    private void cargarPersonajes() {
        // 11 mujeres
        agregar("Alma",      Genero.FEMENINO,  false, false, ColorPelo.COLORADO);
        agregar("Bianca",    Genero.FEMENINO,  false, false, ColorPelo.NEGRO);
        agregar("Carla",     Genero.FEMENINO,  false, false, ColorPelo.AMARILLO);
        agregar("Delfina",   Genero.FEMENINO,  false, true,  ColorPelo.COLORADO);
        agregar("Emma",      Genero.FEMENINO,  false, true,  ColorPelo.NEGRO);
        agregar("Fatima",    Genero.FEMENINO,  false, true,  ColorPelo.AMARILLO);
        agregar("Greta",     Genero.FEMENINO,  true,  false, ColorPelo.COLORADO);
        agregar("Hilda",     Genero.FEMENINO,  true,  false, ColorPelo.NEGRO);
        agregar("Ivana",     Genero.FEMENINO,  true,  false, ColorPelo.AMARILLO);
        agregar("Jimena",    Genero.FEMENINO,  true,  true,  ColorPelo.COLORADO);
        agregar("Keila",     Genero.FEMENINO,  true,  true,  ColorPelo.NEGRO);

        // 12 hombres
        agregar("Adrian",    Genero.MASCULINO, false, false, ColorPelo.COLORADO);
        agregar("Bautista",  Genero.MASCULINO, false, false, ColorPelo.NEGRO);
        agregar("Ciro",      Genero.MASCULINO, false, false, ColorPelo.AMARILLO);
        agregar("Damian",    Genero.MASCULINO, false, true,  ColorPelo.COLORADO);
        agregar("Ezequiel",  Genero.MASCULINO, false, true,  ColorPelo.NEGRO);
        agregar("Facundo",   Genero.MASCULINO, false, true,  ColorPelo.AMARILLO);
        agregar("Gaspar",    Genero.MASCULINO, true,  false, ColorPelo.COLORADO);
        agregar("Hugo",      Genero.MASCULINO, true,  false, ColorPelo.NEGRO);
        agregar("Ivan",      Genero.MASCULINO, true,  false, ColorPelo.AMARILLO);
        agregar("Joaquin",   Genero.MASCULINO, true,  true,  ColorPelo.COLORADO);
        agregar("Lisandro",  Genero.MASCULINO, true,  true,  ColorPelo.NEGRO);
        agregar("Matias",    Genero.MASCULINO, true,  true,  ColorPelo.AMARILLO);
    }

    /** Alta: asigna el id autoincremental e inserta ordenado. */
    private void agregar(String nombre, Genero genero,
                         boolean calvo, boolean usaLentes, ColorPelo colorPelo) {

        Personaje nuevo = new Personaje(siguienteId++, nombre, genero, calvo, usaLentes, colorPelo);
        ordenDeCarga.add(nuevo);
        insertarOrdenado(nuevo);
    }

    /**
     * La lista 'ordenados' es un invariante: siempre esta ordenada segun
     * Personaje.POR_ATRIBUTOS, por eso se puede buscar binariamente sobre ella
     * aunque todavia no esten cargados los 23.
     */
    private void insertarOrdenado(Personaje nuevo) {
        ordenados.add(buscarPosicion(nuevo, 0, ordenados.size() - 1, 0), nuevo);
    }

    /**
     * Busqueda binaria recursiva: devuelve el indice donde va el personaje.
     *
     *   CasoBase(x)        -> ini > fin: la posicion es 'ini'
     *   descomponer(x)     -> partir el rango por el elemento del medio
     *   combinar           -> no hace falta: el orden garantiza que la posicion
     *                         no puede estar en la otra mitad
     *
     * T(n) = T(n/2) + c -> caso de division con a=1, b=2, k=0. Como a = b^k,
     * queda Theta(log n).
     */
    private int buscarPosicion(Personaje nuevo, int ini, int fin, int profundidad) {

        if (ini > fin) {                                    // caso base
            trazaDeCarga.add(String.format("  %-10s -> %d comparacion(es), va a la posicion %d",
                    nuevo.getNombre(), profundidad, ini));
            return ini;
        }

        int medio = (ini + fin) / 2;

        if (Personaje.POR_ATRIBUTOS.compare(nuevo, ordenados.get(medio)) < 0) {
            return buscarPosicion(nuevo, ini, medio - 1, profundidad + 1);
        } else {
            return buscarPosicion(nuevo, medio + 1, fin, profundidad + 1);
        }
    }

    /**
     * Chequea que no haya dos personajes con los mismos atributos. Si fallara,
     * la maquina podria quedar con candidatos empatados y sin ninguna pregunta
     * capaz de separarlos.
     */
    private void verificarUnicidad() {
        Set<String> vistas = new HashSet<>();
        for (Personaje p : ordenDeCarga) {
            if (!vistas.add(p.claveAtributos())) {
                throw new IllegalStateException("Personaje duplicado: " + p.getNombre());
            }
        }
        if (ordenDeCarga.size() != CANTIDAD_PERSONAJES) {
            throw new IllegalStateException("El catalogo debe tener " + CANTIDAD_PERSONAJES
                    + " personajes y tiene " + ordenDeCarga.size());
        }
    }

    /** Lista en orden de alta (agrupada por genero): el tablero inicial. */
    public List<Personaje> getOrdenDeCarga() { return new ArrayList<>(ordenDeCarga); }

    /** Lista ordenada por atributos: la vista interna de la maquina. */
    public List<Personaje> getOrdenados() { return new ArrayList<>(ordenados); }

    /** La misma lista, pero obtenida ordenando el lote completo con MergeSort. */
    public List<Personaje> getOrdenadosPorMergeSort() {
        return new ArrayList<>(ordenadosPorMergeSort);
    }

    public List<String> getTrazaDeCarga() { return new ArrayList<>(trazaDeCarga); }

    public int getCantidad() { return ordenDeCarga.size(); }

    public Personaje buscarPorNombre(String nombre) {
        for (Personaje p : ordenDeCarga) {
            if (p.getNombre().equalsIgnoreCase(nombre.trim())) return p;
        }
        return null;
    }
}
