# Adivina Quién — Programación III

Juego de adivinanzas tipo **"¿Quién es quién?"** hecho en Java para el TPO de Programación III (UADE). Cada participante elige en secreto uno de 23 personajes; en cada turno puede preguntar por una característica (género, calvicie, lentes, color de pelo) para achicar sus candidatos, o arriesgar directamente el nombre del personaje del rival. Si arriesga y se equivoca, pierde la partida.

El proyecto no depende de ninguna librería externa: compila con Java 21 y nada más.

---

## Modos

Los cuatro se pueden correr desde la consola (`JuegoConsola`) o desde el desplegable de la ventana (`VentanaJuego`).

- **Jugador vs Máquina** — la persona contra `MaquinaGreedy`. El tablero se va tachando solo a medida que se descartan candidatos, y hay un botón que muestra qué preguntaría la máquina en tu lugar, con la evaluación completa de cada filtro.
- **Máquina vs Máquina** — `MaquinaGreedy` contra `MaquinaSecuencial`, mostrando turno por turno qué filtros evaluó cada una, cuánto cortaba cada uno, cuál eligió y por qué.
- **Simulación de las 23 partidas** — juega todas las partidas posibles (una por cada personaje secreto) con tres estrategias y compara los resultados.
- **Verificar catálogo** — muestra la carga de los personajes, la traza de la inserción binaria y la verificación de que no haya personajes repetidos.

---

## Los dos algoritmos

En cada turno pasan **dos cosas distintas**, y cada una se resuelve con un patrón diferente:

| Decisión | Patrón | Dónde está |
|---|---|---|
| ¿Cómo achico la lista una vez que me respondieron? | **Divide & Conquer** | `Jugador.descomponer()` / `recibirRespuesta()` |
| ¿Qué conviene preguntar para que esa reducción sea la mayor posible? | **Greedy** | `MaquinaGreedy.elegirFiltro()` |

### Divide & Conquer

Es el mismo problema que el ejemplo del "número secreto" de la cátedra: un espacio de candidatos que se parte con cada respuesta. Los métodos llevan los nombres del esquema visto en clase: `esCasoBase()`, `solucionDirecta()`, `descomponer()`, `combinar()`.

La recursión ocurre a lo largo de los turnos: **23 → 12 → 6 → 3 → 2 → 1**. La clase `BuscadorRecursivo` escribe el mismo algoritmo con la recursión explícita, para poder ver la correspondencia línea por línea con el esquema.

Contra probar personaje por personaje (23 intentos, O(n)), partir el conjunto a la mitad resuelve en **5 preguntas** en el peor caso, O(log n).

### Greedy

Elige el filtro que deja los dos grupos más parejos, o sea el de **menor peor caso** (criterio minimax). Como la máquina no sabe qué le van a responder, no puede optimizar el caso favorable: lo único que puede controlar es qué tan mal le puede ir.

`MaquinaSecuencial` es la máquina de contraste: pregunta en un orden fijo declarado de antemano, sin evaluar cuánto corta cada filtro. Juega peor a propósito, pero **no usa `Random`**: su criterio se explica en una frase.

### El catálogo

2 géneros × 2 (calvo) × 2 (lentes) × 3 colores = **24 combinaciones posibles** para **23 personajes**. El catálogo usa 23 combinaciones distintas, ninguna repetida, así que la máquina siempre termina con un único candidato y nunca tiene que desempatar al azar. El programa lo verifica al arrancar.

---

## Arquitectura

```
com.tpo.adivinaquien
├── modelo/      Personaje, Filtro (+ familia), Particion, EvaluacionFiltro
├── catalogo/    CatalogoPersonajes — carga los 23 con inserción binaria
├── jugador/     Jugador (D&C), JugadorHumano, JugadorMaquina,
│                MaquinaGreedy, MaquinaSecuencial, BuscadorRecursivo
├── juego/       Partida, Oraculo, RegistroRazonamiento (+ implementaciones)
├── vista/       VentanaJuego (Swing), ControladorPartida, RegistroSwing
└── app/         JuegoConsola, SimulacionEstrategias, VerificacionCatalogo
```

Las dos vistas comparten el mismo motor. El motor **nunca llama a `System.out`**: le pasa los mensajes a un `RegistroRazonamiento`, y hay tres implementaciones (consola, Swing, silenciosa). Si se cierra la consola, el juego sigue funcionando en Swing y al revés, sin cambiar una línea del motor.

`Partida` tampoco tiene un bucle adentro: expone a quién le toca, aplicá esta jugada y ¿terminó?, y la vista decide cuándo pedir la próxima. En consola se llama desde un `while`; en Swing, desde el clic de un botón.

La máquina no puede acceder al personaje del rival: solo recibe un `Oraculo` con dos operaciones (preguntar y arriesgar). El personaje secreto vive en un campo privado sin getter, así que ni siquiera por error se puede espiar.

---

## Cómo compilar y correr

```bash
# compilar
javac -d out $(find src -name "*.java")

# interfaz gráfica
java -cp out com.tpo.adivinaquien.vista.VentanaJuego

# consola (menú con los cuatro modos)
java -cp out com.tpo.adivinaquien.app.JuegoConsola
```

También se puede abrir como proyecto de IntelliJ (raíz de fuentes en `src/`, sin Maven ni Gradle).

---

## Documentación

La justificación completa de los patrones aplicados, los **no aplicados** (MergeSort, QuickSort, programación dinámica, búsqueda exhaustiva), el análisis de complejidad y las decisiones de diseño está en:

- [`documentacion/DOCUMENTACION.md`](documentacion/DOCUMENTACION.md)
- [`documentacion/DOCUMENTACION.docx`](documentacion/DOCUMENTACION.docx)

Incluye una medición experimental sobre las 23 partidas posibles que compara tres estrategias y descompone cuánto aporta cada elemento del esquema greedy por separado.
