# Agregar un personaje o un tipo de jugador

## Personaje con un poder fijo

1. Si el personaje requiere un poder nuevo, implementa una clase pública concreta que extienda `Power`, con constructor público sin argumentos. Registra el nombre completo en [powers.txt](../../src/main/resources/domain/powers.txt). Para implementar efectos, sigue [Poderes y colisiones](poderes.md).
2. En [options.properties](../../src/main/resources/presentation/options.properties), agrega el código a `characters` y crea el bloque `character.<código>`. Usa `country`, `animal`, `head`, `preview`, `body`, `flag`, `winner`, `power`, `powerName` y `description`. Si la pata original mira a la derecha, configura `bodyFacesRight=true`. Copia un bloque existente para respetar el formato y coloca cada imagen en la carpeta de otros recursos del mismo tipo.
3. Sigue `PersonalizationPanel.prepareViewPlayers()`. Filtra perfiles con el catálogo de poderes publicado. `updateCountry()` actualiza la vista previa. `createMatchConfiguration()` toma el poder fijo de cada perfil y envía el nombre de clase.
4. Sigue `Game.startSession()` hasta el constructor de `PlayerVsPlayer` o `PlayerVsBot`. La sesión crea al participante, resuelve el poder con `PowerFactory.create()` y llama a `setSpecialPower()`. En manual se reciben los poderes derecho e izquierdo. Contra bot, se reciben poder del humano, poder del bot y perfil de bot, en ese orden.
5. Comprueba el país en `PreparationPanel.refreshFromState()`, las imágenes en `MatchPanel.showMatch()` y los nombres y banderas en `ResultsPanel.setMatchConfiguration()`.

## Cuándo hace falta una clase Java de jugador

Los personajes actuales comparten `Player`. Si el cambio es el poder fijo, basta seguir el flujo anterior. Si requiere comportamiento humano propio, define primero qué acción cambia y crea una subclase de `Player` que conserve la identidad del jugador. Cambia el punto de construcción en el constructor de la sesión correspondiente. El formulario actual transmite poderes, países e imágenes. Una selección de clase de jugador requeriría un contrato nuevo para transportar esa elección hasta el dominio.

Revisa `Game.findPlayer()` para mantener la búsqueda por identidad, y `GameSession.movePlayer()`, `jumpPlayer()` y `kickPlayer()` según la acción afectada. Comprueba inicio, ambos lados, reinicio y carga de una partida si el tipo agrega estado serializable.
