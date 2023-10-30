FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8080
EXPOSE 587

WORKDIR "/authorization-service"
VOLUME "/authorization-service/config"

COPY ./build/libs/Authorization-Service.jar authorization-service.jar
ENTRYPOINT ["java","-jar","authorization-service.jar","--spring.config.location=file:./config/application.yml"]