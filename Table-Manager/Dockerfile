FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8087
ADD ./Table-Manager/build/libs/Table-Manager-0.0.1.jar table-manager.jar
CMD ["java","-jar","table-manager.jar"]