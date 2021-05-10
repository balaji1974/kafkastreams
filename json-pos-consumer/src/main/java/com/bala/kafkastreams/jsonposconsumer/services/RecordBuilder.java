package com.bala.kafkastreams.jsonposconsumer.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bala.kafkastreams.jsonposconsumer.model.HadoopRecord;
import com.bala.kafkastreams.jsonposconsumer.model.LineItem;
import com.bala.kafkastreams.jsonposconsumer.model.Notification;
import com.bala.kafkastreams.jsonposconsumer.model.PosInvoice;

@Service
public class RecordBuilder {
	public Notification getNotification(PosInvoice invoice){
        Notification notification = new Notification();
        notification.setInvoiceNumber(invoice.getInvoiceNumber());
        notification.setCustomerCardNo(invoice.getCustomerCardNo());
        notification.setTotalAmount(invoice.getTotalAmount());
        notification.setEarnedLoyaltyPoints(invoice.getTotalAmount() * 0.02);
        return notification;
    }

    public PosInvoice getMaskedInvoice(PosInvoice invoice){
        invoice.setCustomerCardNo(null);
        if (invoice.getDeliveryType().equalsIgnoreCase("HOME-DELIVERY")) {
            invoice.getDeliveryAddress().setAddressLine(null);
            invoice.getDeliveryAddress().setContactNumber(null);
        }
        return invoice;
    }

    public List<HadoopRecord> getHadoopRecords(PosInvoice invoice){
        List<HadoopRecord> records = new ArrayList<>();

        for (LineItem i : invoice.getInvoiceLineItems()) {
            HadoopRecord record = new HadoopRecord();
            record.setInvoiceNumber(invoice.getInvoiceNumber());
            record.setCreatedTime(invoice.getCreatedTime());
            record.setStoreID(invoice.getStoreID());
            record.setPosID(invoice.getPosID());
            record.setCustomerType(invoice.getCustomerType());
            record.setPaymentMethod(invoice.getPaymentMethod());
            record.setDeliveryType(invoice.getDeliveryType());
            record.setItemCode(i.getItemCode());
            record.setItemDescription(i.getItemDescription());
            record.setItemPrice(i.getItemPrice());
            record.setItemQty(i.getItemQty());
            record.setTotalValue(i.getTotalValue());
            if (invoice.getDeliveryType().toString().equalsIgnoreCase("HOME-DELIVERY")) {
                record.setCity(invoice.getDeliveryAddress().getCity());
                record.setState(invoice.getDeliveryAddress().getState());
                record.setPinCode(invoice.getDeliveryAddress().getPinCode());
            }
            records.add(record);
        }
        return records;
    }
    
    public com.bala.kafkastreams.jsonposconsumer.model.avro.PosInvoice getShipmentRecord(PosInvoice invoice){
        
		
    	com.bala.kafkastreams.jsonposconsumer.model.avro.DeliveryAddress deliveryAddress=new 
    			com.bala.kafkastreams.jsonposconsumer.model.avro.DeliveryAddress(
    					invoice.getDeliveryAddress().getAddressLine(), 
    					invoice.getDeliveryAddress().getCity(), 
    					invoice.getDeliveryAddress().getState(), 
    					invoice.getDeliveryAddress().getPinCode(), 
    					invoice.getDeliveryAddress().getContactNumber()	
    					);
    	
    	List<com.bala.kafkastreams.jsonposconsumer.model.avro.LineItem> lineItems=new ArrayList<com.bala.kafkastreams.jsonposconsumer.model.avro.LineItem>();
    	
    	for(LineItem lnIt:invoice.getInvoiceLineItems()) {
    		com.bala.kafkastreams.jsonposconsumer.model.avro.LineItem lineItem=new 
    			com.bala.kafkastreams.jsonposconsumer.model.avro.LineItem(
    					lnIt.getItemCode(), 
    					lnIt.getItemDescription(), 
    					lnIt.getItemPrice(), 
    					lnIt.getItemQty(), 
    					lnIt.getItemPrice());
    		lineItems.add(lineItem);
    	}
    	
    	
    	
    	
    	
    	
    	com.bala.kafkastreams.jsonposconsumer.model.avro.PosInvoice posInvoice=
        		new com.bala.kafkastreams.jsonposconsumer.model.avro.PosInvoice(invoice.getInvoiceNumber(), 
        				invoice.getCreatedTime(), invoice.getCustomerCardNo(), 
        				invoice.getTotalAmount(), invoice.getNumberOfItems(), invoice.getPaymentMethod(), 
        				invoice.getTaxableAmount(), invoice.getCGST(), invoice.getSGST(), 
        				invoice.getCESS(), invoice.getStoreID(), invoice.getPosID(), invoice.getCashierID(), 
        				invoice.getCustomerType(), invoice.getDeliveryType(), deliveryAddress, 
        				lineItems);
        
        
        return posInvoice;
    }
}
