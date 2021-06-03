FROM maven:3-jdk-14 as builder
COPY ./ build
WORKDIR build
RUN mvn clean package -DskipTests

FROM openjdk:14-jdk-alpine
VOLUME /tmp
COPY twilio /twilio
COPY --from=builder build/target/SmsTrackerMicroservice-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
