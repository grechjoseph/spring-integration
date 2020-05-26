package com.jg.multiplespringintegration.transformer;

import com.jg.multiplespringintegration.model.FlowRequestModel;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class WebToTextTransformer implements GenericTransformer<Message<FlowRequestModel>, String> {

    @Override
    public String transform(final Message<FlowRequestModel> message) {
        return message.getPayload().getText();
    }

}
