# JDK 17의 경량화된 이미지 사용
FROM eclipse-temurin:17-jre

# 컨테이너 내부에서 실행될 모든 명령어의 기준폴더를 /app으로 지정
WORKDIR /app

# 앞서 생성된 JAR파일을 컨테이너 내부의 /app 폴더로 복사하고, 파일명을 app.jar롤 바꾼다
COPY build/libs/*.jar app.jar

# JAVA_OPTS라는 환경변수를 빈 문자열로 설정
ENV JAVA_OPTS=""

# 컨테이너가 8080을 사용할 것이라 외부에 알린다 (실제 외부접근 허용하려면, docker run 시 포트 매핑 필요)
EXPOSE 8080

# 컨테이너가 시작될 때 실행될 최종 명령어, 환경변수와 함께 app.jar 파일을 java로 실행
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]