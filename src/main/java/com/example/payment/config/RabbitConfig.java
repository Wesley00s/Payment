package com.example.payment.config;

import com.example.payment.dto.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_PAYMENT = "payment_queue";
    public static final String EXCHANGE_PAYMENT_DLX = "payment_dlx";
    public static final String QUEUE_PAYMENT_DLQ = "payment_dlq";
    public static final String PAYMENT_EXCHANGE = "payment-events-exchange";

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new MessageConverter() {
            @Override
            @NullMarked
            public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
                try {
                    byte[] bytes = objectMapper.writeValueAsBytes(object);
                    messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    messageProperties.setContentEncoding("UTF-8");
                    messageProperties.setContentLength(bytes.length);

                    messageProperties.setHeader("__TypeId__", object.getClass().getName());

                    return new Message(bytes, messageProperties);
                } catch (Exception e) {
                    throw new MessageConversionException("Erro ao converter para JSON", e);
                }
            }

            @Override
            @NullMarked
            public Object fromMessage(Message message) throws MessageConversionException {
                try {
                    String json = new String(message.getBody(), StandardCharsets.UTF_8);

                    String typeId = message.getMessageProperties().getHeader("__TypeId__");

                    if (typeId != null && typeId.contains("PaymentRequest")) {
                        return objectMapper.readValue(json, PaymentRequest.class);
                    }

                    if (typeId != null) {
                        try {
                            Class<?> targetClass = Class.forName(typeId);
                            return objectMapper.readValue(json, targetClass);
                        } catch (ClassNotFoundException e) {
                            return objectMapper.readTree(json);
                        }
                    } else {
                        return objectMapper.readTree(json);
                    }
                } catch (Exception e) {
                    throw new MessageConversionException("Erro ao converter JSON para Objeto", e);
                }
            }
        };
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT)
                .withArgument("x-dead-letter-exchange", EXCHANGE_PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_PAYMENT_DLQ)
                .build();
    }

    @Bean
    public DirectExchange paymentDlx() {
        return new DirectExchange(EXCHANGE_PAYMENT_DLX);
    }

    @Bean
    public Queue paymentDlq() {
        return QueueBuilder.durable(QUEUE_PAYMENT_DLQ).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(paymentDlq())
                .to(paymentDlx())
                .with(QUEUE_PAYMENT_DLQ);
    }
}