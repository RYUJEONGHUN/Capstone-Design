#!/bin/bash
set -e #bash에서 에러 발생하면 즉시 종료

#1. .env 경로
ENV_PATH="/home/ubuntu/Capstone-Design/.env"

#2. 변수 추출
if [ -f "$ENV_PATH" ]; then
  export $(grep -v '^#' "$ENV_PATH" | xargs)
else
  echo ".env file not found at $ENV_PATH"
  exit 1
fi

#인증서 갱신 시도
/usr/bin/certbot renew --quiet

#Nginx 설정 및 인증서 무중단 재로드
#Spring boot container는 재시작할 필요 없이 Nginx만 리로드하여 새 인증서를 적용
docker exec incheon_mate-nginx nginx -s reload

echo "SSL 인증서 재발급 및 Nginx reload 성공"
