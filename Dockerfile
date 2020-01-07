FROM 221489699002.dkr.ecr.eu-central-1.amazonaws.com/sportalliance/java-adoptopenjdk:jdk-11.0.1.13-ubuntu-slim

ADD ./target/ml-bouncing-racoon*-exec.jar ml-bouncing-racoon.jar
ADD run.sh run.sh
RUN chmod +x run.sh

USER ml
EXPOSE 8122

CMD ["./run.sh"]
