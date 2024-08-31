package com.optidatalake.binance;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public class BinanceApiClient {
    private static final String API_URL = "https://api.binance.com";

    public static void getMarketPriceData() {
        HttpResponse<JsonNode> response = Unirest.get(API_URL + "/api/v3/ticker/price")
                .asJson();

        if (response.getStatus() == 200) {
            System.out.println("Market price data retrieved successfully!");
            System.out.println(response.getBody().toString());
        } else {
            System.out.println("Failed to retrieve market price data. Status: " + response.getStatus());
        }
    }



    public static void getDepthData(String symbol) {
        HttpResponse<JsonNode> response = Unirest.get(API_URL + "/api/v3/depth")
                .queryString("symbol", symbol)
                .queryString("limit", "100")
                .asJson();

        if (response.getStatus() == 200) {
            System.out.println("Depth data for " + symbol + " retrieved successfully!");
            System.out.println(response.getBody().toString());
        } else {
            System.out.println("Failed to retrieve depth data for " + symbol + ". Status: " + response.getStatus());
        }
    }

    public static void getHistoricalData(String symbol, String interval) {
        HttpResponse<JsonNode> response = Unirest.get(API_URL + "/api/v3/klines")
                .queryString("symbol", symbol)
                .queryString("interval", interval)
                .asJson();

        if (response.getStatus() == 200) {
            System.out.println("Historical data for " + symbol + " retrieved successfully!");
            System.out.println(response.getBody().toString());
        } else {
            System.out.println("Failed to retrieve historical data for " + symbol + ". Status: " + response.getStatus());
        }
    }

    public static void main(String[] args) {
        getMarketPriceData();
        getDepthData("BTCUSDT");
        getHistoricalData("BTCUSDT", "1h");
    }
}
