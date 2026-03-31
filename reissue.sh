#!/bin/bash
set -e #bash에서 에러 발생하면 즉시 종료

#1. .env 경로
ENV_PATH="xxx/xxxx/xx/.env"

#2. 변수 추출
if [ -f "$ENV_PATH" ]; then
  export $(grep -v '^#' "$ENV_PATH" | xargs)
else
  echo ".env file not found at $ENV_PATH"
  exit 1
fi

#3. 변수 사용
LIVE_DIR="/etc/letsencrypt/live/$DOMAIN"

#4. .pem -> .p12 변환
openssl pkcs12 -export \
  -in "$LIVE_DIR/fullchain.pem" \
      -inkey "$LIVE_DIR/privkey.pem" \
      -out "$LIVE_DIR/keystore.p12" \
      -name tomcat \
      -passout "pass:$SSL_PASSWORD"

#5. 백엔드 컨테이너 재시작
docker restart incheon_mate-backend


#+++++++++++++++++++ Nginx로 배포시 아래 명령어를 사용 ++++++++++++++++++
##인증서 갱신 시도
#certbot renew --quiet
#
##Nginx 설정 및 인증서 무중단 재로드
#docker exec incheon_mate-nginx nginx -s reload