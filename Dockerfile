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
FROM amazoncorretto:23-alpine
ARG CHORAL_LAUNCHER=/usr/local/bin
ARG CHORAL_HOME=/usr/local/lib/choral
COPY --from=unzipper /tmp/choral/dist/ $CHORAL_HOME
COPY --from=unzipper /tmp/choral/launchers/ $CHORAL_LAUNCHER
COPY --from=build /choral/dist/target/choral-standalone.jar $CHORAL_HOME
RUN chmod +x /usr/local/bin/choral
# Persisting the path variable for choral's libraries
ENV CHORAL_HOME=$CHORAL_HOME
