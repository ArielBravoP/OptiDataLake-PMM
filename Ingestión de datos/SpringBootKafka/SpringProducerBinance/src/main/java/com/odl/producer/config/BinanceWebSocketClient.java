package com.odl.producer.config;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class BinanceWebSocketClient extends WebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(BinanceWebSocketClient.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BinanceWebSocketClient(URI serverUri, KafkaTemplate<String, String> kafkaTemplate) {
        super(serverUri);
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("Conexión WebSocket abierta");
    }

    @Override
    public void onMessage(String message) {
        kafkaTemplate.send("odl-ticks", message); // Envía cada tick al topic "odl-ticks" en Kafka
        logger.info("Mensaje enviado a Kafka: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Conexión cerrada: " + reason + ". Código de cierre: " + code);
    }

    @Override
    public void onError(Exception ex) {
        logger.error("Error en la conexión WebSocket: ", ex);
    }

    /* Ejemplo main
    public static void main(String[] args) throws URISyntaxException {
        String wsURL = "wss://stream.binance.com:9443/ws/btcusdt@trade"; //BTC/USDT
        KafkaTemplate<String, String> kafkaTemplate = null; // Ejemplo
        BinanceWebSocketClient client = new BinanceWebSocketClient(new URI(wsURL), kafkaTemplate);
        client.connect();

    }*/
}
