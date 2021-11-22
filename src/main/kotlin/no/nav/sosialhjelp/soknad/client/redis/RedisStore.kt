package no.nav.sosialhjelp.soknad.client.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisCommandTimeoutException
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.slf4j.LoggerFactory

open class RedisStore(
    redisClient: RedisClient
) {
    private val commands: RedisCommands<String, ByteArray>

    init {
        val connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))
        commands = connection.sync()
    }

    open fun get(key: String?): ByteArray? {
        return try {
            commands.get(key)
        } catch (e: RedisCommandTimeoutException) {
            log.warn("Redis timeout. Returnerer null.", e)
            null
        }
    }

    open fun setex(key: String?, value: ByteArray?, timeToLiveSeconds: Long): String? {
        return try {
            commands.setex(key, timeToLiveSeconds, value)
        } catch (e: RedisCommandTimeoutException) {
            log.warn("Redis timeout. Returnerer null.", e)
            null
        }
    }

    open fun set(key: String?, value: ByteArray?): String? {
        return try {
            commands.set(key, value)
        } catch (e: RedisCommandTimeoutException) {
            log.warn("Redis timeout. Returnerer null.", e)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RedisStore::class.java)
    }
}
