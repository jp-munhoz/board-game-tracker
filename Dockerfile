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
# TieredStopAtLevel=1 foi removido de proposito: ele desligava o compilador C2 (o que
# otimiza codigo "quente") por toda a vida do processo, nao so no boot - trocava alguns
# segundos de startup por performance de runtime pior o tempo todo. Sem essa flag a JVM
# usa tiered compilation completo (padrao), que warma e fica mais rapida com o uso.
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Xss512k -XX:MaxMetaspaceSize=128m -jar app.jar"]
