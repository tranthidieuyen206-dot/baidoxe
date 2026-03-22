# Sử dụng JDK 17 (hoặc phiên bản phù hợp với code của thầy)
FROM eclipse-temurin:17-jdk-jammy as build

# Copy code vào trong container và build (giả sử bạn dùng Maven)
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Chạy chương trình
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]