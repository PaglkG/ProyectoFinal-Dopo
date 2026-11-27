package presentation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee y escribe la configuración visual al final de una partida guardada.
 * La sesión serializada por Game permanece al inicio del archivo.
 * El marcador final permite encontrar la configuración sin leer primero
 * el objeto de dominio. La ventana usa esta lectura con guardados integrados
 * anteriores cuando no encuentra un auxiliar {@code .ui}.
 *
 * <p><strong>Para extender el formato visual:</strong> actualiza a la vez
 * {@code appendConfiguration()} y {@code readConfiguration()}. La ventana
 * coordina este auxiliar con {@link domain.Game#save(java.io.File)} y
 * {@link domain.Game#open(java.io.File)}; no serialices aquí objetos de dominio.</p>
 */
final class MatchFile {
    private static final int FORMAT_MARKER = 0x44504F31; // DPO1
    private static final int FOOTER_SIZE = Long.BYTES + Integer.BYTES;

    /** Impide crear instancias de este formato de archivo. */
    private MatchFile() { }

    /** Agrega la configuración y una referencia a su posición para leerla antes de abrir la sesión.
     * @param file archivo al que se agrega el bloque
     * @param configuration opciones visuales que se guardan
     * @throws IOException si no es posible escribir el bloque
     */
    static void appendConfiguration(File file, MatchConfiguration configuration) throws IOException {
        try (RandomAccessFile data = new RandomAccessFile(file, "rw")) {
            long start = data.length();
            data.seek(start);
            writeList(data, configuration.parametersRules());
            writeList(data, configuration.parametersPowers());
            data.writeUTF(configuration.typeGame());
            data.writeUTF(configuration.typeMatch());
            data.writeBoolean(configuration.playerOne());
            data.writeBoolean(configuration.againstBot());
            data.writeUTF(configuration.playerOneCountryCode());
            data.writeUTF(configuration.playerTwoCountryCode());
            data.writeUTF(configuration.playerOneHeadImage());
            data.writeUTF(configuration.playerTwoHeadImage());
            data.writeLong(start);
            data.writeInt(FORMAT_MARKER);
        }
    }

    /** Recupera las opciones guardadas antes de que Game reemplace la sesión activa.
     * @param file archivo que contiene el bloque visual
     * @return configuración visual persistida
     * @throws IOException si el bloque falta, está truncado o es inválido
     */
    static MatchConfiguration readConfiguration(File file) throws IOException {
        try (RandomAccessFile data = new RandomAccessFile(file, "r")) {
            long end = data.length() - FOOTER_SIZE;
            if (end < 0) throw missingConfiguration();
            data.seek(end);
            long start = data.readLong();
            if (data.readInt() != FORMAT_MARKER || start <= 0 || start >= end) {
                throw missingConfiguration();
            }
            data.seek(start);
            MatchConfiguration configuration = new MatchConfiguration(
                    readList(data), readList(data), data.readUTF(), data.readUTF(),
                    data.readBoolean(), data.readBoolean(), data.readUTF(), data.readUTF(),
                    data.readUTF(), data.readUTF());
            if (data.getFilePointer() != end || configuration.parametersRules().size() < 2) {
                throw new IOException("La configuración de la partida no es válida.");
            }
            try {
                Integer.parseInt(configuration.parametersRules().get(0));
                Integer.parseInt(configuration.parametersRules().get(1));
            } catch (NumberFormatException exception) {
                throw new IOException("Las dimensiones de la cancha no son válidas.", exception);
            }
            return configuration;
        }
    }

    private static IOException missingConfiguration() {
        return new IOException("El archivo no contiene la configuración de una partida guardada desde el juego.");
    }

    /** Escribe el tamaño de la lista y luego sus textos en el mismo orden.
     * @param data archivo abierto para escritura
     * @param values textos que se guardan
     * @throws IOException si falla la escritura
     */
    private static void writeList(RandomAccessFile data, List<String> values) throws IOException {
        data.writeInt(values.size());
        for (String value : values) data.writeUTF(value);
    }

    /** Recupera los textos después de validar que el tamaño cabe en el archivo.
     * @param data archivo posicionado al comienzo de la lista
     * @return valores leídos en el mismo orden
     * @throws IOException si el tamaño es inválido o no se pueden leer los textos
     */
    private static List<String> readList(RandomAccessFile data) throws IOException {
        int size = data.readInt();
        if (size < 0 || size > (data.length() - data.getFilePointer()) / Short.BYTES) {
            throw new IOException("La lista de opciones de la partida no es válida.");
        }
        List<String> values = new ArrayList<>();
        for (int index = 0; index < size; index++) values.add(data.readUTF());
        return values;
    }
}
