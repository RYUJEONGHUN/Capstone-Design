# 1. 빌드 단계
FROM amazoncorretto:17 AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 윈도우/맥 호환성을 위해 실행 권한 부여
RUN chmod +x ./gradlew
# 빌드 실행 (테스트 제외)
RUN ./gradlew bootJar -x test

# 2. 실행 단계
FROM amazoncorretto:17
WORKDIR /app
# 빌드 단계에서 만든 jar 파일 가져오기
COPY --from=builder /app/build/libs/*.jar app.jar

# 타임존 설정 (한국 시간)
ENV TZ=Asia/Seoul

ENTRYPOINT ["java", "-jar", "app.jar"]