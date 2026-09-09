package com.tpo.adivinaquien.catalogo;

import com.tpo.adivinaquien.modelo.ColorPelo;
import com.tpo.adivinaquien.modelo.Genero;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;

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
        VerificadorCatalogo.verificarUnicidad(ordenDeCarga, CANTIDAD_PERSONAJES);
        VerificadorCatalogo.verificarQueCoincidan(ordenadosPorMergeSort, ordenados);
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


    public static CatalogoPersonajes getInstancia() { return INSTANCIA; }

    /**
     * Los 23 personajes de la Academia Umbraluz, agrupados solo por genero como
     * pide el enunciado.
     *
     * Aca esta la cuenta que me ordeno todo el catalogo. Los atributos dan
     * 2 generos x 2 (rapado) x 2 (anteojos) x 3 colores = 24 combinaciones
     * posibles, y el enunciado pide 23 personajes: entran justo, uno por
     * combinacion, y sobra una.
     *
     * Aproveche eso y uso 23 combinaciones DISTINTAS, ninguna repetida. La
     * consecuencia practica es que la maquina siempre termina con un unico
     * candidato y nunca tiene que adivinar al azar.
     *
     * Mi primera version hacia que los rapados no tuvieran color de pelo, que me
     * parecia mas realista. Al hacer la cuenta vi que eso bajaba las
     * combinaciones a 16, y con 23 personajes forzosamente iban a quedar
     * personajes identicos, imposibles de separar con ninguna pregunta. Por eso
     * les dejo color, que se entiende como el de las cejas o la barba.
     *
     * La combinacion que queda afuera es (bruja, rapada, con anteojos, amarillo).
     *
     * El prefijo del nombre es pura ambientacion, no un atributo nuevo: es
     * "Profesor/a" cuando el personaje es calvo y "Aprendiz/a" cuando no, asi
     * que reutiliza el filtro de calvicie que ya existe en vez de agregar un
     * campo que el enunciado no pide.
     */
    private void cargarPersonajes() {
        // 11 aprendizas y profesoras
        agregar("Aprendiza Aurelia",   Genero.FEMENINO,  false, false, ColorPelo.COLORADO);
        agregar("Aprendiza Briseida",  Genero.FEMENINO,  false, false, ColorPelo.NEGRO);
        agregar("Aprendiza Cassandra", Genero.FEMENINO,  false, false, ColorPelo.AMARILLO);
        agregar("Aprendiza Delphine",  Genero.FEMENINO,  false, true,  ColorPelo.COLORADO);
        agregar("Aprendiza Elowen",    Genero.FEMENINO,  false, true,  ColorPelo.NEGRO);
        agregar("Aprendiza Faelynn",   Genero.FEMENINO,  false, true,  ColorPelo.AMARILLO);
        agregar("Profesora Griselda",  Genero.FEMENINO,  true,  false, ColorPelo.COLORADO);
        agregar("Profesora Hesper",    Genero.FEMENINO,  true,  false, ColorPelo.NEGRO);
        agregar("Profesora Ivessa",    Genero.FEMENINO,  true,  false, ColorPelo.AMARILLO);
        agregar("Profesora Jezra",     Genero.FEMENINO,  true,  true,  ColorPelo.COLORADO);
        agregar("Profesora Kaelith",   Genero.FEMENINO,  true,  true,  ColorPelo.NEGRO);

        // 12 aprendices y profesores
        agregar("Aprendiz Aldric",     Genero.MASCULINO, false, false, ColorPelo.COLORADO);
        agregar("Aprendiz Baltasar",   Genero.MASCULINO, false, false, ColorPelo.NEGRO);
        agregar("Aprendiz Ciro",       Genero.MASCULINO, false, false, ColorPelo.AMARILLO);
        agregar("Aprendiz Draven",     Genero.MASCULINO, false, true,  ColorPelo.COLORADO);
        agregar("Aprendiz Ewald",      Genero.MASCULINO, false, true,  ColorPelo.NEGRO);
        agregar("Aprendiz Fenwick",    Genero.MASCULINO, false, true,  ColorPelo.AMARILLO);
        agregar("Profesor Gideon",     Genero.MASCULINO, true,  false, ColorPelo.COLORADO);
        agregar("Profesor Hadrian",    Genero.MASCULINO, true,  false, ColorPelo.NEGRO);
        agregar("Profesor Ivor",       Genero.MASCULINO, true,  false, ColorPelo.AMARILLO);
        agregar("Profesor Joran",      Genero.MASCULINO, true,  true,  ColorPelo.COLORADO);
        agregar("Profesor Lysander",   Genero.MASCULINO, true,  true,  ColorPelo.NEGRO);
        agregar("Profesor Magnus",     Genero.MASCULINO, true,  true,  ColorPelo.AMARILLO);
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
            trazaDeCarga.add(String.format("  %-20s -> %d comparacion(es), va a la posicion %d",
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
