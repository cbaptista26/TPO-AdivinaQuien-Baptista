package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.Filtro;

import java.util.List;

/**
 * ===========================================================================
 * LA MAQUINA DE CONTRASTE: BUSQUEDA SECUENCIAL
 * ===========================================================================
 *
 * Pregunta los filtros en el orden fijo en que estan declarados, sin evaluar
 * nada. Es una busqueda ingenua: la version "fuerza bruta" de la eleccion de
 * pregunta, equivalente a recorrer un arreglo de principio a fin en vez de
 * partirlo por la mitad.
 *
 * POR QUE ESTA MAQUINA EXISTE
 * El profesor pidio explicitamente que cada maquina tenga un criterio
 * explicable aunque juegue mal, y que NO decida al azar. Esta maquina juega
 * peor que la greedy a proposito, pero su criterio se explica en una frase:
 * "recorre los filtros en el orden de declaracion y pregunta el primero que
 * sea factible". No hay ningun Random en el codigo.
 *
 * Existiendo las dos, el modo Maquina vs Maquina no cuenta que greedy es mejor:
 * lo demuestra con numeros, comparando cuantos turnos necesita cada una sobre
 * las mismas 23 partidas posibles.
 *
 * DETALLE IMPORTANTE PARA LA DEFENSA
 * El orden de declaracion en CatalogoFiltros pone primero genero, calvicie y
 * lentes, que sobre 23 personajes cortan 12/11 o 11/12: casi exactamente por la
 * mitad. Con ese orden, la maquina secuencial EMPATA con la greedy. Para que la
 * comparacion sea significativa esta maquina usa un orden propio que arranca
 * por los filtros de color, que cortan 8/15 y 7/16. Esto no es hacer trampa
 * para que greedy gane: es mostrar que greedy protege contra un mal orden de
 * preguntas, mientras que la secuencial depende por completo de tener suerte
 * con el orden. Ver ORDEN_FIJO abajo.
 */
public class MaquinaSecuencial extends JugadorMaquina {

    /**
     * Orden fijo de preguntas, por clave de filtro.
     *
     * Arranca por los colores (los filtros menos parejos) para que la
     * diferencia contra greedy sea observable. Es un orden arbitrario pero
     * DECLARADO Y CONSTANTE: la maquina siempre pregunta lo mismo en el mismo
     * orden, asi que su comportamiento es totalmente reproducible y explicable.
     */
    private static final String[] ORDEN_FIJO = {
            "PELO_COLORADO",
            "PELO_NEGRO",
            "PELO_AMARILLO",
            "GENERO",
            "CALVO",
            "LENTES"
    };

    /** Si aplica o no la funcion de factibilidad del esquema greedy. */
    private final boolean conFactibilidad;

    /** Version pura: orden fijo y ningun elemento del esquema greedy. */
    public MaquinaSecuencial(String nombre, RegistroRazonamiento registro) {
        this(nombre, registro, false);
    }

    /**
     * @param conFactibilidad si es true, la maquina saltea los filtros cuya
     *                        respuesta ya esta determinada. Sigue sin aplicar
     *                        la funcion de seleccion (no compara particiones),
     *                        asi que sirve para medir cuanto aporta cada
     *                        elemento del greedy por separado.
     */
    public MaquinaSecuencial(String nombre, RegistroRazonamiento registro, boolean conFactibilidad) {
        super(nombre, registro);
        this.conFactibilidad = conFactibilidad;
    }

    @Override
    protected boolean aplicaFactibilidad() {
        return conFactibilidad;
    }

    @Override
    public String getCriterio() {
        return "Busqueda secuencial: pregunto los filtros en un orden fijo declarado "
             + "de antemano, sin evaluar cuanto corta cada uno"
             + (conFactibilidad ? ", pero salteo los que ya no aportan informacion." : ".");
    }

    /**
     * FUNCION DE SELECCION (si se la puede llamar asi).
     *
     * Recorre el orden fijo y devuelve el primer filtro que siga siendo
     * factible. No calcula particiones ni compara nada.
     *
     * Complejidad: Theta(f) con f = cantidad de filtros. Es mas barato por
     * turno que greedy, pero necesita mas turnos, que es lo que importa.
     */
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

        // No deberia llegar aca, pero si el orden fijo quedara incompleto
        // tomamos el primer factible para no romper la partida.
        return factibles.get(0);
    }
}
