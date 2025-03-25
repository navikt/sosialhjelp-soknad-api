package no.nav.sosialhjelp.soknad.valkey

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisCommandTimeoutException
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.slf4j.LoggerFactory

class ValkeyStore(
    valkeyClient: RedisClient,
) {
    private val commands: RedisCommands<String, ByteArray>

    init {
        val connection = valkeyClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))
        commands = connection.sync()
    }

    fun get(key: String?): ByteArray? {
        return try {
            commands.get(key)
        } catch (e: RedisCommandTimeoutException) {
            log.warn("Valkey timeout. Returnerer null.", e)
            null
        }
    }

    fun setex(
        key: String?,
        value: ByteArray?,
        timeToLiveSeconds: Long,
    ): String? {
        return try {
            commands.setex(key, timeToLiveSeconds, value)
        } catch (e: RedisCommandTimeoutException) {
            log.warn("Valkey timeout. Returnerer null.", e)
            null
        }
    }

    fun set(
        key: String?,
        value: ByteArray?,
    ): String? {
        return try {
            commands.set(key, value)
        } catch (e: RedisCommandTimeoutException) {
            log.warn("Valkey timeout. Returnerer null.", e)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ValkeyStore::class.java)
    }
}
