package io.corbel.evci.converter;

import java.io.IOException;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.corbel.evci.model.EworkerMessage;

public class DomainObjectJsonMessageConverter extends Jackson2JsonMessageConverter {

    private static final Logger LOG = LoggerFactory.getLogger(DomainObjectJsonMessageConverter.class);

    private final Type domainObjectClass;
    private final ObjectMapper objectMapper;

    public DomainObjectJsonMessageConverter(Type domainObjectClass, ObjectMapper objectMapper) {
        this.domainObjectClass = domainObjectClass;
        this.objectMapper = objectMapper;
        setJsonObjectMapper(objectMapper);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        Object content = null;
        if (message.getClass().equals(domainObjectClass)) {
            return message;
        }
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String contentType = properties.getContentType();
            if (contentType != null && contentType.contains("json")) {
                String encoding = properties.getContentEncoding();
                if (encoding == null) {
                    encoding = getDefaultCharset();
                }
                try {
                    JavaType targetJavaType = objectMapper.getTypeFactory().constructType(domainObjectClass);
                    if (EworkerMessage.class.equals(targetJavaType.getParameterSource())) {
                        content = convertBytesToObject(message.getBody(), encoding, targetJavaType);
                    } else {
                        JavaType eworkerMsgJavaType = objectMapper.getTypeFactory().constructType(EworkerMessage.class);
                        EworkerMessage eworkerMessage = (EworkerMessage) convertBytesToObject(message.getBody(), encoding,
                                eworkerMsgJavaType);
                        content = eworkerMessage.getContent() != null
                                ? objectMapper.convertValue(eworkerMessage.getContent(), targetJavaType)
                                : convertBytesToObject(message.getBody(), encoding, targetJavaType);
                    }
                } catch (IOException e) {
                    throw new MessageConversionException("Failed to convert message content", e);
                }
            } else {
                LOG.warn("Could not convert incoming message with content-type [" + contentType + "]");
                throw new MessageConversionException("Failed to convert message content. Unknown content-type: [" + contentType + "]");
            }
        }
        if (content == null) {
            content = message.getBody();
        }
        return content;
    }

    private Object convertBytesToObject(byte[] body, String encoding, JavaType targetJavaType) throws IOException {
        String contentAsString = new String(body, encoding);
        return objectMapper.readValue(contentAsString, targetJavaType);
    }

}
