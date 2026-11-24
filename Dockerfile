FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --create-home --uid 10001 evidencevault
COPY target/evidencevault-*.jar app.jar
RUN mkdir -p /var/lib/evidencevault/evidence && chown -R evidencevault:evidencevault /var/lib/evidencevault
USER evidencevault
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-Dspring.profiles.active=prod","-jar","/app/app.jar"]
