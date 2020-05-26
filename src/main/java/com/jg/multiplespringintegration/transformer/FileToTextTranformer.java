package com.jg.multiplespringintegration.transformer;

import lombok.SneakyThrows;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class FileToTextTranformer implements GenericTransformer<Message<File>, String> {

    @SneakyThrows
    @Override
    public String transform(final Message<File> message) {
        return Files.readString(Paths.get(message.getPayload().getPath()));
    }

}
