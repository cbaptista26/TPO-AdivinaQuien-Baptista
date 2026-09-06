package com.tpo.adivinaquien.modelo;

/**
 * Un filtro junto con el resultado de haberlo evaluado contra los candidatos
 * actuales. Es lo que compara el greedy para decidir que preguntar.
 */
public class EvaluacionFiltro {

    private final Filtro filtro;
    private final Particion particion;

    public EvaluacionFiltro(Filtro filtro, Particion particion) {
        this.filtro = filtro;
        this.particion = particion;
    }

    public Filtro getFiltro()       { return filtro; }
    public Particion getParticion() { return particion; }

    /** Criterio de seleccion del greedy: cuanto peor puede salir esta pregunta. */
    public int peorCaso() { return particion.peorCaso(); }

    /** Cuantos candidatos se descartan en el peor caso. Solo para mostrar en consola. */
    public int descarteMinimo() { return particion.total() - particion.peorCaso(); }

    @Override
    public String toString() {
        return String.format("%-26s parte %2d/%-2d  peor caso: %2d",
                filtro.getDescripcion(),
                particion.cantidadCumplen(),
                particion.cantidadNoCumplen(),
                peorCaso());
    }
}
