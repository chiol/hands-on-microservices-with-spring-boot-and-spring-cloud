#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.5.1.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=product-service \
--package-name=io.github.chiol.microservices.core.product \
--groupId=io.github.chiol.microservices.core.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-service

spring init \
--boot-version=2.5.1.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=review-service \
--package-name=io.github.chiol.microservices.core.review \
--groupId=io.github.chiol.microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

spring init \
--boot-version=2.5.1.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=recommendation-service \
--package-name=io.github.chiol.microservices.core.recommendation \
--groupId=io.github.chiol.microservices.core.recommendation \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
recommendation-service

spring init \
--boot-version=2.5.1.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=product-composite-service \
--package-name=io.github.chiol.microservices.composite.product \
--groupId=io.github.chiol.microservices.composite.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-composite-service

cd ..

mkdir spring-cloud
cd spring-cloud

spring init \
--boot-version=2.5.1.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=eureka-server \
--package-name=io.github.chiol.springcloud.eurekaserver \
--groupId=io.github.chiol.springcloud.eurekaserver \
--version=1.0.0-SNAPSHOT \
eureka-server