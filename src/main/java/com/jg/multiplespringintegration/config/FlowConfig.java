package com.jg.multiplespringintegration.config;

import com.jg.multiplespringintegration.model.FlowRequestModel;
import com.jg.multiplespringintegration.service.MessageLogger;
import com.jg.multiplespringintegration.service.WireTapLogger;
import com.jg.multiplespringintegration.transformer.FileToTextTranformer;
import com.jg.multiplespringintegration.transformer.WebToTextTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.messaging.MessageChannel;

import java.io.File;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@RequiredArgsConstructor
public class FlowConfig {

    public static final String FILE_SOURCING_CHANNEL = "fileSourcingChannel";
    public static final String WEB_SOURCING_CHANNEL = "webSourcingChannel";
    public static final String WEB_REPLY_CHANNEL = "webReplyChannel";

    public static final String START_MESSAGE_PROCESS_CHANNEL = "startMessageProcessChannel";
    public static final String WIRETAP_CHANNEL = "wireTapChannel";

    private final FileToTextTranformer fileToTextTranformer;
    private final WebToTextTransformer webToTextTransformer;
    private final MessageLogger messageLogger;
    private final WireTapLogger wireTapLogger;

    /** INBOUND **/
    // File
    @Bean
    @InboundChannelAdapter(value = FILE_SOURCING_CHANNEL, poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "-1"))
    public FileReadingMessageSource fileReadingMessageSource() {
        final FileReadingMessageSource fileReader = new FileReadingMessageSource();
        fileReader.setDirectory(new File("source"));
        return fileReader;
    }

    // Http
    @Bean
    public HttpRequestHandlingMessagingGateway httpRequestHandlingMessagingGateway() {
        HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway(false);
        gateway.setRequestMapping(requestMapping());
        gateway.setRequestPayloadType(ResolvableType.forClass(FlowRequestModel.class));
        gateway.setRequestChannelName(WEB_SOURCING_CHANNEL);
        gateway.setReplyChannel(webReplyChannel());
        return gateway;
    }

    @Bean
    public RequestMapping requestMapping() {
        RequestMapping requestMapping = new RequestMapping();
        requestMapping.setPathPatterns("/test");
        requestMapping.setMethods(POST);
        return requestMapping;
    }

    @Bean
    public MessageChannel webReplyChannel() {
        return MessageChannels.direct(WEB_REPLY_CHANNEL)
                .get();
    }

    /** TRANSFORMERS **/
    // File
    @Bean
    public IntegrationFlow fileToMessageFlow() {
        return IntegrationFlows.from(FILE_SOURCING_CHANNEL)
                .transform(fileToTextTranformer)
                .channel(START_MESSAGE_PROCESS_CHANNEL)
                .get();
    }

    // Http
    @Bean
    public IntegrationFlow httpToMessageFlow() {
        return IntegrationFlows.from(WEB_SOURCING_CHANNEL)
                .transform(webToTextTransformer)
                .channel(START_MESSAGE_PROCESS_CHANNEL)
                .get();
    }

    /** OUTBOUNDS **/
    // Logging
    @Bean
    public IntegrationFlow messageProcessFlow() {
        return IntegrationFlows.from(START_MESSAGE_PROCESS_CHANNEL)
                .wireTap(WIRETAP_CHANNEL)
                .handle(messageLogger, "handleMessage")
                .get();
    }

    /**
     * WireTapping intercepts the flow where it is called (.wireTap(...)) without disrupting the original flow.
     */
    @Bean
    public IntegrationFlow wireTapFlow() {
        return IntegrationFlows.from(WIRETAP_CHANNEL)
                .handle(wireTapLogger, "handleMessage")
                .get();
    }

}
