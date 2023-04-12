# https://www.baeldung.com/spring-boot-docker-start-with-profile

FROM azul/zulu-openjdk:11
ARG PROFILE
ARG SECRET
ENV PROFILE=${PROFILE}
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}, secret", "-jar","/app.jar"]