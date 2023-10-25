FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8086
ADD ./Query-Executor/build/libs/Query-Executor.jar query-executor.jar
CMD ["java","-jar","query-executor.jar"]