FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8081

WORKDIR "/dataquire-gateway"
VOLUME "/dataquire-gateway/config"

COPY ./build/libs/Dataquire-Gateway.jar dataquire-gateway.jar
ENTRYPOINT ["java","-jar","dataquire-gateway.jar","--spring.config.location=file:./config/application.yml"]