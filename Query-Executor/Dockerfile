FROM openjdk:19
EXPOSE 8086
ADD ./Query-Executor/build/libs/Query-Executor-0.0.1.jar query-executor.jar
CMD ["java","-jar","query-executor.jar"]