package com.odl.producer;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringProducerBinanceApplication {

	private static final Logger logger = LoggerFactory.getLogger(SpringProducerBinanceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringProducerBinanceApplication.class, args);
	}

	@Bean
	CommandLineRunner init(KafkaTemplate<String, String> kafkaTemplate, MarketDataService marketDataService) {
		return args -> {
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(() -> {
				try {
					Ticker ticker = marketDataService.getTicker(CurrencyPair.BTC_USDT);
					String message = "BTC/USD: " + ticker.getLast();

					// Enviar el mensaje a Kafka y registrar el mensaje enviado
					kafkaTemplate.send("odl", message);
					logger.info("Mensaje enviado a Kafka: " + message);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 0, 1, TimeUnit.SECONDS);
			// Configuración del tiempo:
			/*
			 * Ejemplos de TimeUnit:
			 * - TimeUnit.SECONDS: Intervalo en segundos. TimeUnit.SECONDS.sleep(10); // 10 segundos
			 * - TimeUnit.MINUTES: Intervalo en minutos. TimeUnit.MINUTES.sleep(1); // 1 minuto
			 * - TimeUnit.MILLISECONDS: Intervalo en milisegundos. TimeUnit.MILLISECONDS.sleep(500); // 500 milisegundos
			 *
			 * Para cambiar el intervalo de tiempo en que se solicita la información, se ajusta el tercer parámetro de scheduleAtFixedRate:
			 * - Para cada milisegundo: scheduler.scheduleAtFixedRate(..., 0, 1, TimeUnit.MILLISECONDS);
			 * - Para cada segundo: scheduler.scheduleAtFixedRate(..., 0, 1, TimeUnit.SECONDS);
			 * - Para cada minuto: scheduler.scheduleAtFixedRate(..., 0, 1, TimeUnit.MINUTES);
			 */
		};
	}
}

