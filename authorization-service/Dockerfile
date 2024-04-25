FROM bellsoft/liberica-openjdk-alpine:20
EXPOSE 8080

WORKDIR "/authorization-service"
VOLUME "/authorization-service/config"

COPY ./build/libs/Authorization-Service.jar authorization-service.jar
ENTRYPOINT ["java","-jar","authorization-service.jar","--spring.config.location=file:./config/application.yml"]