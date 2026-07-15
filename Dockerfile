FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
EXPOSE 8080
EXPOSE 8081
ARG JAR_FILE=target/covoitdark-1.0.0.jar
ADD ${JAR_FILE} app.jar
COPY web_ui /web_ui
ENTRYPOINT ["java","-jar","/app.jar"]
