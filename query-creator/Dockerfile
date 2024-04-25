FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8091
WORKDIR "/query-creator"
VOLUME "/query-creator/config"
ADD ./build/libs/Query-Creator.jar query-creator.jar
CMD ["java","-jar","query-creator.jar","--spring.config.location=file:./config/application.yml"]