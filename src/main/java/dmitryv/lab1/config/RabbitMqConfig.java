package dmitryv.lab1.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitMqConfig {

    @Bean
    Queue queue() {
        return new org.springframework.amqp.core.Queue("MessageQueue", true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("forumTopicExchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("topic.newMessage");
    }

    @Bean
    Queue emailQueue() {
        return new Queue("EmailQueue", true);
    }

    @Bean
    TopicExchange emailExchange() {
        return new TopicExchange("EmailExchange");
    }

    @Bean
    Binding emailBinding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with("email.send");
    }

}