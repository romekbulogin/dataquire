FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8085

WORKDIR "/database-manager"
VOLUME "/database-manager/config"

COPY ./build/libs/Database-Manager.jar database-manager.jar
ENTRYPOINT ["java","-jar","database-manager.jar","--spring.config.location=file:./config/application.yml"]