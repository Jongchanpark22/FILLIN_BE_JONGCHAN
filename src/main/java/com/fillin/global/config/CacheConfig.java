package com.fillin.global.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 캐시 설정
 *
 * 용도:
 * - OAuth 프로필 정보 캐싱 (5분)
 * - 약관 목록 캐싱 (1시간)
 * - JWT 토큰 검증 캐싱 (토큰 유효시간)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis 캐시 매니저 설정
     * - 기본 TTL: 1시간
     * - Key Serializer: String
     * - Value Serializer: JSON
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 기본 TTL 1시간
                .entryTtl(Duration.ofHours(1))
                // Key는 String으로 직렬화
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()
                ))
                // Value는 JSON으로 직렬화
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ))
                // null 값 캐싱 방지
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                // 특정 캐시별 설정
                .withCacheConfiguration("oauthProfile",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5)) // OAuth 프로필은 5분
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()
                                ))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()
                                ))
                                .disableCachingNullValues()
                )
                .withCacheConfiguration("agreements",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1)) // 약관 목록은 1시간
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()
                                ))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()
                                ))
                                .disableCachingNullValues()
                )
                .withCacheConfiguration("tokenValidation",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1)) // JWT Access Token TTL과 동일
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()
                                ))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()
                                ))
                                .disableCachingNullValues()
                )
                .build();
    }
}

