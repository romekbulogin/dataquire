FROM bellsoft/liberica-openjdk-alpine:19.0.2
EXPOSE 8091
ADD ./Query-Creator/build/libs/Query-Creator.jar query-creator.jar
CMD ["java","-jar","query-creator.jar"]