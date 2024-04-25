FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8086
WORKDIR "/query-executor"
VOLUME "/query-executor/config"
COPY ./build/libs/Query-Executor.jar query-executor.jar
ENTRYPOINT ["java","-jar","query-executor.jar","--spring.config.location=file:./config/application.yml"]