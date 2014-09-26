package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.service.support.GuavaCacheFactoryBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() throws Exception {
        SimpleCacheManager o = new SimpleCacheManager();
        o.setCaches(
                Arrays.asList(
                        new GuavaCacheFactoryBean(Caches.SECURITY_SETTINGS, 1, 600).getObject(),
                        new GuavaCacheFactoryBean(Caches.LDAP_SETTINGS, 1, 600).getObject()
                )
        );
        return o;
    }
}
