package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.Filtro;

import java.util.List;

/**
 * BUSQUEDA SECUENCIAL: la maquina de contraste. Pregunta los filtros en un
 * orden fijo, sin evaluar cuanto corta cada uno. Es la version ingenua de la
 * eleccion de pregunta, equivalente a recorrer un arreglo de punta a punta en
 * vez de partirlo por la mitad.
 *
 * Juega peor que la greedy a proposito, pero tiene un criterio explicable y
 * NO usa Random. Existiendo las dos, el modo Maquina vs Maquina no cuenta que
 * greedy es mejor: lo demuestra con numeros.
 *
 * El orden fijo arranca por los filtros de color (los menos parejos: cortan
 * 8/15 y 7/16) para que la diferencia sea observable. Ver seccion 4 de la
 * documentacion.
 */
public class MaquinaSecuencial extends JugadorMaquina {

    private static final String[] ORDEN_FIJO = {
            "PELO_COLORADO", "PELO_NEGRO", "PELO_AMARILLO", "GENERO", "CALVO", "LENTES"
    };

    private final boolean conFactibilidad;

    /** Version pura: orden fijo y ningun elemento del esquema greedy. */
    public MaquinaSecuencial(String nombre, RegistroRazonamiento registro) {
        this(nombre, registro, false);
    }

    /**
     * @param conFactibilidad si es true, saltea los filtros cuya respuesta ya
     *                        esta determinada. Sigue sin aplicar la funcion de
     *                        seleccion, asi que permite medir cuanto aporta
     *                        cada elemento del greedy por separado.
     */
    public MaquinaSecuencial(String nombre, RegistroRazonamiento registro, boolean conFactibilidad) {
        super(nombre, registro);
        this.conFactibilidad = conFactibilidad;
    }

    @Override
    protected boolean aplicaFactibilidad() { return conFactibilidad; }

    @Override
    public String getCriterio() {
        return "Busqueda secuencial: pregunto los filtros en un orden fijo declarado "
             + "de antemano, sin evaluar cuanto corta cada uno"
             + (conFactibilidad ? ", pero salteo los que ya no aportan informacion." : ".");
    }

    /** Devuelve el primer filtro del orden fijo que siga siendo factible. */
    @Override
    protected Filtro elegirFiltro(List<Filtro> factibles) {

        for (String clave : ORDEN_FIJO) {
            for (Filtro f : factibles) {
                if (f.getClave().equals(clave)) {
                    registro.registrar(String.format(
                            "    [%s] SELECCION secuencial: \"%s\", el primero pendiente "
                            + "del orden fijo. No evaluo cuanto corta.",
                            getNombre(), f.getDescripcion()));
                    return f;
                }
            }
        }
        return factibles.get(0);
    }
}
