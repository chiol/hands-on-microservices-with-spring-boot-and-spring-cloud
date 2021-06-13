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
