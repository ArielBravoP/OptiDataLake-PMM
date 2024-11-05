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
    private MongoDatabase database;
    private JTextField symbolField, minPriceField, maxPriceField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel queryTimeLabel;
    private JLabel totalResultsLabel;
    private JLabel dataSizeLabel;

    // Casillas de verificación para seleccionar las columnas a mostrar
    private JCheckBox showSymbol, showPrice, showQuantity, showTradeTime, showEventType, showEventTime, showTradeId, showBuyerIsMaker, showMarketMaker;

    // Constructor que inicializa la conexión a MongoDB y la interfaz gráfica
    public MongoQuery() {
        try {
            // Conectar a MongoDB
            String uri = "mongodb://localhost:27017";
            MongoClient mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("OptiDataLake");
            this.collection = database.getCollection("Raw_data");

            // Mensaje de éxito al conectarse
            System.out.println("Conexión a MongoDB exitosa. Base de datos: " + database.getName());

        } catch (Exception e) {
            // Mensaje en caso de error
            System.err.println("Error al conectar a MongoDB: " + e.getMessage());
        }

        // Configuración de la ventana
        setTitle("Consulta de Precios en MongoDB");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crear panel superior con campos de texto y etiquetas
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(10, 2));

        // Crear campos y etiquetas
        JLabel symbolLabel = new JLabel("Símbolo de las monedas (separados por comas, ej: BTCUSDT,ETHUSDT):");
        symbolField = new JTextField();
        JLabel minPriceLabel = new JLabel("Precio mínimo:");
        minPriceField = new JTextField();
        JLabel maxPriceLabel = new JLabel("Precio máximo:");
        maxPriceField = new JTextField();

        // Casillas de verificación para las columnas
        showSymbol = new JCheckBox("Mostrar Símbolo", true);
        showPrice = new JCheckBox("Mostrar Precio", true);
        showQuantity = new JCheckBox("Mostrar Cantidad", true);
        showTradeTime = new JCheckBox("Mostrar Tiempo de Trade", true);
        showEventType = new JCheckBox("Mostrar Tipo de Evento", true);
        showEventTime = new JCheckBox("Mostrar Tiempo de Evento", true);
        showTradeId = new JCheckBox("Mostrar TradeId", true);
        showBuyerIsMaker = new JCheckBox("Mostrar BuyerIsMaker", true);
        showMarketMaker = new JCheckBox("Mostrar MarketMaker", true);

        // Agregar campos y etiquetas al panel
        inputPanel.add(symbolLabel);
        inputPanel.add(symbolField);
        inputPanel.add(minPriceLabel);
        inputPanel.add(minPriceField);
        inputPanel.add(maxPriceLabel);
        inputPanel.add(maxPriceField);

        // Agregar casillas de verificación para las columnas
        inputPanel.add(showSymbol);
        inputPanel.add(showPrice);
        inputPanel.add(showQuantity);
        inputPanel.add(showTradeTime);
        inputPanel.add(showEventType);
        inputPanel.add(showEventTime);
        inputPanel.add(showTradeId);
        inputPanel.add(showBuyerIsMaker);
        inputPanel.add(showMarketMaker);

        // Crear botón para ejecutar la consulta
        JButton queryButton = new JButton("Buscar");
        inputPanel.add(new JLabel()); // Espacio vacío
        inputPanel.add(queryButton);

        // Agregar el panel de entrada a la ventana
        add(inputPanel, BorderLayout.NORTH);

        // Crear tabla y modelo de tabla para mostrar los resultados
        tableModel = new DefaultTableModel();
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

        // Acción del botón "Buscar"
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runQuery();
            }
        });
    }

    // Método que ejecuta la consulta con los valores ingresados en la interfaz
    private void runQuery() {
        // Obtener los símbolos de las monedas y separarlos por comas
        String symbolsInput = symbolField.getText();
        String[] symbols = symbolsInput.split(",");

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

        // Agregar columnas seleccionadas por el usuario
        tableModel.setColumnCount(0);
        if (showSymbol.isSelected()) tableModel.addColumn("Symbol");
        if (showPrice.isSelected()) tableModel.addColumn("Price");
        if (showQuantity.isSelected()) tableModel.addColumn("Quantity");
        if (showTradeTime.isSelected()) tableModel.addColumn("TradeTime");
        if (showEventType.isSelected()) tableModel.addColumn("EventType");
        if (showEventTime.isSelected()) tableModel.addColumn("EventTime");
        if (showTradeId.isSelected()) tableModel.addColumn("TradeId");
        if (showBuyerIsMaker.isSelected()) tableModel.addColumn("BuyerIsMaker");
        if (showMarketMaker.isSelected()) tableModel.addColumn("MarketMaker");

        // Crear índice en los campos más utilizados si no existe
        collection.createIndex(ascending("symbol", "price"));

        // Obtener estadísticas de la colección antes de la consulta
        Document statsCommand = new Document("collStats", "Raw_data");
        Document stats = database.runCommand(statsCommand);

        // Manejar tanto Double como Integer para el valor "avgObjSize"
        double avgObjSize;
        Object avgObjSizeObj = stats.get("avgObjSize");
        if (avgObjSizeObj instanceof Integer) {
            avgObjSize = ((Integer) avgObjSizeObj).doubleValue();
        } else if (avgObjSizeObj instanceof Double) {
            avgObjSize = (Double) avgObjSizeObj;
        } else {
            avgObjSize = 0.0;
        }

        // Tomar el tiempo inicial usando System.nanoTime() para mayor precisión
        long startTime = System.nanoTime();

        // Realizar la consulta
        FindIterable<Document> results = collection.find(and(
                in("symbol", symbols),
                gte("price", minPrice),
                lte("price", maxPrice)
        )).sort(ascending("price"));  // Ordenar por price en orden ascendente

        // Contador de resultados encontrados
        int count = 0;

        List<Object[]> rows = new ArrayList<>();

        for (Document doc : results) {
            List<Object> row = new ArrayList<>();
            if (showSymbol.isSelected()) row.add(doc.getString("symbol"));
            if (showPrice.isSelected()) row.add(convertPriceToDouble(doc.get("price")));
            if (showQuantity.isSelected()) row.add(doc.get("quantity"));
            if (showTradeTime.isSelected()) row.add(doc.get("tradeTime"));
            if (showEventType.isSelected()) row.add(doc.getString("eventType"));
            if (showEventTime.isSelected()) row.add(doc.get("eventTime"));
            if (showTradeId.isSelected()) row.add(doc.get("tradeId"));
            if (showBuyerIsMaker.isSelected()) row.add(doc.getBoolean("buyerIsMaker"));
            if (showMarketMaker.isSelected()) row.add(doc.getBoolean("marketMaker"));

            rows.add(row.toArray());
            count++;
        }

        // Actualizar la tabla con los resultados
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }

        // Calcular el tiempo final usando System.nanoTime()
        long endTime = System.nanoTime();
        long queryTime = (endTime - startTime) / 1_000_000;  // Convertir de nanosegundos a milisegundos

        // Calcular el tamaño total en megabytes y MB por segundo
        double totalSizeMB = (count * avgObjSize) / (1024.0 * 1024.0);
        double mbPerSecond = totalSizeMB / (queryTime / 1000.0);

        // Mostrar tiempo de consulta, total de resultados y tamaño de datos
        queryTimeLabel.setText("Tiempo de consulta: " + queryTime + " ms");
        totalResultsLabel.setText("Total de resultados: " + count);
        dataSizeLabel.setText(String.format("Tamaño de datos recuperados: %.2f MB (%.2f MB/s)", totalSizeMB, mbPerSecond));

        // Mostrar mensaje si no se encontraron resultados
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron resultados para la consulta.");
        }
    }

    // Método auxiliar para convertir el campo "price" a Double si es necesario
    private double convertPriceToDouble(Object priceObject) {
        if (priceObject instanceof Double) {
            return (Double) priceObject;
        } else if (priceObject instanceof String) {
            try {
                return Double.parseDouble((String) priceObject);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else {
            return 0.0;
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
