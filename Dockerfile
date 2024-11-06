FROM amazoncorretto:8-alpine
ENV context ""
ENV port 8021
ADD /src/main/resources/application.properties //
ADD /target/corejava1.8-with-maven-template-1.0-SNAPSHOT-jar-with-dependencies.jar //
RUN mkdir /temp
ENTRYPOINT ["java","-jar", "/corejava1.8-with-maven-template-1.0-SNAPSHOT-jar-with-dependencies.jar","--server.port=${port}"]