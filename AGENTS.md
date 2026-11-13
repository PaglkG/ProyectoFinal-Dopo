# Reglas de trabajo del proyecto

Estas reglas aplican a cualquier trabajo realizado en este repositorio, en cualquier chat o sesión de Codex.

## Fuente de verdad de la lógica

- Antes de cambiar comportamiento, consultar `docs/logica-proyecto.md`.
- No tratar el comportamiento actual del código, nombres de métodos ni pruebas existentes como requisitos confirmados si el documento no lo establece.
- Si falta una regla o hay más de una interpretación posible, preguntar al usuario antes de programar o escribir una prueba que dependa de esa regla.
- Antes de agregar, modificar o eliminar cualquier regla o decisión en `docs/logica-proyecto.md`, pedir aprobación explícita al usuario para el cambio concreto y esperar su respuesta.
- Después de recibir esa aprobación, actualizar `docs/logica-proyecto.md` junto con el cambio de implementación o pruebas correspondiente.
- Mantener separadas en el documento las reglas confirmadas, los detalles de implementación actuales y las preguntas pendientes.

## Correspondencia con diagramas

- No abrir, editar, guardar, renombrar ni eliminar proyectos o archivos de Astah; en particular, no tocar `Head Soccer.asta` ni sus archivos de bloqueo, bajo ninguna circunstancia.
- Cuando el usuario indique que el código debe seguir un diagrama, respetar exactamente las clases, atributos, asociaciones, multiplicidades, operaciones, nombres, orden de parámetros y tipos mostrados.
- No añadir, cambiar ni quitar elementos para completar o reinterpretar un diagrama parcial. Si el diagrama no aclara el alcance o contradice código existente, preguntar antes de modificarlo.

## Pruebas

- Ubicar las pruebas en `src/test/java` y conservar la misma estructura de paquetes que la clase probada. Por ejemplo, `src/main/java/domain/Ball.java` corresponde a `src/test/java/domain/BallTest.java` con el paquete `domain`.
- Nombrar las clases de prueba como `<NombreDeClase>Test` y usar JUnit Jupiter, de acuerdo con la configuración existente del proyecto.
- En este alcance, crear pruebas para dominio y reglas; no probar esqueletos de presentación.
- Limitar cada trabajo de pruebas exactamente a lo que pida el usuario. Si solicita pruebas para un diagrama de secuencia, probar solo el flujo y las interacciones de ese diagrama; si solicita un método específico, probar solo ese método. No ampliar la cobertura a otras clases, métodos o diagramas sin que lo solicite.
- No modificar, eliminar ni debilitar una prueba que ya pasaba sin preguntarle primero al usuario y obtener su autorización.
- Si un cambio hace fallar una prueba que pasaba, detenerse antes de modificar esa prueba. Informar qué falló y preguntar si el usuario quiere que se arregle. Mientras espera, se puede investigar la causa y avanzar en trabajo independiente.
- Probar métodos que ya tengan comportamiento implementado. Cada prueba debe comprobar un resultado o efecto observable relevante; cubrir casos normales, límites y errores aplicables sin duplicar casos equivalentes ni verificar detalles internos salvo que no exista una interfaz observable adecuada.
- Mantener cada prueba independiente, con datos preparados para el caso, una sola acción principal y aserciones que expresen el comportamiento esperado.
- Para excepciones, comprobar el tipo de excepción propia del proyecto y el mensaje/código esperado, además del comportamiento que documenta ese error.
- Antes de definir expectativas para un método o parámetro cuyo propósito, entradas, efectos o retorno no estén claros, preguntarle al usuario.

## Compilación Maven en Codex

- Si Maven falla porque el entorno de Codex no puede abrir o escribir en `C:\Users\pablo\.m2\repository`, no asumir que es un error del proyecto ni cambiar sus pruebas.
- Para verificar el proyecto, usar una copia temporal del repositorio Maven dentro de una ruta permitida y un `settings.xml` temporal que apunte a esa copia.
- Al terminar, eliminar únicamente la copia y los archivos temporales creados para la ejecución. No cambiar `pom.xml`, la configuración global de Maven ni el repositorio `C:\Users\pablo\.m2\repository` para resolver esta restricción del entorno.
- Informar que el error corresponde al entorno aislado cuando la compilación y las pruebas pasen con el repositorio temporal.

## Archivos temporales e informes

- Guardar los archivos temporales creados por el agente durante la ejecución en `tmp/agent-work/`; no mezclarlos con fuentes ni pruebas.
- Al terminar cada tarea, eliminar de esa carpeta los temporales que ya no sean necesarios y conservar la carpeta para ejecuciones futuras. No borrar archivos preexistentes del usuario.
- Conservar únicamente el conjunto de informes Surefire de la ejecución más reciente. El conjunto incluye los XML y TXT que corresponden a esa misma ejecución; eliminar informes de ejecuciones anteriores.
- Conservar únicamente el informe JaCoCo más reciente y su archivo de datos `target/jacoco.exec` correspondiente. Si la ejecución produjo `jacoco.exec` pero no el informe HTML, generarlo con el objetivo `jacoco:report` sin modificar `pom.xml`.
- Limpiar artefactos generados obsoletos que no sean necesarios para esos informes. No eliminar fuentes, pruebas ni archivos del usuario durante esa limpieza.

## Diseño e implementación

- No agregar código a la clase `Game` bajo ninguna circunstancia.
- Mantener el código escalable y consistente con las responsabilidades de las clases.
- Si el diseño de un método depende de una regla de negocio, de otro método pendiente o de una decisión de arquitectura no especificada, preguntar antes de implementarlo.
- No inventar contratos para completar vacíos. Se puede inspeccionar y explicar el estado actual mientras se espera aclaración, sin codificar la suposición.

## Comunicación

- Explicar la clase y el método afectados, la causa de cualquier fallo y la decisión que se necesita del usuario.
- Conservar el idioma y las convenciones de código existentes en el proyecto.
