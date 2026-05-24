package com.example.IncheonMate.common.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final StringRedisTemplate stringRedisTemplate;

    //컨트롤러에서 RT:xxx는 나중에 삭제함
    //message_store:xxx는 삭제해도 되는지 모르겠음
    public void removeGuestInfo(String identifier){

        List<String> keysToDelete = new ArrayList<>();

        // 1. 고정 키 정의 및 추가
        keysToDelete.add("GUEST_CHAT:" + identifier);
        keysToDelete.add("GUEST_COUNT:" + identifier);
        keysToDelete.add("GUEST_PROFILE:" + identifier);

        //2. 가변 키 정의
        String viewPattern = "history:view:" + identifier + ":*";

        // 3. SCAN 명령어를 통해 Redis 성능 저하 없이 패턴 매칭 키 탐색
        ScanOptions options = ScanOptions.scanOptions()
                .match(viewPattern)
                .count(100) // 한 번에 조회할 갯수 (Redis 성능 최적화)
                .build();

        //4. 가변 키를 keysToDelete Array에 추가
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keysToDelete.add(cursor.next());
            }
        } catch (Exception e) {
            log.error("[Auth] [Logout] [Redis] 가변 키 SCAN 중 에러 발생: {}", e.getMessage(), e);
        }

        //5. 게스트와 관련된 모든 키 삭제
        if (!keysToDelete.isEmpty()) {
            try {
                // delete(Collection<K> keys)는 내부적으로 단일 파이프라인/멀티 삭제 처리가 되어 효율적입니다.
                Long deletedCount = stringRedisTemplate.delete(keysToDelete);
                log.info("[Redis] 게스트 데이터 일괄 삭제 완료. (요청: {}개, 실제 삭제: {}개)", keysToDelete.size(), deletedCount);
            } catch (Exception e) {
                log.error("[Redis] 키 삭제 실행 중 실패: {}", e.getMessage(), e);
            }
        } else {
            log.info("[Redis] 삭제할 게스트 데이터가 없습니다.");
        }

    }
}
