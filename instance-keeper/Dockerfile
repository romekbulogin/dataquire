FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8084
WORKDIR "/instance-keeper"
VOLUME "/instance-keeper/config"
COPY ./build/libs/Instance-Keeper.jar instance-keeper.jar
ENTRYPOINT ["java","-jar","instance-keeper.jar","--spring.config.location=file:./config/application.yml"]