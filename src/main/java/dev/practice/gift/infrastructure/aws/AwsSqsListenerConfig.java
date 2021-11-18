package dev.practice.gift.infrastructure.aws;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class AwsSqsListenerConfig {
    // 1. configuration : 개발자가 외부 라이브러리 또는 설정을 위한 클래스를 Bean으로 등록할 때 @Bean 어노테이션을 활용
    // 리턴 객체는 스프링 빈으로 사용되며, 빈 이름은 메소드이름과 같다.
    // 사실 @Configuration 이든 @Component 든 상관은 없지만 이름으로 구별하는 용도로 쓰인다고 한다.
    // 서비스에만 @Service를 쓰는 것과 같은 이치.
    // 2. 이렇게 등록된 빈은 개발자가 직접 사용할수도, 라이브러리가 사용하고 개발자는 직접 사용하지 않을 수도 있다.
    // 즉, yml파일로 설정을 작성하듯 @Configuration으로 설정을 작성하고, 코드적으로는 개발자가 직접 관여하지 않을 수도 있다는 것이다.
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSQSAsync) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSQSAsync);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(
            SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory,
            QueueMessageHandler queueMessageHandler,
            ThreadPoolTaskExecutor messageThreadPoolTaskExecutor
    ) {
        SimpleMessageListenerContainer container = simpleMessageListenerContainerFactory.createSimpleMessageListenerContainer();
        container.setMessageHandler(queueMessageHandler);
        container.setTaskExecutor(messageThreadPoolTaskExecutor);
        return container;
    }

    @Bean
    @ConditionalOnMissingBean(QueueMessageHandlerFactory.class)
    public QueueMessageHandlerFactory queueMessageHandlerFactory(AmazonSQSAsync amazonSQSAsync) {
        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        factory.setAmazonSqs(amazonSQSAsync);

        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setStrictContentTypeMatch(false);
        factory.setArgumentResolvers(Collections.singletonList(new PayloadMethodArgumentResolver(messageConverter)));
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(QueueMessageHandler.class)
    public QueueMessageHandler queueMessageHandler(QueueMessageHandlerFactory queueMessageHandlerFactory) {
        return queueMessageHandlerFactory.createQueueMessageHandler();
    }

    @Bean
    public ThreadPoolTaskExecutor messageThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("sqs-");
        taskExecutor.setCorePoolSize(8);
        taskExecutor.setMaxPoolSize(100);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
}
