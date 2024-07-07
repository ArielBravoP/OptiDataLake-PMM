import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.swing.JOptionPane;

public class main {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\x_ari\\OneDrive\\Escritorio\\Lectura de datos\\FIX.4.4-FH_XSGO_P2-BCSGATEWAY.messages_20240305.log";

        // Uso de JOptionPane para obtener ventana emergente para aplicar el filtro deseado
        String filter = JOptionPane.showInputDialog(null, "Ingrese el filtro deseado o deje en blanco para leer todo el archivo:", "Filtro de Archivo", JOptionPane.QUESTION_MESSAGE);

        // Uso de Files.lines() para crear un Stream (procesa mas eficiente el archivo)
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            if (filter != null && !filter.isEmpty()) {
                stream
                        .filter(line -> line.contains(filter)) // Si no esta vacio aplica el filtro
                        .forEach(System.out::println); // Imprime cada línea que cumple con el filtro aplicado
            } else if (filter != null) {
                stream.forEach(System.out::println); // Imprime todas las líneas si no hay filtro
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al leer el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
