package dev.practice.gift.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsSqsConfig {
    @Value("${cloud.aws.access-key}")
    private String awsAccessKey;

    @Value("${cloud.aws.secret-key}")
    private String awsSecretKey;

    // 1. configuration : 개발자가 외부 라이브러리 또는 설정을 위한 클래스를 Bean으로 등록할 때 @Bean 어노테이션을 활용
    // 리턴 객체는 스프링 빈으로 사용되며, 빈 이름은 메소드이름과 같다.
    // 사실 @Configuration 이든 @Component 든 상관은 없지만 이름으로 구별하는 용도로 쓰인다고 한다.
    // 서비스에만 @Service를 쓰는 것과 같은 이치.
    // 2. 이렇게 등록된 빈은 개발자가 직접 사용할수도, 라이브러리가 사용하고 개발자는 직접 사용하지 않을 수도 있다.
    // 즉, yml파일로 설정을 작성하듯 @Configuration으로 설정을 작성하고, 코드적으로는 개발자가 직접 관여하지 않을 수도 있다는 것이다.
    @Bean
    public AmazonSQSAsync amazonSQSAsync() {
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(awsAccessKey, awsSecretKey)
        );

        return AmazonSQSAsyncClientBuilder
                .standard()
                .withRegion(Regions.AP_NORTHEAST_2)
                .withCredentials(awsCredentialsProvider)
                .build();
    }
}
