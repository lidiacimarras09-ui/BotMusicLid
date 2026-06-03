FROM openjdk:21
COPY BotMusicLid.jar app.jar
CMD ["java", "-jar", "app.jar"]