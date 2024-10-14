package com.odl.consumer.repository;

import com.odl.consumer.model.TradeData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeDataRepository extends MongoRepository<TradeData, String> {
}