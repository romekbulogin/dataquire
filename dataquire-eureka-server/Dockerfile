FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8761
WORKDIR "/dataquire-eureka-server"
VOLUME "/dataquire-eureka-server/config"
COPY ./build/libs/Dataquire-Eureka-Server.jar dataquire-eureka-server.jar
ENTRYPOINT ["java","-jar","dataquire-eureka-server.jar","--spring.config.location=file:./config/application.yml"]