# 베이스 이미지 설정 (JDK 17 사용)
FROM eclipse-temurin:17-jdk-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
# Gradle 빌드 경로는 build/libs/*.jar 입니다.
# (Build Job에서 빌드가 먼저 수행됨)
COPY build/libs/*-SNAPSHOT.jar app.jar

# 실행 포트 노출
EXPOSE 8080

# 컨테이너 시작 명령 (프로파일은 'docker run'에서 환경 변수로 지정됨)
ENTRYPOINT ["java", "-jar", "app.jar"]
