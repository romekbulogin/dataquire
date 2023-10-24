FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8091
ADD ./Query-Creator/build/libs/Query-Creator-0.0.1.jar query-creator.jar
CMD ["java","-jar","query-creator.jar"]