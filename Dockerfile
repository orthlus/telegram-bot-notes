FROM openjdk:17-alpine
ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY target/notes-telegram-bot.jar .
RUN mkdir "files-to-send"
ENTRYPOINT ["java", "-jar", "notes-telegram-bot.jar"]