package com.jg.multiplespringintegration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WireTapLogger {

    public void handleMessage(final Message<String> message) {
        log.info("[WireTap] Received message: {}", message.getPayload());
    }
}
