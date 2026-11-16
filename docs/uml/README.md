# UML del proyecto

Los diagramas fuente de este directorio están escritos en PlantUML (`.puml`) para que se puedan revisar y versionar junto al código. `domain-class-diagram.puml` describe el código actual; `domain-sequences.puml` transcribe los flujos de dominio planteados en los PDF y señala cuándo un flujo aún no está implementado. La secuencia `updateGame` describe ahora la lógica implementada de actualización, contactos, gol y fin de partida.

- `domain-class-diagram.puml`: clases principales del dominio, relaciones y puerto del controlador.
- `start-session-sequence.puml`: flujo implementado de creación reflectiva de una sesión.
- `player-vs-bot-sequence.puml`: inicialización implementada de participantes y lados.
- `by-time-check-sequence.puml`: comprobación implementada del límite de tiempo y muerte súbita.
- `domain-sequences.puml`: secuencias del modelo compartidas con los PDF de referencia.
- `domain-review.md`: contraste entre UML, reglas documentadas y código.
- `package-diagram.puml`: dependencias de alto nivel entre presentación y dominio.
- `player-vs-player-sequence.puml`: inicialización actual de dos jugadores humanos.
- `presentation.testing`: aplicación independiente de banco de pruebas visual para las mecánicas del dominio.
- `mechanics-test-sequence.puml`: flujo del banco visual para preparar escenarios, mover participantes, patear el balón y reflejar los goles del dominio.
- `capturas/`: capturas legibles correspondientes a los diagramas cuando se exporten.

Los métodos vacíos no se presentan como comportamientos ya realizados. Las capturas PDF recibidas se conservan en `capturas/`; las páginas vacías se anotan en el informe. Las imágenes PNG de los diagramas PlantUML se regeneran en `capturas/` desde sus fuentes.
