package com.game.subwayworkerserver.profanity;

import com.game.subwayworkerserver.entity.ProfanityWordEntity;
import com.game.subwayworkerserver.repository.ProfanityWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfanityService {

    private final ProfanityWordRepository profanityWordRepository;

    @Getter
    private volatile Set<String> profanitySet = Collections.emptySet();

    @PostConstruct
    public void loadInitialProfanityList() {
        refreshProfanityList();
    }

    /**
     * 주기적으로 DB에서 비속어 목록을 갱신합니다. (예: 매일 오전 3시)
     * 이 메서드가 작동하려면 메인 애플리케이션에 @EnableScheduling을 추가해야 합니다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshProfanityList() {
        List<ProfanityWordEntity> words = profanityWordRepository.findAll();

        // 메모리에 저장할 Set을 생성하고 모든 단어를 소문자로 변환합니다.
        Set<String> newSet = words.stream()
                .map(ProfanityWordEntity::getWord)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        this.profanitySet = newSet;
    }

}