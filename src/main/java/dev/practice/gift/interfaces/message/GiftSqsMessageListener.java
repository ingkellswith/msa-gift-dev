package dev.practice.gift.interfaces.message;


import dev.practice.gift.application.GiftFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiftSqsMessageListener {
    private final GiftFacade giftFacade;

    // amazon sqs 에서 message를 받는 부분
    // annotation만 @SqsListener이지 Http Controller와 구조가 매우 흡사하다.
    // deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS는 fifo queue에서 sqs message를 잘 처리했을 때 삭제한다는 의미
    @SqsListener(value = "msa-dev.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void readMessage(GiftPaymentCompleteMessage message) {
        var orderToken = message.getOrderToken();
        log.info("[GiftSqsMessageListener.readMessage] orderToken = {}", orderToken);
        giftFacade.completePayment(orderToken);
    }
}
