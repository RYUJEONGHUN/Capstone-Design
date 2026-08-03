# 1. 빌드 단계
#FROM amazoncorretto:17 AS builder
#WORKDIR /app
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle .
#COPY settings.gradle .
#COPY src src

# 윈도우/맥 호환성을 위해 실행 권한 부여
#RUN chmod +x ./gradlew
# 빌드 실행 (테스트 제외)
#RUN ./gradlew bootJar -x test


#RUN curl -sS "https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem" > global-bundle.pem \
#    && awk 'split_after == 1 {n++;split_after=0} /-----END CERTIFICATE-----/ {split_after=1}{print > "rds-ca-" n ".pem"}' < global-bundle.pem \
#    && for CERT in rds-ca-*.pem; do \
#         keytool -import -file ${CERT} -alias "${CERT}" -storepass truststore-password -keystore rds-truststore.jks -noprompt; \
#       done


# 2. 실행 단계
#FROM amazoncorretto:17
#WORKDIR /app
# 빌드 단계에서 만든 jar 파일 가져오기
#COPY --from=builder /app/build/libs/*.jar app.jar

# 빌드 단계에서 정상적으로 생성된 JKS 파일 가져오기
#COPY --from=builder /app/rds-truststore.jks /app/rds-truststore.jks

# 타임존 설정 (한국 시간)
#ENV TZ=Asia/Seoul

#ENTRYPOINT ["java", \
#            "-Djavax.net.ssl.trustStore=/app/rds-truststore.jks", \
#            "-Djavax.net.ssl.trustStorePassword=truststore-password", \
#            "-jar", "app.jar"]




## 1. 빌드 단계
#FROM amazoncorretto:17 AS builder
#WORKDIR /app
#
## 소스 코드 및 빌드 도구 복사
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle .
#COPY settings.gradle .
#COPY src src
#
## 윈도우/맥 호환성을 위해 실행 권한 부여 및 빌드 실행 (테스트 제외)
#RUN chmod +x ./gradlew
#RUN ./gradlew bootJar -x test
#
## [핵심 1] 자바 기본 인증서(cacerts)를 작업 폴더로 복사해옵니다.
#RUN cp /usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts ./my-cacerts
#
## [핵심 2] curl로 최신 AWS 인증서를 다운받고, 쪼갠 뒤 기본 인증서에 추가(Append)합니다.
#RUN curl -sS "https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem" > global-bundle.pem \
#    && awk 'split_after == 1 {n++;split_after=0} /-----END CERTIFICATE-----/ {split_after=1}{print > "rds-ca-" n ".pem"}' < global-bundle.pem \
#    && for CERT in rds-ca-*.pem; do \
#         keytool -import -file ${CERT} -alias "${CERT}" -storepass changeit -keystore ./my-cacerts -noprompt; \
#       done
#
## 2. 실행 단계
#FROM amazoncorretto:17
#WORKDIR /app
#
## 빌드 단계에서 만든 jar 파일 가져오기
#COPY --from=builder /app/build/libs/*.jar app.jar
#
## 빌드 단계에서 만든 통합 인증서(my-cacerts) 가져오기
#COPY --from=builder /app/my-cacerts /app/my-cacerts
#
## 타임존 설정 (한국 시간)
#ENV TZ=Asia/Seoul
#
## 통합 인증서와 기본 비밀번호(changeit)를 적용하여 애플리케이션 실행
#ENTRYPOINT ["java", \
#            "-Djavax.net.ssl.trustStore=/app/my-cacerts", \
#            "-Djavax.net.ssl.trustStorePassword=changeit", \
#            "-jar", "app.jar"]

# 1. 빌드 단계
FROM amazoncorretto:17 AS builder
WORKDIR /app

# 빌드에 필요한 Gradle 설정 및 소스코드 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 실행 권한 부여 및 JAR 빌드 (테스트 제외)
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# 2. 실행 단계 (경량화 및 리소스 절약을 위해 alpine 이미지 사용)
FROM amazoncorretto:17-alpine
WORKDIR /app

# 빌드 단계에서 생성된 실행 가능한 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 타임존 설정 (한국 시간)
ENV TZ=Asia/Seoul

# 512MB RAM 환경 최적화 JVM 설정
# -Xms256m: 초기 힙 메모리 256MB
# -Xmx384m: 최대 힙 메모리 384MB (나머지 128MB는 JVM 메타스페이스 및 스택용)
# -XX:+UseSerialGC: 단일 코어/소형 메모리 환경에서 GC 스레드 메모리 오버헤드 최소화
ENV JAVA_OPTS="-Xms256m -Xmx384m -XX:+UseSerialGC"

EXPOSE 8080

# JVM 메모리 옵션을 적용하여 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]