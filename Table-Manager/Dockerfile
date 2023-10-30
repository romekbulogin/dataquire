FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8087
WORKDIR "/table-manager"
VOLUME "/table-manager/config"
COPY ./build/libs/Table-Manager.jar table-manager.jar
ENTRYPOINT ["java","-jar","table-manager.jar","--spring.config.location=file:./config/application.yml"]