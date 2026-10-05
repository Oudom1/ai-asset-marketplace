FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
COPY api/pom.xml api/pom.xml
RUN mvn -q -pl api -am dependency:go-offline
COPY backend/src backend/src
COPY api/src api/src
RUN mvn -q -DskipTests -pl api -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api/target/marketplace-api-0.0.1-SNAPSHOT.jar /app/app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
