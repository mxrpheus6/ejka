package by.kazachenko.ejka.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange imageExchange(@Value("${rabbitmq.exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue requestQueue(@Value("${rabbitmq.queues.image-processing.name}") String name) {
        return new Queue(name, true);
    }

    @Bean
    public Queue responseQueue(@Value("${rabbitmq.queues.image-responses.name}") String name) {
        return new Queue(name, true);
    }

    @Bean
    public Binding requestBinding(Queue requestQueue,
            DirectExchange imageExchange,
            @Value("${rabbitmq.queues.image-processing.routing-key}") String routingKey) {
        return BindingBuilder.bind(requestQueue).to(imageExchange).with(routingKey);
    }

    @Bean
    public Binding responseBinding(Queue responseQueue,
            DirectExchange imageExchange,
            @Value("${rabbitmq.queues.image-responses.routing-key}") String routingKey) {
        return BindingBuilder.bind(responseQueue).to(imageExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
