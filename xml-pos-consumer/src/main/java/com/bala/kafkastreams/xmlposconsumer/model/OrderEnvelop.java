package com.bala.kafkastreams.xmlposconsumer.model;

import com.bala.kafkastreams.model.Order;

import lombok.Data;

@Data
public class OrderEnvelop {
    String xmlOrderKey;
    String xmlOrderValue;

    String orderTag;
    Order validOrder;
}