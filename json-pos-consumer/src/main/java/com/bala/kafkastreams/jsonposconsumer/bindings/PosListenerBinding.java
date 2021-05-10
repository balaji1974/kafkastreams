package com.bala.kafkastreams.jsonposconsumer.bindings;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;

import com.bala.kafkastreams.jsonposconsumer.model.HadoopRecord;
import com.bala.kafkastreams.jsonposconsumer.model.Notification;
import com.bala.kafkastreams.jsonposconsumer.model.PosInvoice;

public interface PosListenerBinding {

    /*@Input("notification-input-channel")
    KStream<String, PosInvoice> notificationInputStream();

    @Output("notification-output-channel")
    KStream<String, Notification> notificationOutputStream();

    @Input("hadoop-input-channel")
    KStream<String, PosInvoice> hadoopInputStream();

    @Output("hadoop-output-channel")
    KStream<String, HadoopRecord> hadoopOutputStream();*/

}
