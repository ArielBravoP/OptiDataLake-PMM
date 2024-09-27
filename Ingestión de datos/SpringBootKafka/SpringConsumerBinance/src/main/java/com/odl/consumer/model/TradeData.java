package com.odl.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Raw_data")  // Nombre de la colección en MongoDB
public class TradeData {

    @JsonProperty("e")
    private String eventType;  // Mapea el campo "e"
    @JsonProperty("E")
    private Long eventTime;    // Mapea el campo "E"
    @JsonProperty("s")
    private String symbol;     // Mapea el campo "s"
    @JsonProperty("t")
    private Long tradeId;      // Mapea el campo "t"
    @JsonProperty("p")
    private Double price;      // Mapea el campo "p"
    @JsonProperty("q")
    private Double quantity;   // Mapea el campo "q"
    @JsonProperty("T")
    private Long tradeTime;    // Mapea el campo "T"
    @JsonProperty("m")
    private Boolean buyerIsMaker;  // Mapea el campo "m"
    @JsonProperty("M")
    private Boolean marketMaker;   // Mapea el campo "M"
    // Getters y Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getEventTime() { return eventTime; }
    public void setEventTime(Long eventTime) { this.eventTime = eventTime; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Long getTradeId() { return tradeId; }
    public void setTradeId(Long tradeId) { this.tradeId = tradeId; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Long getTradeTime() { return tradeTime; }
    public void setTradeTime(Long tradeTime) { this.tradeTime = tradeTime; }

    public Boolean getBuyerIsMaker() { return buyerIsMaker; }
    public void setBuyerIsMaker(Boolean buyerIsMaker) { this.buyerIsMaker = buyerIsMaker; }

    public Boolean getMarketMaker() { return marketMaker; }
    public void setMarketMaker(Boolean marketMaker) { this.marketMaker = marketMaker; }
}
