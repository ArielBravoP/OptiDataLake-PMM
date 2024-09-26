package com.odl.producer.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ConfigWebSocket {

    @Bean
    public CommandLineRunner startWebSocket(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            List<String> websocketURIs = Arrays.asList(
                    "wss://stream.binance.com:9443/ws/btcusdt@trade",  // Para los trades de BTC
                    "wss://stream.binance.com:9443/ws/ethusdt@trade",  // Para los trades de ETH
                    "wss://stream.binance.com:9443/ws/xrpusdt@trade"   // Para los trades de XRP
            );

            // Crear un WebSocket para cada criptomoneda
            for (String uri : websocketURIs) {
                try {
                    BinanceWebSocketClient client = new BinanceWebSocketClient(new URI(uri), kafkaTemplate);
                    client.connect();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
