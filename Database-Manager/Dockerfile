FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8085
ADD ./Database-Manager/build/libs/Database-Manager.jar database-manager.jar
CMD ["java","-jar","database-manager.jar"]