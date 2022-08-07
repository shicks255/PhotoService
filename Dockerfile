FROM adoptopenjdk:openj9

ARG JAR_FILE=build/*.jar

ADD "./build/libs/PhotoServicePipeline-0.0.1-SNAPSHOT.jar app.jar

ADD "./photos" /photos

CMD "ls"

EXPOSE 7474

ENTRYPOINT ["java", "-XX:+UseSerialGC", "-jar", "app.jar"]

