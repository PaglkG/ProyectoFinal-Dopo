# Reglas de trabajo del proyecto

Estas reglas aplican a cualquier trabajo realizado en este repositorio, en cualquier chat o sesión de Codex.

## Fuente de verdad de la lógica

- Antes de cambiar comportamiento, consultar `docs/logica-proyecto.md`.
- No tratar el comportamiento actual del código, nombres de métodos ni pruebas existentes como requisitos confirmados si el documento no lo establece.
- Si falta una regla o hay más de una interpretación posible, preguntar al usuario antes de programar o escribir una prueba que dependa de esa regla.
- Antes de agregar, modificar o eliminar cualquier regla o decisión en `docs/logica-proyecto.md`, pedir aprobación explícita al usuario para el cambio concreto y esperar su respuesta.
- Después de recibir esa aprobación, actualizar `docs/logica-proyecto.md` junto con el cambio de implementación o pruebas correspondiente.
- Mantener separadas en el documento las reglas confirmadas, los detalles de implementación actuales y las preguntas pendientes.

## Pruebas

- No modificar, eliminar ni debilitar una prueba que ya pasaba sin preguntarle primero al usuario y obtener su autorización.
- Si un cambio hace fallar una prueba que pasaba, detenerse antes de modificar esa prueba. Informar qué falló y preguntar si el usuario quiere que se arregle. Mientras espera, se puede investigar la causa y avanzar en trabajo independiente.
- Las pruebas nuevas deben comprobar comportamientos observables y significativos, cubrir casos relevantes y corresponder a reglas confirmadas. No deben limitarse a repetir la implementación ni a codificar comportamientos accidentales.
- Antes de definir expectativas para un método o parámetro cuyo propósito, entradas, efectos o retorno no estén claros, preguntarle al usuario.

## Diseño e implementación

- Mantener el código escalable y consistente con las responsabilidades de las clases.
- Si el diseño de un método depende de una regla de negocio, de otro método pendiente o de una decisión de arquitectura no especificada, preguntar antes de implementarlo.
- No inventar contratos para completar vacíos. Se puede inspeccionar y explicar el estado actual mientras se espera aclaración, sin codificar la suposición.

## Comunicación

- Explicar la clase y el método afectados, la causa de cualquier fallo y la decisión que se necesita del usuario.
- Conservar el idioma y las convenciones de código existentes en el proyecto.
