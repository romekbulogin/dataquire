FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8080
EXPOSE 587
ADD ./Authorization-Service/build/libs/Authorization-Service-0.0.1.jar authorization-service.jar
CMD ["java","-jar","authorization-service.jar"]