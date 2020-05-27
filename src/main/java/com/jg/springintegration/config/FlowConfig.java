package com.jg.springintegration.config;

import com.jg.springintegration.model.FlowRequestModel;
import com.jg.springintegration.service.MessageLogger;
import com.jg.springintegration.service.WireTapLogger;
import com.jg.springintegration.transformer.FileToTextTranformer;
import com.jg.springintegration.transformer.MailToTextTransformer;
import com.jg.springintegration.transformer.WebToTextTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.graph.IntegrationGraphServer;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@RequiredArgsConstructor
@EnableIntegrationGraphController
public class FlowConfig {

    public static final String FILE_SOURCING_CHANNEL = "fileSourcingChannel";
    public static final String WEB_SOURCING_CHANNEL = "webSourcingChannel";
    public static final String MAIL_SOURCING_CHANNEL = "mailSourcingChannel";

    public static final String START_MESSAGE_PROCESS_CHANNEL = "startMessageProcessChannel";
    public static final String WIRETAP_CHANNEL = "wireTapChannel";

    private final FileToTextTranformer fileToTextTranformer;
    private final WebToTextTransformer webToTextTransformer;
    private final MailToTextTransformer mailToTextTransformer;

    private final MessageLogger messageLogger;
    private final WireTapLogger wireTapLogger;

    /** CONFIG **/

    @Bean
    public IntegrationGraphServer integrationGraphServer() {
        return new IntegrationGraphServer();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(final CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }

    /** INBOUND **/
    // File
    @Bean
    @InboundChannelAdapter(value = FILE_SOURCING_CHANNEL, poller = @Poller(fixedDelay = "5000", maxMessagesPerPoll = "-1"))
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
    @InboundChannelAdapter(value = MAIL_SOURCING_CHANNEL, poller = @Poller(fixedDelay = "5000", maxMessagesPerPoll = "-1"))
    public MailReceivingMessageSource mailReceivingMessageSource() {
        return new MailReceivingMessageSource(mailReceiver());
    }

    @Bean
    public MailReceiver mailReceiver() {
        final ImapMailReceiver imapMailReceiver = new ImapMailReceiver("imaps://ganni.dow:JohnDoe123!@imap.gmail.com:993/INBOX");
        imapMailReceiver.setShouldMarkMessagesAsRead(false);
        imapMailReceiver.setSimpleContent(true);
        return imapMailReceiver;
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

    @Bean
    public IntegrationFlow mailToMessageFlow() {
        return IntegrationFlows.from(MAIL_SOURCING_CHANNEL)
                .transform(mailToTextTransformer)
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
