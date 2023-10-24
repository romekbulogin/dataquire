FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8085
ADD ./Database-Manager/build/libs/Database-Manager-0.0.1.jar database-manager.jar
CMD ["java","-jar","database-manager.jar"]