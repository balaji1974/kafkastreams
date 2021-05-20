package com.bala.kafkastreams.kstreamkstreamjoins.service;

import org.springframework.stereotype.Service;

import com.bala.kafkastreams.kstreamkstreamjoins.model.PaymentConfirmation;
import com.bala.kafkastreams.kstreamkstreamjoins.model.PaymentRequest;
import com.bala.kafkastreams.kstreamkstreamjoins.model.TransactionStatus;

@Service
public class RecordBuilder {
    public TransactionStatus getTransactionStatus(PaymentRequest request, PaymentConfirmation confirmation){
        String status = "Failure";
        if(request.getOTP().equals(confirmation.getOTP()))
            status = "Success";

        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setTransactionID(request.getTransactionID());
        transactionStatus.setStatus(status);
        return transactionStatus;
    }
}