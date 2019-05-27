package no.nav.sbl.dialogarena.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheCacheManager;

public class EhCacheManagerAdmin implements CacheAdmin {

    private static final Logger LOG = LoggerFactory.getLogger(EhCacheManagerAdmin.class);

    private EhCacheCacheManager cacheManager;

    public EhCacheManagerAdmin(EhCacheCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void clear(String cacheName) {
        cacheManager.getCache(cacheName).clear();
        LOG.info("Cleared cache '" + cacheName + "'");
    }
}
