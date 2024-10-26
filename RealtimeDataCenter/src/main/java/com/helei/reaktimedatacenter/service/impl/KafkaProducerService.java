package com.helei.reaktimedatacenter.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Qualifier("kafkaAdminClient")
    private AdminClient adminClient;


    public CompletableFuture<SendResult<String, String>> sendMessage(String topic, String message) {
        log.debug("Sent message=[{}] to topic=[{}]", message, topic);
        checkAndCreateTopic(topic, 2, (short) 1);
        return kafkaTemplate.send(topic, message);
    }


    public boolean topicExists(String topicName) {
        try {
            Set<String> topics = adminClient.listTopics().names().get();
            return topics.contains(topicName);
        } catch (InterruptedException | ExecutionException e) {
           log.info("check topic exists failed", e);
            return false;
        }
    }

    public void createTopic(String topicName, int numPartitions, short replicationFactor) {
        NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
        try {
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("Topic created: {}", topicName);
        } catch (InterruptedException | ExecutionException e) {
            log.info("Failed to create topic", e);
        }
    }

    public void checkAndCreateTopic(String topicName, int numPartitions, short replicationFactor) {
        if (!topicExists(topicName)) {
            createTopic(topicName, numPartitions, replicationFactor);
        } else {
            log.debug("Topic already exists: {}", topicName);
        }
    }

    public void close() {
        adminClient.close();
    }

}
