FROM adoptopenjdk:openj9

ARG JAR_FILE=build/*.jar

ARG DB_PASSWORD

ADD "./build/libs/PhotoServicePipeline-0.0.1-SNAPSHOT.jar" app.jar

ADD "./photos" /photos

#ADD "C:\caddy\photoService\application-prod.yml" application-prod.yml

CMD "ls"

EXPOSE 7474

ENTRYPOINT ["java", "-XX:+UseSerialGC", "-jar", "app.jar", "--spring.datasource.password=${DB_PASSWORD}"]

#"-Dspring.config.location = application-prod.yml",
