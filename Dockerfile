FROM openjdk:17-alpine
ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY target/habr-abbr-scanner.jar .
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "habr-abbr-scanner.jar"]