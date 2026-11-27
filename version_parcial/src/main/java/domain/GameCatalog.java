package domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/** Obtiene los catálogos del dominio por registro o por descubrimiento de clases. */
final class GameCatalog {
    /** Evita instanciar el lector del catálogo. */
    private GameCatalog() { }

    /**
     * Lista las subclases concretas del paquete del dominio en el directorio
     * compilado o en el JAR del juego, sin inicializarlas.
     */
    static List<String> discover(Class<?> type, boolean publicConstructor, Class<?>... parameters) {
        try {
            Path location = Path.of(GameCatalog.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            String prefix = GameCatalog.class.getPackageName().replace('.', '/') + "/";
            List<String> entries;
            if (Files.isDirectory(location)) {
                try (var files = Files.list(location.resolve(prefix))) {
                    entries = files.map(file -> file.getFileName().toString()).toList();
                }
            } else {
                try (JarFile jar = new JarFile(location.toFile())) {
                    entries = jar.stream().map(entry -> entry.getName())
                            .filter(name -> name.startsWith(prefix))
                            .map(name -> name.substring(prefix.length()))
                            .toList();
                }
            }
            return entries.stream()
                    .filter(name -> name.endsWith(".class") && !name.contains("/") && !name.contains("$"))
                    .map(name -> name.substring(0, name.length() - ".class".length()))
                    .filter(name -> hasFactoryConstructor(name, type, publicConstructor, parameters))
                    .sorted()
                    .toList();
        } catch (IOException | URISyntaxException exception) {
            throw new GameException(GameException.NO_FOUND_TYPE_GAME, exception);
        }
    }

    /** Comprueba la herencia y la firma que necesita la fábrica correspondiente. */
    private static boolean hasFactoryConstructor(String name, Class<?> type,
            boolean publicConstructor, Class<?>... parameters) {
        try {
            Class<?> candidate = Class.forName(type.getPackageName() + "." + name,
                    false, type.getClassLoader());
            if (!isConcreteSubclass(candidate, type)) return false;
            if (publicConstructor) {
                candidate.getConstructor(parameters);
            } else {
                candidate.getDeclaredConstructor(parameters);
            }
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        } catch (ClassNotFoundException exception) {
            throw new GameException(GameException.NO_FOUND_TYPE_GAME, exception);
        }
    }

    /** Identifica implementaciones públicas, concretas y de nivel superior. */
    static boolean isConcreteSubclass(Class<?> candidate, Class<?> type) {
        return candidate != type && type.isAssignableFrom(candidate)
                && Modifier.isPublic(candidate.getModifiers())
                && !Modifier.isAbstract(candidate.getModifiers())
                && candidate.getEnclosingClass() == null;
    }

    /** Devuelve en orden las clases concretas con el constructor que usa el juego. */
    static List<Class<?>> load(String resource, Class<?> type, Class<?>... parameters) {
        try (var stream = GameCatalog.class.getResourceAsStream(resource)) {
            if (stream == null) throw new GameException(GameException.NO_FOUND_TYPE_GAME);
            Set<Class<?>> classes = new LinkedHashSet<>();
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String name = line.strip();
                    if (name.isEmpty() || name.startsWith("#")) continue;
                    Class<?> entry = Class.forName(name, false, GameCatalog.class.getClassLoader());
                    if (!isConcreteSubclass(entry, type)
                            || !entry.getPackageName().equals(GameCatalog.class.getPackageName())) {
                        throw new GameException(GameException.NO_FOUND_TYPE_GAME);
                    }
                    entry.getConstructor(parameters);
                    classes.add(entry);
                }
            }
            return List.copyOf(classes);
        } catch (IOException | ClassNotFoundException exception) {
            throw new GameException(GameException.NO_FOUND_TYPE_GAME, exception);
        } catch (NoSuchMethodException exception) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND, exception);
        }
    }
}
