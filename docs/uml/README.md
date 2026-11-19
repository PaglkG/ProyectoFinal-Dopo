# UML del proyecto

Las fuentes PlantUML y las capturas se conservan para contrastar el diseño recibido con el código. La reorganización del dominio aplanó todos sus paquetes salvo domain.hitbox.

- domain-class-diagram.puml y domain-sequences.puml representan el modelo de referencia recibido, no el inventario vigente del código. Sus diferencias conocidas se enumeran en domain-review.md.
- package-diagram.puml y package-diagram.png son anteriores al aplanado del dominio y no representan los paquetes actuales.
- Las secuencias start-session, player-vs-bot, player-vs-player y by-time-check describen flujos implementados, pero se deben contrastar con el código y la lógica documentada antes de tratarlas como contratos.
- mechanics-test-sequence.puml representa el banco visual a través de la fachada Game.
- capturas conserva las exportaciones y los PDF de referencia originales.

## Lectura del código actual

- Presentación: DOPOSoccerGUI y NavigationController organizan la interfaz Swing; la presentación principal aún no se comunica con el dominio.
- Banco de pruebas: MechanicsTestApp abre MechanicsTestSetupPanel. MechanicsTestController, MechanicsTestPanel y PresentationMovement usan Game y sus instantáneas; el salto se simula en la presentación.
- Partida: Game crea y administra sesiones. GameSession mantiene cancha, participantes, marcador y modalidad; SoccerField resuelve la física, los contactos y los goles.
- Paquetes: las clases de dominio están en domain, salvo las hitboxes en domain.hitbox.
- Modalidad: TypeOfMatch define la comprobación; ByGoals compara el límite de goles y ByTime controla el límite de tiempo y la muerte súbita.

Las reglas acordadas y las discrepancias entre ellas y la implementación están en docs/logica-proyecto.md.
