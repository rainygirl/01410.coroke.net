FROM openjdk:18.0.2.1-slim
EXPOSE 8080

ENTRYPOINT ["java"]
CMD ["-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom","-Xms64m","-Xmx384m","-jar","/app/app.jar"]
