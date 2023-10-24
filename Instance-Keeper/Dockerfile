FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8084
ADD ./Instance-Keeper/build/libs/Instance-Keeper-0.0.1.jar instance-keeper.jar
CMD ["java","-jar","instance-keeper.jar"]