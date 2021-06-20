# 스프링으로 하는 마이크로서비스 구축

## 스프링 이니셜라이저로 골격 코드 생성


## 그래들에 멀티 프로젝트 빌드 설정

## RESTful API 추가

- api, utils 프로젝트 추가
  1. API를 정의를 배ㅣ할 별도의 그래들 프로젝트 생성
    - 자바 인터페이스를 사용해 RESTful API를 설명
    - 모델 클래스로 API 요청 및 응답에 사용할 데이터를 정의
  2. 전체 마이크로서비스가 공유하는 헬퍼 클래스를 배치할 util 프로젝트 생
  
- api 프로젝트
  - main 애플리케이션 없이 라이브러리로만 구성
  
- utils 프로젝트
  - 예외 클래스
    - InvalidInputException
    - NoutFoundException
  - 유틸리티 클래스
    - ServiceUtil - 호스트이름, IP 주소, 포트를 검색
    - GlobalControllerExceptionHandler - 자바 예외를 적절한 HTTP 상태코드와 매핑
    - HttpErrorInfo - 자바 예외를 적절한 HTTP 상태코드와 매핑
    
# 도커를 사용한 마이크로서비스 배포

## 도커로 단일 마이크로서비스 실행

```shell script
./gradlew :microservices:product-service:build
cd microservices/product-service
docker build -t product-service .
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product-service
curl localhost:8080/product/3
```
## 도커 컴포즈를 사용한 마이크로서비스 환경 관리
```shell script
./gradlew build
docker compose build
docker compose up -d
docker compose down
```
## 도커 컴포즈를 이용한 마이크로서비스 환경 테스트
```shell script
./test-em-all.bash start stop
```

# 영속성 추가

## 진행 방향 확인

- 프로토콜 계층
  - 공통 클래스인 GlobalControllerExceptionHandler와 RestController 애노테이
- 서비스 계층
  - 마이크로서비스의 주요 기능
- 통합 계층
  - product-composite 서비스에는 세 가지 핵심 마이크로서비스와 통신하는 계층
- 영속성 계층
  - 모든 마이크로서비스에서 자체 데이터베이스와 통신

## 엔티티 클래스를 사용해 데이터 저장
