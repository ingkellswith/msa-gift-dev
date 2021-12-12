msa-gift-dev
==============
(수강 강의 - 패스트캠퍼스, The RED : 비즈니스 성공을 위한 Java/Spring 기반 서비스 개발과 MSA 구축 by 이희창)

repository 주제 : ddd, msa 구현

msa-gift-dev에서는 [msa-order-dev](https://github.com/ingkellswith/msa-order-dev) 에서 구현한 주문 서비스와 통신합니다

# 선물하기 기능 기본 설계
1. msa-order-dev repository에서 파트너, 상품, 주문(결제까지) 도메인 서비스를 구현했다
2. msa를 위해 선물 도메인 서비스는 이 repository에서 구현하고, msa-order-dev에 있는 주문, 결제 처리가 필요한 상황
3. 선물하기 주문 작업은 1) 선물하기 주문 정보 등록 2) 선물하기 요청에 맞는 주문 정보 생성 3) 결제 처리 4) 선물 지급 등으로 구성
4. 따라서 2), 3)은 msa-order-dev에서 처리 1), 4)는 msa-gift-dev에서 처리
5. 이미 운영중인 주문 도메인 서비스에 결제 정보 생성과 결제 처리를 위한 메시지를 정의해야 한다.
- 일반 주문과 다르게 선물하기 주문은 결제 과정에서는 배송지 정보가 fix 되지 않기 때문에, 기존의 일반 주문 메시지와는 약간 다른 메시지 형태를 가진다
- 그런데 배송지 정보를 필수로 가져가는 식의 @Embeddable fragment로 구현한 상황
- 문제 상황 해결을 위한 방안 3가지 존재
- a) 기존 msa-order-dev의 api변경
- b) 기존 msa-order-dev에서 v2로 신규 api생성
- c) 신규 msa-gift-dev에서 배송지 정보에 선물하기 기능에서만 사용할 식별가능한 temp값 넣기
- 이 프로젝트에서는 c)방안을 채택해 진행 
- c)방안은 기존 서비스들의 로직을 변경할 필요가 없다는 장점이 존재
- 메시지 큐를 사용하지 않을 경우 양방향 의존 관계가 생김  
- 메시지 큐를 사용함으로써 서버 간 결합 관계 느슨
- 메시지 큐에 성공 메시지만 보내면 성공 메시지를 수신할 대상은 신경쓸 필요가 없다
- 선물하기 서버가 장애 상황이라도 서비스 복구 후에 메시지 큐의 메시지를 읽어 주문 성공 처리가능  

(메시지 큐를 사용하지 않을 때의 구조)  

![nosqs](https://user-images.githubusercontent.com/55550753/136477754-fd6e5b52-9a73-4406-bfe2-a014464fbd2c.PNG)  
7. 따라서 메시지 기반 비동기 통신을 사용해 '주문 서비스 → 선물하기 서비스'의 방향성을 제거한다  

![withsqs](https://user-images.githubusercontent.com/55550753/136477898-f2be5514-e759-4522-b6bb-a6003b96c871.PNG)  

# 메시지 큐를 이용한 msa설계

- sequence diagram : 선물 구매시  

![msawithsqs](https://user-images.githubusercontent.com/55550753/136494089-eceb77ca-4e4d-40d9-9921-7e798e7c72bd.png)  

- sequence diagram : 선물 수락시  

![msa-gift-accept](https://user-images.githubusercontent.com/55550753/136478722-c8d86ace-a1c2-465a-bb16-9d5f9febd0eb.PNG)  

- sequence diagram : 선물 거절시  

![msa-gift-deny](https://user-images.githubusercontent.com/55550753/136478912-9638ca3b-e51b-499c-ab79-45d55b09e8ad.PNG)  

# 참고 : kafka(이 프로젝트에서 사용하지 않음)
#### kafka 의 multi consumer 구조를 활용한다면, 주문 서비스를 활용하는 클라이언트가 지속적으로 추가되어도 모든 클라이언트가 결제 성공 메시지를 전달 받을 수 있다  
![kafkabasic](https://user-images.githubusercontent.com/55550753/136479313-dc930dc2-47ac-4f06-8c4b-0d6387e722c9.PNG)

- 다만 메시지 내부에는 해당 주문의 클라이언트가 어디인지를 구분하는 구분값이 있
  어야하고, 각 클라이언트는 자신의 메시지인지를 확인하는 필터링 로직이 추가되어
  야 하는 단점이 있다
- 예를 들어 주식 서비스에서 주문 서비스를 통해 결제 처리를 요청했고 주문 서비스가 결제 성공 메시지를 발행한다면, 해당 메시지 내부에는
  serviceType="주식" 과 같은 프로퍼티가 추가되어야 한다
- kafka 의 주문 성공 topic 을 consuming 하는 다양한 서비스들은 동일한 메시지를 수신할 것이기 때문에 주식 서비스 이외에는 결제 성공 로직을 처리하지
  않도록 필터링 로직을 구현해야 한다




