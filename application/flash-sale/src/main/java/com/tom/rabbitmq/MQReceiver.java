package com.tom.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


/**
 * @author: Tom
 * @date: 2020-03-03 14:56
 * @description: 消费者
 */
@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message) {
        log.info("receive message" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info("receive topic queue1 message" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info("receive topic queue2 message" + message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeader(byte[] message) {
        log.info("receive header queue2 message" + new String(message));
    }
}
