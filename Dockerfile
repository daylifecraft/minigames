FROM registry-1.docker.io/library/eclipse-temurin:21.0.2_13-jre-alpine

# Minecraft port
EXPOSE 25565/tcp
# Prometheus metrics port
EXPOSE 9119/tcp

USER nobody:nobody
WORKDIR /opt/minigames

ENTRYPOINT ["/opt/java/openjdk/bin/java", "-XX:+AlwaysPreTouch", "-XX:+ParallelRefProcEnabled", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:G1HeapRegionSize=4M", "-XX:MaxInlineLevel=15", "-jar", "runner.jar"]

COPY --chown=nobody:nobody --chmod=700 ["./runner/build/libs", "/opt/minigames"]
