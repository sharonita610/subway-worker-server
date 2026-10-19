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
         */
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshProfanityList() {
        List<ProfanityWordEntity> words = profanityWordRepository.findAll();

        Set<String> newSet = words.stream()
                .map(entity -> {
                    String word = entity.getWord();
                    String language = entity.getLanguage();

                    if ("eng".equalsIgnoreCase(language)) {
                        return word.toLowerCase();
                    }

                    return word;
                })
                .collect(Collectors.toSet());

        this.profanitySet = newSet;
    }


}