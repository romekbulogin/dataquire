FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8761
ADD ./Dataquire-Eureka-Server/build/libs/Dataquire-Eureka-Server-0.0.1.jar dataquire-eureka-server.jar
CMD ["java","-jar","dataquire-eureka-server.jar"]