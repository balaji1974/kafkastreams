package com.bala.kafkastreams.kstreamreduceconsumer.service;

import org.springframework.stereotype.Service;

import com.bala.kafkastreams.model.Notification;
import com.bala.kafkastreams.model.PosInvoice;


@Service
public class RecordBuilder {
	public Notification getNotification(PosInvoice invoice){
        Notification notification = new Notification();
        notification.setInvoiceNumber(invoice.getInvoiceNumber());
        notification.setCustomerCardNo(invoice.getCustomerCardNo());
        notification.setTotalAmount(invoice.getTotalAmount());
        notification.setEarnedLoyaltyPoints(invoice.getTotalAmount() * 0.02);
        notification.setTotalLoyaltyPoints(notification.getEarnedLoyaltyPoints());
        return notification;
    }

    
}
