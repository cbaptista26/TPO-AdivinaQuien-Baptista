package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.CatalogoFiltros;
import com.tpo.adivinaquien.modelo.Filtro;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de las maquinas. Aporta lo unico que un humano no necesita: decidir sola
 * que jugar en su turno.
 *
 * El Divide and Conquer (descomponer / combinar / caso base) esta en Jugador,
 * la clase padre, porque el tablero del humano se achica con el mismo
 * algoritmo. Lo que se agrega aca es la eleccion de la pregunta, que es donde
 * cada maquina aplica su propio criterio.
 */
public abstract class JugadorMaquina extends Jugador {

    protected JugadorMaquina(String nombre, RegistroRazonamiento registro) {
        super(nombre, registro);
    }

    @Override
    public boolean esMaquina() {
        return true;
    }

    // ==================================================================
    // FILTROS DISPONIBLES
    // ==================================================================

    /**
     * Los filtros que todavia se pueden preguntar.
     *
     * Descarta dos cosas:
     *   1. los que esta maquina ya pregunto (no aportarian nada nuevo); y
     *   2. los que sobre los candidatos actuales dejan un lado vacio, o sea que
     *      la respuesta ya se conoce de antemano. Ejemplo: si ya sabemos que
     *      todos los candidatos que quedan tienen el pelo amarillo, preguntar
     *      "tiene el pelo amarillo?" gasta un turno sin descartar a nadie.
     *      Esta es la FUNCION DE FACTIBILIDAD del esquema greedy.
     */
    protected List<Filtro> filtrosFactibles() {
        List<Filtro> factibles = new ArrayList<>();

        for (Filtro f : CatalogoFiltros.todos()) {
            if (filtrosUsados.contains(f.getClave())) {
                continue;
            }
            if (aplicaFactibilidad() && descomponer(candidatos, f).esInutil()) {
                continue;
            }
            factibles.add(f);
        }
        return factibles;
    }

    /**
     * Si esta maquina aplica o no la funcion de factibilidad.
     *
     * La factibilidad es UNO DE LOS CINCO ELEMENTOS del esquema greedy, no una
     * optimizacion generica: requiere evaluar la particion de cada filtro
     * contra los candidatos actuales, que es justamente el trabajo que una
     * busqueda ingenua no hace. Por eso una maquina puramente secuencial la
     * tiene desactivada.
     *
     * Tenerlo como metodo redefinible nos permite medir por separado cuanto
     * aporta cada elemento del greedy. Ver SimulacionEstrategias.
     */
    protected boolean aplicaFactibilidad() {
        return true;
    }

    // ==================================================================
    // DECISION DEL TURNO
    // ==================================================================

    /**
     * Que hace la maquina en su turno.
     *
     * La estructura es siempre la misma (es el esquema D&C); lo que cambia
     * entre MaquinaGreedy y MaquinaSecuencial es UNICAMENTE como se elige el
     * filtro. Por eso ese paso queda abstracto.
     */
    public Jugada jugarTurno() {

        // CasoBase(x) -> SolucionDirecta(x)
        if (esCasoBase()) {
            registro.registrar(String.format(
                    "    [%s] CASO BASE: queda 1 candidato -> lanzo la suposicion.", getNombre()));
            return solucionDirecta();
        }

        List<Filtro> factibles = filtrosFactibles();

        // Sin filtros utiles no hay forma de seguir achicando: se arriesga.
        if (factibles.isEmpty()) {
            registro.registrar(String.format(
                    "    [%s] No quedan filtros que aporten informacion -> arriesgo.", getNombre()));
            return new Suposicion(candidatos.get(0));
        }

        return new PreguntaFiltro(elegirFiltro(factibles));
    }

    /**
     * El criterio propio de cada maquina para elegir que preguntar.
     * Es el unico punto donde las dos estrategias se diferencian.
     */
    protected abstract Filtro elegirFiltro(List<Filtro> factibles);
}
