FROM openjdk:8-jre-alpine
ENV context ""
ADD /src/main/resources/application.properties //

ADD ./target/demo-0.0.1-SNAPSHOT.jar //
ENTRYPOINT ["java", "-jar", "/demo-0.0.1-SNAPSHOT.jar","--server.servlet.context-path=/${context}"]
