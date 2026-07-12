FROM eclipse-temurin:21
RUN mkdir /opt/server
COPY /build/libs/server-1.0.0.jar /opt/app/app.jar
CMD [ "java", "-jar", "/opt/app/app.jar" ]
LABEL org.opencontainers.image.source=https://github.com/gekoppe/mail-server