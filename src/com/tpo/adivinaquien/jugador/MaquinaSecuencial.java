package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;

/**
 * Esta maquina la hice a proposito para tener con que comparar.
 *
 * Pregunta los filtros en un orden fijo, sin fijarse cuanto corta cada uno. Es
 * la version ingenua: seria como recorrer una lista de punta a punta en vez de
 * partirla al medio.
 *
 * Juega peor que la greedy, pero OJO: no usa Random. El criterio se explica en
 * una frase, "recorro los filtros en el orden en que los declare y pregunto el
 * primero que sirva". Eso era importante porque el profesor pidio que hasta la
 * maquina que juega mal tenga un criterio explicable.
 *
 * Teniendo las dos, el modo maquina vs maquina no cuenta que greedy es mejor:
 * lo muestra con numeros.
 *
 * El orden fijo arranca por los colores, que son los filtros mas desparejos
 * (cortan 8/15 y 7/16), asi la diferencia se nota. Ver seccion 5.3.
 */
public class MaquinaSecuencial extends JugadorMaquina {

    private static final String[] ORDEN_FIJO = {
            "PELO_COLORADO", "PELO_NEGRO", "PELO_AMARILLO", "GENERO", "CALVO", "LENTES"
    };

    private final boolean conFactibilidad;

    /** Version pura: orden fijo y ningun elemento del esquema greedy. */
    public MaquinaSecuencial(String nombre, List<Personaje> candidatosIniciales,
                             RegistroRazonamiento registro) {
        this(nombre, candidatosIniciales, registro, false);
    }

    /**
     * @param conFactibilidad si es true, saltea los filtros cuya respuesta ya
     *                        esta determinada. Sigue sin aplicar la funcion de
     *                        seleccion, asi que permite medir cuanto aporta
     *                        cada elemento del greedy por separado.
     */
    public MaquinaSecuencial(String nombre, List<Personaje> candidatosIniciales,
                             RegistroRazonamiento registro, boolean conFactibilidad) {
        super(nombre, candidatosIniciales, registro);
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
