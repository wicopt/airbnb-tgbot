FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache maven

COPY . .

RUN mvn package -DskipTests

CMD java -jar target/*.jar