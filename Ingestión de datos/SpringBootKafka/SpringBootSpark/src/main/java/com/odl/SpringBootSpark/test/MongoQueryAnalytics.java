package com.odl.SpringBootSpark.test;

import com.mongodb.client.*;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

public class MongoQueryAnalytics extends JFrame {

    private MongoCollection<Document> collection;
    private JComboBox<String> symbolComboBox;
    private JComboBox<String> metricComboBox;
    private JTextField minValueField, maxValueField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel queryTimeLabel;
    private JLabel totalResultsLabel;
    private JLabel dataSizeLabel;
    private JCheckBox[] columnCheckBoxes;

    // Constructor que inicializa la conexión a MongoDB y la interfaz gráfica
    public MongoQueryAnalytics() {
        try {
            // Conectar a MongoDB
            String uri = "mongodb://localhost:27017";
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("OptiDataLake");
            this.collection = database.getCollection("Analytics");

            // Mensaje de éxito al conectarse
            System.out.println("Conexión a MongoDB exitosa. Base de datos: " + database.getName());

        } catch (Exception e) {
            // Mensaje en caso de error
            System.err.println("Error al conectar a MongoDB: " + e.getMessage());
        }

        // Configuración de la ventana
        setTitle("Consulta de Datos de Analytics en MongoDB");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crear panel superior con campos de texto y etiquetas
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 4, 5, 5));

        // Crear campos y etiquetas
        JLabel symbolLabel = new JLabel("Selecciona la criptomoneda:");
        symbolComboBox = new JComboBox<>(new String[]{"BTCUSDT", "ETHUSDT", "XRPUSDT"});
        JLabel metricLabel = new JLabel("Selecciona la métrica para filtrar:");
        metricComboBox = new JComboBox<>(new String[]{"Volumen Total", "Promedio de Precios", "Volatilidad", "Movimiento de Precios"});
        JLabel minValueLabel = new JLabel("Valor mínimo:");
        minValueField = new JTextField();
        JLabel maxValueLabel = new JLabel("Valor máximo:");
        maxValueField = new JTextField();

        // Checkboxes para seleccionar columnas
        JLabel selectColumnsLabel = new JLabel("Selecciona las columnas a mostrar:");
        String[] columns = {"Cryptocurrency", "Time Window", "Volumen Total", "Promedio de Precios", "Volatilidad", "Movimiento de Precios"};
        columnCheckBoxes = new JCheckBox[columns.length];
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new GridLayout(2, 3, 5, 5));
        for (int i = 0; i < columns.length; i++) {
            columnCheckBoxes[i] = new JCheckBox(columns[i], true);
            checkBoxPanel.add(columnCheckBoxes[i]);
        }

        // Agregar campos y etiquetas al panel
        inputPanel.add(symbolLabel);
        inputPanel.add(symbolComboBox);
        inputPanel.add(metricLabel);
        inputPanel.add(metricComboBox);
        inputPanel.add(minValueLabel);
        inputPanel.add(minValueField);
        inputPanel.add(maxValueLabel);
        inputPanel.add(maxValueField);

        // Crear botón para ejecutar la consulta
        JButton queryButton = new JButton("Buscar");

        // Crear panel principal para los inputs y checkboxes
        JPanel mainInputPanel = new JPanel();
        mainInputPanel.setLayout(new BorderLayout(5, 5));
        mainInputPanel.add(inputPanel, BorderLayout.NORTH);
        mainInputPanel.add(selectColumnsLabel, BorderLayout.WEST);
        mainInputPanel.add(checkBoxPanel, BorderLayout.CENTER);
        mainInputPanel.add(queryButton, BorderLayout.EAST);

        // Agregar el panel de entrada a la ventana
        add(mainInputPanel, BorderLayout.NORTH);

        // Crear tabla y modelo de tabla para mostrar los resultados
        tableModel = new DefaultTableModel();
        for (String column : columns) {
            tableModel.addColumn(column);
        }

        resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);

        // Agregar tabla a la ventana
        add(scrollPane, BorderLayout.CENTER);

        // Panel para mostrar el tiempo de consulta, total de resultados y tamaño de datos
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(3, 1));

        queryTimeLabel = new JLabel("Tiempo de consulta: ");
        totalResultsLabel = new JLabel("Total de resultados: ");
        dataSizeLabel = new JLabel("Tamaño de datos recuperados: ");

        infoPanel.add(queryTimeLabel);
        infoPanel.add(totalResultsLabel);
        infoPanel.add(dataSizeLabel);

        // Agregar el panel de información a la ventana
        add(infoPanel, BorderLayout.SOUTH);

        // Acción del botón
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runQuery();
            }
        });
    }

    // Método que ejecuta la consulta con los valores ingresados en la interfaz
    private void runQuery() {
        // Obtener el símbolo seleccionado
        String symbol = (String) symbolComboBox.getSelectedItem();
        String metric = (String) metricComboBox.getSelectedItem();

        double minValue;
        double maxValue;

        try {
            // Obtener valores de los campos de texto
            minValue = Double.parseDouble(minValueField.getText());
            maxValue = Double.parseDouble(maxValueField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa valores válidos para el valor mínimo y máximo.");
            return;
        }

        // Limpiar la tabla antes de realizar la nueva consulta
        tableModel.setRowCount(0);

        // Tomar el tiempo inicial usando System.nanoTime() para mayor precisión
        long startTime = System.nanoTime();

        // Realizar el conteo de documentos
        long totalDocuments = collection.countDocuments(and(
                eq("cryptocurrency", symbol),
                gte(getMetricField(metric), minValue),
                lte(getMetricField(metric), maxValue)
        ));

        // Realizar consulta con filtros basados en la entrada del usuario y ordenando por la métrica seleccionada
        FindIterable<Document> results = collection.find(and(
                eq("cryptocurrency", symbol),
                gte(getMetricField(metric), minValue),
                lte(getMetricField(metric), maxValue)
        )).sort(ascending(getMetricField(metric)));

        // Contador de resultados encontrados
        int count = 0;

        List<Object[]> rows = new ArrayList<>();
        long totalSizeBytes = 0;

        for (Document doc : results) {
            String cryptocurrency = doc.getString("cryptocurrency");
            String timeWindow = doc.get("time_window").toString();
            double volumenTotal = doc.getDouble("volumen_total");
            double promedioPrecios = doc.getDouble("promedio_precios");
            double volatilidad = doc.getDouble("volatilidad");
            double movimientoPrecios = doc.getDouble("movimiento_precios");

            // Agregar datos a la tabla según las columnas seleccionadas
            List<Object> row = new ArrayList<>();
            if (columnCheckBoxes[0].isSelected()) row.add(cryptocurrency);
            if (columnCheckBoxes[1].isSelected()) row.add(timeWindow);
            if (columnCheckBoxes[2].isSelected()) row.add(volumenTotal);
            if (columnCheckBoxes[3].isSelected()) row.add(promedioPrecios);
            if (columnCheckBoxes[4].isSelected()) row.add(volatilidad);
            if (columnCheckBoxes[5].isSelected()) row.add(movimientoPrecios);

            rows.add(row.toArray());
            count++;

            // Calcular el tamaño del documento en bytes usando JSON, evitando el error
            totalSizeBytes += doc.toJson().getBytes().length;
        }

        // Actualizar la tabla con los resultados
        tableModel.setColumnCount(0);
        for (JCheckBox checkBox : columnCheckBoxes) {
            if (checkBox.isSelected()) {
                tableModel.addColumn(checkBox.getText());
            }
        }
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }

        // Calcular el tiempo final usando System.nanoTime()
        long endTime = System.nanoTime();
        long queryTime = (endTime - startTime) / 1_000_000;  // Convertir de nanosegundos a milisegundos

        // Calcular el tamaño total en megabytes
        double totalSizeMB = totalSizeBytes / (1024.0 * 1024.0);
        double mbPerSecond = (totalSizeMB / (queryTime / 1000.0));

        // Mostrar tiempo de consulta, total de resultados y tamaño de datos
        queryTimeLabel.setText("Tiempo de consulta: " + queryTime + " ms");
        totalResultsLabel.setText("Total de resultados: " + count);
        dataSizeLabel.setText(String.format("Tamaño de datos recuperados: %.2f MB (%.2f MB/s)", totalSizeMB, mbPerSecond));

        // Mostrar mensaje si no se encontraron resultados
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron resultados para la consulta.");
        }
    }

    // Método para obtener el campo correspondiente a la métrica seleccionada
    private String getMetricField(String metric) {
        switch (metric) {
            case "Volumen Total":
                return "volumen_total";
            case "Promedio de Precios":
                return "promedio_precios";
            case "Volatilidad":
                return "volatilidad";
            case "Movimiento de Precios":
                return "movimiento_precios";
            default:
                throw new IllegalArgumentException("Métrica no válida: " + metric);
        }
    }

    // Método principal para iniciar la aplicación con la interfaz gráfica
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MongoQueryAnalytics gui = new MongoQueryAnalytics();
            gui.setVisible(true);
        });
    }
}
