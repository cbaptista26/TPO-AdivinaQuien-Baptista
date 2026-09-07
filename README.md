# Adivina Quién - Lógica Algorítmica

Este proyecto es una implementación en Java del clásico juego "Adivina Quién", desarrollado como Trabajo Práctico Obligatorio (TPO) para la asignatura Diseño y Análisis de Algoritmos. El enfoque principal del proyecto no es únicamente la jugabilidad, sino la aplicación, demostración y justificación formal de patrones de **Lógica Algorítmica**.

## 🧠 Patrones Algorítmicos Aplicados

El motor del juego está diseñado para separar la toma de decisiones del procesamiento de datos, garantizando la trazabilidad algorítmica exigida por la cátedra:

- **Greedy (Algoritmo Voraz):** 
  - Utilizado por la máquina para seleccionar la mejor pregunta en cada turno. Aplica una función de selección *minimax* (minimiza el tamaño del peor caso posible) y una función de factibilidad que descarta filtros lógicamente redundantes, optimizando la reducción del espacio de búsqueda.
- **Divide & Conquer (Divide y Conquista):** 
  - **Filtrado:** Utilizado para reducir el conjunto de candidatos tras recibir una respuesta, descartando las ramas inválidas en tiempo $\Theta(\log n)$ en un árbol de decisión ideal.
  - **Ordenamiento:** Implementado en la carga inicial del catálogo mediante **MergeSort** $\Theta(n \log n)$, y en la inserción de nuevos personajes mediante **Búsqueda Binaria** $\Theta(\log n)$.

## 🏗️ Arquitectura y Vistas (Separación de Responsabilidades)

El proyecto separa estrictamente el motor lógico de la presentación mediante la interfaz `RegistroRazonamiento` y el patrón *Observer*.
- **Vista de Consola:** Orientada a la auditoría algorítmica. Expone paso a paso el razonamiento interno de la máquina (criterios de partición, evaluación minimax y porcentajes de descarte), demostrando que las decisiones se toman por un criterio formal y no al azar.
- **Vista Gráfica (Swing):** Orientada a la experiencia de usuario. Implementada con una arquitectura basada en estados para no bloquear el hilo de la interfaz (EDT), permitiendo un juego fluido.

## 🚀 Modos de Ejecución (Paquete `app`)

El proyecto incluye cuatro puntos de entrada independientes:
1. **JuegoConsola:** Partida interactiva jugable desde la terminal con traza de razonamiento detallada.
2. **VentanaJuego:** Interfaz gráfica (Swing) para jugar contra la máquina.
3. **SimulacionEstrategias:** Modo *Máquina vs Máquina* que enfrenta distintas estrategias (Greedy vs. Secuencial) para medir empíricamente su eficacia.
4. **VerificacionCatalogo / BenchmarkOrdenamiento:** Pruebas de correctitud de datos y comparativas de rendimiento (MergeSort vs algoritmos cuadráticos) para justificar las decisiones de ordenamiento.

## 📋 Reglas y Entidades
- **Catálogo:** 23 personajes con características y combinaciones únicas.
- **Filtros de búsqueda:** Género, Calvicie, Uso de Lentes, y 3 variables excluyentes de Color de Pelo.
- **Restricción de Información:** La máquina accede a las respuestas a través de una interfaz estricta (`Oraculo`), imposibilitando el acceso directo a la variable seleccionada por el jugador.

## 🛠️ Tecnologías
- Java 21 (Microsoft OpenJDK)
- Java Swing (UI)

---
*Desarrollado por Candela Baptista.*
