package presentation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Consulta los textos, campos y recursos visuales de las opciones del juego. */
final class PresentationOptions {
    private static final Properties OPTIONS = load();

    /** Evita instanciar el catálogo visual compartido. */
    private PresentationOptions() { }

    /** Lee una propiedad visual o usa el valor indicado si falta. */
    static String get(String key, String fallback) {
        return OPTIONS.getProperty(key, fallback).strip();
    }

    /** Muestra el texto configurado para una opción, con su identificador como respaldo. */
    static String label(String className) {
        String name = simpleName(className);
        return get("option." + name + ".label", name);
    }

    /** Devuelve el nombre que usan los catálogos de la fachada. */
    static String simpleName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /** Lee un recurso o texto asociado al código de país. */
    static String character(String countryCode, String property, String fallback) {
        return get("character." + countryCode + "." + property, fallback);
    }

    /** Carga los metadatos sin depender de clases del dominio. */
    private static Properties load() {
        Properties properties = new Properties();
        try (var resource = PresentationOptions.class.getResourceAsStream("options.properties")) {
            if (resource != null) {
                properties.load(new InputStreamReader(resource, StandardCharsets.UTF_8));
            }
        } catch (IOException | IllegalArgumentException exception) {
            properties.clear();
        }
        return properties;
    }
}
