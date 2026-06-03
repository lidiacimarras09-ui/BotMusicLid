FROM eclipse-temurin:21-jre

COPY target/BotMusicLidLinux.jar app.jar

CMD ["java", "-jar", "app.jar"]