FROM quay.io/sportalliance/java-adoptopenjdk:jdk-11.0.1.13-ubuntu-slim

ADD ./target/ml-bouncing-racoon*.jar ml-bouncing-racoon.jar
ADD ../run.sh run.sh
RUN chmod +x run.sh

USER ml
EXPOSE 8107

CMD ["./run.sh"]
