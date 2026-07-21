# ---- Build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage mais alto que o default (25%) porque o container roda so essa JVM
# (free tier do Render tem pouca RAM, entao vale aproveitar o que tem disponivel).
#
# TieredStopAtLevel=1 mantem a JVM so no compilador C1 (mais leve). Tentamos tirar essa
# flag pra deixar a JVM otimizar mais a fundo (C2) com o tempo, mas no free tier a CPU e
# compartilhada e escassa - o proprio trabalho de compilacao C2 rouba ciclos de CPU de um
# app que raramente tem trafego suficiente pra "esquentar" e aproveitar essa otimizacao.
# Resultado pratico: piorou. Voltando pro C1-only, que e mais previsivel nesse ambiente.
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:MaxMetaspaceSize=128m -jar app.jar"]
