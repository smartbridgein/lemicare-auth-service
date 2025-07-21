FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar file
COPY target/cosmicdoc-auth-service-0.0.1-SNAPSHOT.jar app.jar

# Hard-code port 8081 for Cloud Run
ENV PORT=8081
ENV SPRING_PROFILES_ACTIVE=cloud

# Expose port 8081
EXPOSE 8081

# Run with explicit debug settings
CMD ["java", "-Dserver.port=8081", "-Dspring.profiles.active=cloud", "-XX:InitialRAMPercentage=50", "-XX:MaxRAMPercentage=70", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
