package com.game.subwayworkerserver.config;

import com.game.subwayworkerserver.service.gps.GpsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 기본 RedisTemplate 설정 (Key: String, Value: JSON 직렬화)
     * 이 Bean은 GpsService, AbstractRedisRepository 등에서 사용됩니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // 1. Key Serializer 설정 (String 사용)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 2. Value Serializer 설정 (JSON 직렬화 사용)
        // GenericJackson2JsonRedisSerializer를 커스터마이징하여 자바 객체를 JSON으로 변환
        redisTemplate.setValueSerializer(jsonRedisSerializer());
        redisTemplate.setHashValueSerializer(jsonRedisSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Value Serializer로 사용할 ObjectMapper 설정 및 GenericJackson2JsonRedisSerializer 생성
     */
    @Bean
    public GenericJackson2JsonRedisSerializer jsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 역직렬화 시 타입 정보를 포함하도록 설정 (Redis에 저장된 객체를 올바른 자바 객체로 변환하기 위해 필수)
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Object.class) // 모든 클래스 허용 (보안에 주의 필요)
                .build();

        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}