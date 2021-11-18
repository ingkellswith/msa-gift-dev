package dev.practice.gift.interfaces.api;

import dev.practice.gift.application.GiftFacade;
import dev.practice.gift.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gifts")
public class GiftApiController {
    private final GiftFacade giftFacade;
    private final GiftDtoMapper giftDtoMapper;

    // 선물하기 서비스 내부 동작
    @GetMapping("/{giftToken}")
    public CommonResponse retrieveOrder(@PathVariable String giftToken) {
        var giftInfo = giftFacade.getOrder(giftToken);
        return CommonResponse.success(giftInfo);
    }

    // 주문하기 서비스 직접 호출, retrofit 사용
    @PostMapping
    public CommonResponse registerOrder(@RequestBody @Valid GiftDto.RegisterReq request) {
        var command = giftDtoMapper.of(request);
        var giftInfo = giftFacade.registerOrder(command);
        return CommonResponse.success(new GiftDto.RegisterRes(giftInfo));
    }

    // 선물하기 서비스 내부 동작
    @PostMapping("/{giftToken}/payment-processing")
    public CommonResponse requestPaymentProcessing(@PathVariable String giftToken) {
        giftFacade.requestPaymentProcessing(giftToken);
        return CommonResponse.success("OK");
    }

    // 주문하기 서비스 직접 호출, retrofit 사용
    @PostMapping("/{giftToken}/accept-gift")
    public CommonResponse acceptGift(
            @PathVariable String giftToken,
            @RequestBody @Valid GiftDto.AcceptGiftReq request
    ) {
        var acceptCommand = giftDtoMapper.of(giftToken, request);
        giftFacade.acceptGift(acceptCommand);
        return CommonResponse.success("OK");
    }
}
