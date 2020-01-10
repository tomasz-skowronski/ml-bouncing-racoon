FROM  docker.pkg.github.com/sportalliance/spa-docker-base-images/adoptopenjdk:jdk-11

ADD ./target/ml-bouncing-racoon*-exec.jar ml-bouncing-racoon.jar
ADD run.sh run.sh
RUN chmod +x run.sh

USER ml
EXPOSE 8122

CMD ["./run.sh"]
