package com.odl.producer.config;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigBinance {

    @Bean
    public Exchange binanceExchange() {
        return ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class.getName());
    }

    @Bean
    public MarketDataService marketDataService(Exchange binanceExchange) {
        return binanceExchange.getMarketDataService();
    }

    @Bean
    public Ticker getTicker(MarketDataService marketDataService) throws Exception {
        // Se cambia CurrencyPair.BTC_USDT por el par de divisas que se quiera consultar
        return marketDataService.getTicker(CurrencyPair.BTC_USDT);
    }
}
