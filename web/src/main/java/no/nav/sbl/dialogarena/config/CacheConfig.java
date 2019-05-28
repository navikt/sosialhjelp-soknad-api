package no.nav.sbl.dialogarena.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Import this into your Spring configuration to enable caching. Remember to add ehcache.xml in your Resources folder.
 */

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Refer to {@link org.springframework.cache.CacheManager} if you need to inject this bean in your own code.
     *
     * @return the EHCache cache manager.
     */
    @Bean
    public EhCacheCacheManager cacheManager() {
        EhCacheCacheManager cacheManager = new EhCacheCacheManager();
        net.sf.ehcache.CacheManager manager = ehCacheManagerFactoryBean().getObject();
        cacheManager.setCacheManager(manager);
        return cacheManager;
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        return new EhCacheManagerFactoryBean();
    }

    @Bean
    public CacheAdmin cacheAdmin() {
        return new EhCacheManagerAdmin(cacheManager());
    }
}
