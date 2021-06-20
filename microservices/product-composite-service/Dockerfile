FROM openjdk:12.0.2

EXPOSE 8080

ADD ./build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar","/app.jar"]