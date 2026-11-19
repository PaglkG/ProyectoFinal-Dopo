# ⚽ Head Soccer

<div align="center">

### Proyecto universitario · Escuela Colombiana de Ingeniería Julio Garavito

*Un videojuego de fútbol 1 vs 1 con cabezones, desarrollado en Java.*

[![Compilación](https://github.com/PaglkG/ProyectoFinal-Dopo/actions/workflows/build.yml/badge.svg)](https://github.com/PaglkG/ProyectoFinal-Dopo/actions/workflows/build.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-compilación-C71A36?logo=apachemaven&logoColor=white)
![JUnit Jupiter](https://img.shields.io/badge/JUnit%20Jupiter-6.1.3-25A162?logo=junit5&logoColor=white)
![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)
![Licencia](https://img.shields.io/badge/uso-acad%C3%A9mico-blue)

</div>

---

## 📖 Sobre el proyecto

Este repositorio contiene el **proyecto final** de la asignatura, desarrollado en la **Escuela Colombiana de Ingeniería Julio Garavito**, como parte del programa de **Ingeniería de Sistemas**.

Es un trabajo **académico y sin fines comerciales**. Su objetivo es aplicar análisis de requisitos, diseño orientado a objetos, pruebas automáticas y trabajo colaborativo con Git.

El proyecto desarrolla una versión académica inspirada en **Head Soccer**, seleccionada en la convocatoria *Juego 2026-2*. Actualmente contamos con mecánicas básicas de dominio y una pantalla independiente para probarlas; la presentación oficial sigue en desarrollo.

> ⚠️ **Aviso:** *Head Soccer* es un juego creado por **D&D Dream Corp.** Este proyecto es una reinterpretación educativa; no está afiliado ni respaldado por sus creadores y no se distribuye con fines comerciales.

## 📂 Documentación y recursos

Los enlaces llevan a los documentos compartidos, los diagramas y las guías del repositorio.

| Recurso | Enlace | Descripción |
|---|---|---|
| 📝 Documento colaborativo | [Abrir en Google Docs](https://docs.google.com/document/d/1i9cOyOaORip1lEBSSzXO6fPmAldYHS1KjRxTYh8H3WA/edit?usp=sharing) | Documento compartido del proyecto. |
| 📄 Archivo complementario | [Abrir en Google Drive](https://drive.google.com/file/d/1dhQskSDOwRWgXCxy5vZDP-IV2ra9s9VO/view?usp=sharing) | Archivo compartido por el equipo. |
| 📐 Diagrama de clases | [Abrir en diagrams.net](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&link-icons=1&tooltip-icons=1&nav=1&title=Head_Soccer.drawio&dark=auto#Uhttps%3A%2F%2Fdrive.google.com%2Fuc%3Fid%3D1kuQxyHiroSopLOlZwFoC23GFreDoKCrz%26export%3Ddownload) | Visualización y edición del diagrama de clases. |
| 💻 Código fuente | [Repositorio GitHub](https://github.com/PaglkG/ProyectoFinal-Dopo) | Código, versiones, ramas y seguimiento del desarrollo. |
| 📘 Reglas del proyecto | [`docs/logica-proyecto.md`](docs/logica-proyecto.md) | Reglas confirmadas, estado de implementación y decisiones pendientes. |
| 📚 Referencia de clases | [`docs/referencia-clases.md`](docs/referencia-clases.md) | Responsabilidad, atributos y métodos de cada tipo declarado en `src/main/java`. |
| 🧪 Banco de pruebas | [`docs/guia-banco-pruebas.md`](docs/guia-banco-pruebas.md) | Instrucciones para abrir y usar la ventana de prueba de mecánicas. |
| 📐 Diagramas UML | [`docs/uml/`](docs/uml/) | Diagramas fuente, capturas e informe de contraste con el código. |

> El acceso a los archivos de Google depende de los permisos compartidos para cada integrante.

## 🎮 Referente: Head Soccer

*Head Soccer* es un juego de fútbol arcade, **uno contra uno**, donde los jugadores son personajes de cabeza enorme que golpean el balón con la cabeza y los pies.

Fue lanzado por D&D Dream Corp. en 2012 para dispositivos móviles. Sus desarrolladores reportan más de 100 millones de descargas.

La idea es simple y se aprende en segundos:

- 🥅 **Gana quien marque más goles** antes de que se acabe el tiempo del partido.
- 🏃 Te mueves de lado a lado, **saltas** para cabecear y **pateas** para disparar.
- 💥 Puedes ejecutar **tiros especiales**, por ejemplo, de fuego, hielo o rayo.
- 🧠 Si vas arriba en el marcador, defender bien hasta el final también es una estrategia.
- ⚙️ Utiliza **física** para el balón y los rebotes, haciendo que cada partido sea impredecible.

### Elementos característicos del juego original

| Elemento | Descripción |
|---|---|
| 🧑‍🎤 **Personajes** | Decenas de avatares, cada uno con su propio tiro especial. |
| 🏆 **Modos de juego** | Arcade, Torneo, Supervivencia, Liga, Copa y otros modos. |
| 🌍 **Multijugador** | Partidas contra amigos o jugadores en línea. |
| 🐾 **Personalización** | Sistema de mascotas y accesorios para los personajes. |
| 🎯 **Controles** | Mínimos: moverse, saltar y patear. |

> Las versiones web del juego se controlan con las **flechas del teclado** para moverse y la **barra espaciadora** para saltar.

## 🧩 Estado actual del proyecto

El proyecto está en desarrollo. La capa de dominio ya contiene sesiones para jugador contra jugador y jugador contra bot, modalidades por goles y por tiempo, hitboxes, actualización física del balón, colisiones, rebotes, goles, marcador y finalización de la partida. La pantalla independiente de pruebas permite simular estas mecánicas y preparar escenarios para observarlas.

La presentación oficial tiene pantallas y componentes Swing en construcción, pero todavía no integra una partida completa con todas las mecánicas. El movimiento de los jugadores de la pantalla de prueba se simula en presentación para poder ensayar el dominio; no corresponde a una implementación definitiva de controles del juego. La modalidad contra bot está creada, pero la toma de decisiones del bot, los poderes y sus efectos siguen pendientes. Algunos casos límite de colisiones también requieren revisión.

## 📝 Retrospectiva de la entrega

En esta entrega nos enfocamos principalmente en la capa de dominio. El proyecto ya implementa funcionalidades básicas, en particular las hitboxes y varias interacciones entre los objetos. Aun así, hemos encontrado fallos en algunas colisiones, sobre todo cuando los dos jugadores se superponen con el balón. Por eso consideramos que esta parte funciona como una base, pero todavía necesita revisión y correcciones.

Uno de los avances principales fue ampliar el paquete `hitbox`. Después de discutir varias alternativas entre el equipo y la IA, elegimos centralizar la detección geométrica en una clase y crear otra clase especial para indicar si ocurrió una intersección y, cuando ocurre, identificar el objeto y la hitbox que participaron. Esta información nos permite controlar después la respuesta correspondiente. Al principio pensamos en calcular las colisiones directamente mediante funciones distribuidas por el código; la retroalimentación de la IA nos ayudó a considerar que repetir esos cálculos en cada actualización podía ser costoso y difícil de mantener. Separar esta responsabilidad nos pareció una alternativa más ordenada y escalable.

La implementación de la física fue especialmente difícil. La sintaxis de las operaciones y el razonamiento matemático nos tomaron bastante tiempo. Tuvimos que representar el movimiento del balón mediante componentes horizontales y verticales para poder aplicar los rebotes y los golpes, y decidir cómo debían cambiar esas velocidades en cada contacto. También fue necesario organizar cuidadosamente la secuencia de cada actualización: por ejemplo, comprobar primero si el balón entró en el arco antes de tratarlo como un objeto que salió de la cancha. La lógica es secuencial y cambiar el orden de las comprobaciones puede cambiar el resultado del partido.

La interacción entre el jugador y el balón también requirió varias decisiones. Implementamos que el jugador pueda ejercer un impulso sobre el balón y revisamos la fuerza y las proporciones mediante simulaciones. Como algunos métodos de física quedaron enrevesados, pedimos a la IA que nos ayudara a dividir el código en métodos privados y a documentar sus pasos, incluso cuando esos métodos no forman parte de la interfaz pública. Queremos que esa documentación nos sirva para estudiar, preparar el parcial y recordar qué está haciendo cada cálculo cuando tengamos que depurar el juego.

También incorporamos algunas excepciones para representar situaciones problemáticas, como objetos fuera de la partida o jugadores superpuestos. Para decidir qué casos debían producir errores, revisamos escenarios posibles con ayuda de la IA. Sin embargo, la física es todavía una de las áreas con más posibilidades de presentar casos límite, así que reconocemos que faltan excepciones y validaciones por revisar.

La capa de presentación oficial ha avanzado en su diseño, pero todavía no está terminada. En esta etapa priorizamos el dominio y elaboramos diseños para la presentación. Para poder comprobar las mecánicas sin esperar a que la interfaz definitiva estuviera lista, le pedimos a la IA una capa de presentación independiente para pruebas. Con esa pantalla pudimos observar el comportamiento del balón y los jugadores, experimentar con la fuerza de los golpes y revisar tamaños y proporciones. Esa posibilidad de ver las interacciones nos permitió hacer correcciones que habrían sido más difíciles de encontrar leyendo únicamente el código.

Además, revisamos métodos que no debían estar en la clase principal del dominio y seguimos analizando cómo separar mejor las responsabilidades. Exploramos también cómo podría configurarse el teclado para el juego, aunque esa configuración aún no está integrada como una funcionalidad completa. Hasta el momento tampoco hemos programado la modalidad contra la IA ni los poderes; por ahora, el trabajo se ha concentrado en las funcionalidades básicas y en la modalidad entre jugadores.

En cuanto a las pruebas, realizamos pruebas de precodificación y de postcodificación. Las primeras se plantearon a partir de los diagramas de secuencia para anticipar el flujo esperado. Las segundas verifican el comportamiento de los métodos una vez implementados y buscan que cambios posteriores no dañen lo que ya consideramos funcional. La IA nos ayudó a preparar casos y a revisar que no se nos escaparan interacciones importantes. Durante el desarrollo encontramos errores y cambiamos algunas reglas, especialmente en el paquete `hitbox`; por esa razón también tuvimos que ajustar ciertas pruebas que ya existían. Esto fue necesario porque había cambiado la lógica central de interacción entre los objetos. En la última ejecución de Maven pasaron las 140 pruebas. Esperamos que, una vez estabilicemos esta lógica gruesa del dominio, ya no tengamos que modificar con tanta frecuencia las pruebas anteriores.

También reconocemos que todavía hay diferencias o elementos faltantes en algunos diagramas de secuencia. Debemos seguir contrastándolos con el comportamiento implementado y actualizarlos cuando sea necesario, para que el UML describa de forma fiel los flujos del proyecto.

En conclusión, esta entrega nos permitió pasar de tener principalmente estructuras iniciales a contar con una base que ya simula interacciones importantes entre objetos. El proceso nos exigió aprender y revisar conceptos de geometría, vectores, orden de actualización, pruebas y diseño por responsabilidades. Nos apoyamos en la IA para discutir alternativas, detectar escenarios, documentar métodos y construir la pantalla de prueba, pero las decisiones y revisiones del proyecto las hicimos en equipo. El estado actual todavía no es definitivo: quedan correcciones de colisiones y física, excepciones por revisar, diagramas por completar o sincronizar, la presentación oficial, la modalidad contra la IA, los poderes y la configuración de controles. Esta base nos permite continuar esos pasos con mecánicas que ya podemos observar y probar.

## 🧰 Tecnologías

| Herramienta | Uso |
|---|---|
| ☕ **Java 21** | Lenguaje principal. |
| 📦 **Maven** | Gestión de dependencias y compilación. |
| 🧪 **JUnit Jupiter 6.1.3** | Dependencia configurada para las pruebas unitarias. |
| 🤖 **GitHub Actions** | Integración continua (`build-and-test`). |
| 🔀 **Git / GitHub** | Control de versiones y revisión de código. |

## 🚀 Cómo compilar, probar y ejecutar

### Requisitos

- JDK 21.
- Maven 3.9 o superior.
- Git.

### Compilar y ejecutar pruebas

```bash
# 1. Clonar el repositorio
git clone https://github.com/PaglkG/ProyectoFinal-Dopo.git

# 2. Entrar al directorio
cd ProyectoFinal-Dopo

# 3. Compilar y ejecutar todas las pruebas
mvn --batch-mode --no-transfer-progress clean verify
```

### Abrir la aplicación

```bash
mvn exec:java
```

Esto inicia el punto de entrada configurado (`presentation.App`). La interfaz oficial está en desarrollo y todavía no ofrece una partida completa.

### Abrir el banco visual de mecánicas

Desde el IDE, ejecuta la clase `presentation.testing.MechanicsTestApp`. Esta ventana es independiente de la aplicación principal. Permite probar sesiones, movimiento visual, patadas y escenarios de colisión; consulta [`docs/guia-banco-pruebas.md`](docs/guia-banco-pruebas.md) para los controles y escenarios disponibles.

## 🌿 Flujo de trabajo

Trabajamos con ramas protegidas y revisión cruzada entre integrantes para mantener un desarrollo organizado y evitar que cambios incompletos lleguen a la versión principal.

### Estructura de ramas

```mermaid
gitGraph
    commit id: "inicio"
    branch develop
    checkout develop
    branch pablo
    branch samuel
    checkout pablo
    commit id: "funcionalidad Pablo"
    checkout develop
    merge pablo id: "solicitud + revisión"
    checkout samuel
    commit id: "funcionalidad Samuel"
    checkout develop
    merge samuel id: "solicitud + revisión"
    checkout main
    merge develop id: "entrega"
```

| Rama | Propósito | Reglas |
|---|---|---|
| `main` | Versiones estables, listas para entregar. | Pull Request, 1 aprobación y CI obligatoria. |
| `develop` | Integración del trabajo del equipo. | Pull Request, 1 aprobación y CI obligatoria. |
| `pablo` | Rama personal de desarrollo. | Sin borrado ni *force push*. |
| `samuel` | Rama personal de desarrollo. | Sin borrado ni *force push*. |

### Reglas de colaboración

1. Cada integrante desarrolla sus funcionalidades en su propia rama.
2. Los cambios se envían mediante Pull Requests.
3. El otro integrante revisa el código antes de aprobarlo.
4. Los Pull Requests deben superar las pruebas automáticas de GitHub Actions.
5. Los cambios se integran mediante **Merge Commit**.
6. La rama `main` debe contener únicamente versiones estables del proyecto.

## 👥 Equipo de desarrollo

| Integrante |
|---|
| Pablo Andres Gualdron Lindo |
| Samuel Ahumada Florez |

## 🎓 Contexto académico

- **Institución:** Escuela Colombiana de Ingeniería Julio Garavito.
- **Programa:** Ingeniería de Sistemas.
- **Tipo de trabajo:** Proyecto final universitario.
- **Juego asignado:** Head Soccer.
- **Periodo:** 2026-2.

## 📚 Créditos y referencias

- *Head Soccer* © **D&D Dream Corp.**, juego original que inspira este proyecto.
- Convocatoria *Juego 2026-2*, Escuela Colombiana de Ingeniería Julio Garavito.

---

<div align="center">

**Hecho con ☕ y mucho código en Bogotá, Colombia 🇨🇴**

*Proyecto académico · Ingeniería de Sistemas · 2026-2*

</div>
