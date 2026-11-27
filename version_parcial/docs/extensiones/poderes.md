# Agregar un poder y conectarlo de extremo a extremo

## Estado actual y registro

`Power` administra la recarga. `GameSession.updateGame()` la avanza y `goal(String)` concede el bono al lado que recibió el gol. `Game.powerPlayer(boolean)` recibe la solicitud por identidad y `GameSession.powerPlayer()` valida el participante. `SessionParticipant.power()` consume una carga completa y deja el poder armado hasta que el participante toca el balón. El resolutor recibe las listas de participantes izquierdos y derechos para orientar el tiro y aplicar el impacto al rival una sola vez. La captura expone el estado armado para dibujar el aura.

1. Crea una subclase concreta de `Power` con constructor público sin argumentos y registra su nombre en [powers.txt](../../src/main/resources/domain/powers.txt). Vincúlala al personaje mediante `character.<código>.power` en `options.properties`.
2. Usa `PowerFactory.create()` para comprobar la creación. Sigue la asignación en el constructor de la sesión y `SessionParticipant.setSpecialPower()`. Conserva la recarga compartida y documenta en la clase qué efecto específico corresponde al poder.

## Flujo implementado: activar y ejecutar un poder

| Punto del flujo | Método que debes revisar | Responsabilidad propuesta |
| --- | --- | --- |
| Entrada | `ConfigurationPanel`, `DOPOSoccerGUI` y `Game.powerPlayer(boolean)` | Usar los códigos de tecla asignados en presentación y enviar una solicitud por identidad. La presentación no recibe ni entrega objetos `SessionParticipant`. |
| Activación | `GameSession.powerPlayer()` y `SessionParticipant.power()` | Ignorar participantes ajenos, sesiones terminadas, participantes bloqueados y cargas incompletas. Armar una sola vez el poder cargado y consumir su recarga. |
| Contacto | `BallParticipantCollisionResolver.resolveAll()` | Procesar la colisión del participante y el balón. Solo si el participante había solicitado el poder se inicia la trayectoria; el booleano de lado proviene de la lista izquierda/derecha recibida por el resolutor. |
| Impacto | `BallParticipantCollisionResolver.resolveForSide()` y `Ball` | El balón conserva el poder y el lado atacante. El resolutor compara ese lado con el participante contactado, aplica el efecto solo al rival y retira la atribución después del primer impacto. |
| Estado visible | `Game.isPlayerPowerActive()`, `MatchSnapshot.Participant` y `MatchPanel.drawChargedPower()` | Publicar el estado armado y dibujar el aura hasta que el tiro se inicie. La barra continúa mostrando la recarga. |

Traza la ruta completa en este orden:

```text
tecla asignada → Game.powerPlayer(identidad) → validar y armar el poder
→ contacto con el balón → iniciar trayectoria según lado → impacto rival una vez
→ publicar estado y dibujar el aura mientras sigue armado
```

Los controles se consultan al entrar en la cancha; la pantalla de configuración todavía no persiste las asignaciones entre ejecuciones. `GameSession.restartRound()` limpia el estado armado del participante y `Ball.resetBall()` descarta la atribución de un tiro anterior.
