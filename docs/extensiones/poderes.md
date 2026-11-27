# Agregar un poder y conectarlo de extremo a extremo

## Estado actual y registro

`Power` administra la recarga. `GameSession.updateGame()` la avanza y `goal(String)` concede el bono al lado que recibió el gol. `Power.usePower()` consume la carga disponible. `Game.getPlayerPowerRemainingSeconds()` alimenta la captura y la barra. Las rutas `Game.powerPlayer(...)`, `GameSession.powerPlayer(...)` y `SessionParticipant.power()` son puntos de extensión pendientes. Registrar una clase permite asignarla, pero todavía hace falta conectar activación y efectos.

1. Crea una subclase concreta de `Power` con constructor público sin argumentos y registra su nombre en [powers.txt](../../src/main/resources/domain/powers.txt). Vincúlala al personaje mediante `character.<código>.power` en `options.properties`.
2. Usa `PowerFactory.create()` para comprobar la creación. Sigue la asignación en el constructor de la sesión y `SessionParticipant.setSpecialPower()`. Conserva la recarga compartida y documenta en la clase qué efecto específico corresponde al poder.

## Ejemplo propuesto: potenciar un tiro al balón

| Punto del flujo | Método que debes revisar | Responsabilidad propuesta |
| --- | --- | --- |
| Entrada | Controlador del banco o de la cancha y fachada `Game` | Enviar una solicitud por identidad. La cancha oficial aún no tiene teclas de juego conectadas. El método actual de poder recibe `SessionParticipant`, así que usarlo directamente desde presentación incumpliría la separación. Una operación de fachada por identidad requiere autorización explícita. |
| Activación | `GameSession.powerPlayer()`, `SessionParticipant.power()`, `Power.isUsable()` y `usePower()` | Validar sesión, participante, estado y carga. Armar el efecto disponible una vez, consumir la recarga y conservar el estado necesario hasta el contacto confirmado. |
| Contacto | `GameSession.kickPlayer()` y `isBallInKickReach()` | Separar la patada normal de la ejecución del tiro de poder. La patada normal actualmente prioriza al rival. Acordar qué sucede si el rival y el balón están al alcance al mismo tiempo. Para el ejemplo, aplicar el efecto al balón cuando se confirma su contacto. |
| Impulso | `Ball.applyImpulse(deltaX, deltaY)` | Elegir el signo horizontal según el lado del atacante. El método suma velocidades. Si la regla pide una velocidad final fija, calcula la diferencia respecto de los getters de velocidad. Confirma magnitud, dirección vertical y multiplicador antes de introducir valores. |
| Trayectoria | `GameSession.updateGame()`, `collision()`, resolutores de cancha y balón | Dejar que el balón avance con gravedad, rebote con participantes y límites y pase por la comprobación de gol. Un efecto que solo impulsa al balón evita una llamada directa a `takeDamage()` o `applyStatusEffect()` en el momento del tiro. |
| Daño posterior, si se requiere | Resolución balón-participante y `SessionParticipant.takeDamage()` / `applyStatusEffect()` | Acordar si un impacto posterior causa daño. Si lo hace, el balón necesita identificar el tiro y su origen. Aplicar el efecto en el contacto confirmado y consumir esa marca para evitar repeticiones. |
| Estado visible | `DOPOSoccerGUI.readParticipant()`, `publishMatch()`, `MatchSnapshot.Participant`, `MatchPanel.showParticipantStatus()` | La barra existente representa recarga. Para mostrar aura o tiro activo, publica ese estado mediante un dato explícito y dibújalo en el panel. |

Traza la ruta completa en este orden:

```text
solicitud de activación → validar y armar el poder → contacto con el balón
→ aplicar impulso una vez → avance normal del balón → publicar y dibujar la captura
```

La regla vigente consume la carga al activar y ejecuta el tiro al tocar el balón. Para este ejemplo, confirma qué contactos cuentan y qué ocurre al reiniciar. Para poderes con estado transitorio, revisa `GameSession.restartRound()`, el reinicio del balón y la serialización. En las pruebas comprueba carga incompleta, activación válida, contacto con balón, rival cercano, cada sentido del impulso, gol y reinicio. Si el ejemplo se aprueba como un tiro que solo afecta al balón, verifica que la vida del rival no cambie al ejecutarlo.
