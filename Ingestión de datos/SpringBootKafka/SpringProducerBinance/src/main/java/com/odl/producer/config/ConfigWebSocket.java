package com.odl.producer.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class ConfigWebSocket {

    @Bean
    public CommandLineRunner startWebSocket(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            try {
                BinanceWebSocketClient client = new BinanceWebSocketClient(
                        new URI("wss://stream.binance.com:9443/ws/btcusdt@trade"), kafkaTemplate);
                client.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        };
    }

}


