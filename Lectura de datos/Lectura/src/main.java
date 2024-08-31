import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import java.util.HashMap;

public class main {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\gianf\\Documents\\GitHub\\OptiDataLake-PMM\\Lectura de datos\\FIX.4.4-FH_XSGO_P2-BCSGATEWAY.messages_20240305.log";

        // Uso de JOptionPane para obtener ventana emergente para aplicar el filtro deseado
        String filter = JOptionPane.showInputDialog(null, "Ingrese el filtro deseado o deje en blanco para leer todo el archivo:", "Filtro de Archivo", JOptionPane.QUESTION_MESSAGE);

        // Uso de Files.lines() para crear un Stream (procesa más eficientemente el archivo)
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            if (filter != null && !filter.isEmpty()) {
                stream
                        .filter(line -> line.contains(filter)) // Si no está vacío, aplica el filtro
                        .map(main::parseFIXMessage) // Parsea cada línea que cumple con el filtro
                        .forEach(System.out::println); // Imprime el resultado del parseo
            } else if (filter != null) {
                stream
                        .map(main::parseFIXMessage) // Parsea todas las líneas
                        .forEach(System.out::println); // Imprime el resultado del parseo
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al leer el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para parsear un mensaje FIX y extraer campos relevantes
    public static Map<String, String> parseFIXMessage(String message) {
        Map<String, String> fields = new HashMap<>();
        String[] pairs = message.split("\\u0001"); // Separa los campos por el delimitador FIX (SOH character)
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                fields.put(keyValue[0], keyValue[1]);
            }
        }
        return fields; // Retorna un mapa con los campos del mensaje
    }
}
