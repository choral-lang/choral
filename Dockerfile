# Build Choral dependencies
FROM maven:3.9.9-amazoncorretto-23 AS build
WORKDIR /choral
COPY . /choral/
RUN mvn clean install

# Unzip artifact
FROM alpine AS unzipper
WORKDIR /tmp
COPY --from=build /choral/dist/target/choral*.zip ./choral.zip
RUN apk add unzip
RUN unzip choral.zip 

# Move libraries and launcher to designated directories
FROM amazoncorretto:23
COPY --from=unzipper /tmp/choral/dist/ /usr/local/lib/choral/
COPY --from=unzipper /tmp/choral/launchers/ /usr/local/bin/
COPY --from=build /choral/dist/target/choral-standalone.jar /usr/local/lib/choral/
RUN chmod +x /usr/local/bin/choral
ENV CHORAL_HOME=/usr/local/lib/choral
