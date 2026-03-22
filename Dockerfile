# ===== Stage 1: Build =====
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Tải PostgreSQL JDBC driver
RUN apt-get update && apt-get install -y wget && \
    wget -O postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.3.jar

# Copy source code
COPY . .

# Compile Java source
RUN javac -cp postgresql-42.7.3.jar -d out $(find src -name "*.java")

# ===== Stage 2: Run =====
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy compiled classes + JDBC driver
COPY --from=build /app/out ./out
COPY --from=build /app/postgresql.jar ./postgresql.jar

# Mở port (nếu server dùng)
EXPOSE 8080

# Chạy chương trình
ENTRYPOINT ["java", "-cp", "out:postgresql.jar", "Server1.NewServer1"]