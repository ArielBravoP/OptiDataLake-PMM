package com.odl.consumer.repository;

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

public class MongoQuery extends JFrame {

    private MongoCollection<Document> collection;
    private JTextField symbolField, minPriceField, maxPriceField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel queryTimeLabel;
    private JLabel totalResultsLabel;

    // Constructor que inicializa la conexión a MongoDB y la interfaz gráfica
    public MongoQuery() {
        try {
            // Conectar a MongoDB
            String uri = "mongodb://localhost:27017"; // Ajusta según tu configuración
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("OptiDataLake");
            this.collection = database.getCollection("Raw_data");

            // Mensaje de éxito al conectarse
            System.out.println("Conexión a MongoDB exitosa. Base de datos: " + database.getName());

        } catch (Exception e) {
            // Mensaje en caso de error
            System.err.println("Error al conectar a MongoDB: " + e.getMessage());
        }

        // Configuración de la ventana
        setTitle("Consulta de Precios en MongoDB");
        setSize(1000, 700);  // Ajusta el tamaño para acomodar más columnas
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crear panel superior con campos de texto y etiquetas
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        // Crear campos y etiquetas
        JLabel symbolLabel = new JLabel("Símbolo de la moneda (ej: BTCUSDT):");
        symbolField = new JTextField();
        JLabel minPriceLabel = new JLabel("Precio mínimo:");
        minPriceField = new JTextField();
        JLabel maxPriceLabel = new JLabel("Precio máximo:");
        maxPriceField = new JTextField();

        // Agregar campos y etiquetas al panel
        inputPanel.add(symbolLabel);
        inputPanel.add(symbolField);
        inputPanel.add(minPriceLabel);
        inputPanel.add(minPriceField);
        inputPanel.add(maxPriceLabel);
        inputPanel.add(maxPriceField);

        // Crear botón para ejecutar la consulta
        JButton queryButton = new JButton("Buscar");
        inputPanel.add(new JLabel()); // Espacio vacío
        inputPanel.add(queryButton);

        // Agregar el panel de entrada a la ventana
        add(inputPanel, BorderLayout.NORTH);

        // Crear tabla y modelo de tabla para mostrar los resultados
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Symbol");
        tableModel.addColumn("Price");
        tableModel.addColumn("Quantity");
        tableModel.addColumn("TradeTime");
        tableModel.addColumn("EventType");
        tableModel.addColumn("EventTime");
        tableModel.addColumn("TradeId");
        tableModel.addColumn("BuyerIsMaker");
        tableModel.addColumn("MarketMaker");

        resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);

        // Agregar tabla a la ventana
        add(scrollPane, BorderLayout.CENTER);

        // Panel para mostrar el tiempo de consulta y los resultados
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 1));

        queryTimeLabel = new JLabel("Tiempo de consulta: ");
        totalResultsLabel = new JLabel("Total de resultados: ");

        infoPanel.add(queryTimeLabel);
        infoPanel.add(totalResultsLabel);

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
        String symbol = symbolField.getText();
        double minPrice;
        double maxPrice;

        try {
            // Obtener valores de los campos de texto
            minPrice = Double.parseDouble(minPriceField.getText());
            maxPrice = Double.parseDouble(maxPriceField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa valores válidos para el precio mínimo y máximo.");
            return;
        }

        // Limpiar la tabla antes de realizar la nueva consulta
        tableModel.setRowCount(0);

        // Tomar el tiempo inicial
        long startTime = System.currentTimeMillis();

        // Realizar consulta con filtros basados en la entrada del usuario y ordenando por price ascendente
        FindIterable<Document> results = collection.find(and(
                eq("symbol", symbol),
                gte("price", minPrice),  // Comparar price como double
                lte("price", maxPrice)   // Comparar price como double
        )).sort(ascending("price"));  // Ordenar por price en orden ascendente

        // Contador de resultados encontrados
        int count = 0;

        List<Object[]> rows = new ArrayList<>();

        for (Document doc : results) {
            String symbolResult = doc.getString("symbol");

            // Recuperar el campo "price" y convertir a Double si es necesario
            Object priceObject = doc.get("price");
            double priceResult;
            if (priceObject instanceof Double) {
                priceResult = (Double) priceObject;
            } else if (priceObject instanceof String) {
                try {
                    priceResult = Double.parseDouble((String) priceObject);  // Convertir string a double
                } catch (NumberFormatException e) {
                    continue;  // Saltar si no se puede convertir
                }
            } else {
                continue;  // Si no es ni Double ni String, ignorar este documento
            }

            // Recuperar otros campos
            double quantity = doc.get("quantity") instanceof Double ? (Double) doc.get("quantity") : 0.0;
            long tradeTime = doc.get("tradeTime") instanceof Long ? (Long) doc.get("tradeTime") : 0L;
            String eventType = doc.getString("eventType");
            long eventTime = doc.get("eventTime") instanceof Long ? (Long) doc.get("eventTime") : 0L;
            long tradeId = doc.get("tradeId") instanceof Long ? (Long) doc.get("tradeId") : 0L;
            boolean buyerIsMaker = doc.getBoolean("buyerIsMaker", false);
            boolean marketMaker = doc.getBoolean("marketMaker", false);

            // Agregar datos a la tabla
            rows.add(new Object[]{
                    symbolResult, priceResult, quantity, tradeTime, eventType, eventTime, tradeId, buyerIsMaker, marketMaker
            });
            count++;
        }

        // Actualizar la tabla con los resultados
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }

        // Calcular el tiempo final
        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;  // Tiempo en milisegundos

        // Mostrar tiempo de consulta y total de resultados
        queryTimeLabel.setText("Tiempo de consulta: " + queryTime + " ms");
        totalResultsLabel.setText("Total de resultados: " + count);

        // Mostrar mensaje si no se encontraron resultados
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron resultados para la consulta.");
        }
    }

    // Método principal para iniciar la aplicación con la interfaz gráfica
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MongoQuery gui = new MongoQuery();
            gui.setVisible(true);
        });
    }
}