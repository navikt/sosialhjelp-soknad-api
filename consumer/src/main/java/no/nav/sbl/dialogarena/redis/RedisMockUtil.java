package no.nav.sbl.dialogarena.redis;

import com.github.fppt.jedismock.RedisServer;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public final class RedisMockUtil {

    private static final Logger log = getLogger(RedisMockUtil.class);
    private static RedisServer mockedRedisServer;

    private RedisMockUtil() {
    }

    public static void startRedisMocked(int port) {
        log.warn("Starter MOCKET in-memory redis. Denne meldingen skal aldri vises i prod.");
        try {
            mockedRedisServer = RedisServer.newRedisServer(port);
            mockedRedisServer.start();
        } catch (IOException e) {
            log.warn("Noe feilet ved oppstart av MOCKED redis", e);
        }
    }

    public static void stopRedisMocked() {
        mockedRedisServer.stop();
    }
}
