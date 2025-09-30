package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.common.security.refreshToken.repository.BlacklistTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenPurger {

    private final BlacklistTokenRepository blacklistTokenRepository;

    //@Scheduled(fixedRate = 10000) // 테스트용
    @Scheduled(cron = "0 30 4 * * *")
    public void purgeExpiredTokens() {
        log.info("--- Blacklist Token Purging Task Started ---");

        int deletedCount = blacklistTokenRepository.deleteExpiredTokens(LocalDateTime.now());

        if(deletedCount > 0) {
            log.info("--- Blacklist Token Purging Task Completed : {}개 토큰 삭제 완료  ---",deletedCount);
        }else{
            log.info("--- Blacklist Token Purging Task Completed: 삭제된 토큰 없음 ---");
        }

    }
}
