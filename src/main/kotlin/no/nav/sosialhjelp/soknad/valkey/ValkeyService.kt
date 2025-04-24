package no.nav.sosialhjelp.soknad.valkey

import no.nav.sosialhjelp.soknad.app.LoggingUtils.maskerFnr
import no.nav.sosialhjelp.soknad.valkey.ValkeyUtils.valkeyObjectMapper
import org.slf4j.LoggerFactory.getLogger
import java.io.IOException

interface ValkeyService {
    fun get(
        key: String,
        requestedClass: Class<*>,
    ): Any?

    fun setex(
        key: String,
        value: ByteArray,
        timeToLiveSeconds: Long,
    )
}

class ValkeyServiceImpl(
    private val valkeyStore: ValkeyStore,
) : ValkeyService {
    override fun get(
        key: String,
        requestedClass: Class<*>,
    ): Any? {
        val value = valkeyStore.get(key)

        return if (value != null) {
            try {
                valkeyObjectMapper.readValue(value, requestedClass)
            } catch (e: IOException) {
                log.warn("Fant key=${maskerFnr(key)} i cache, men value var ikke ${requestedClass.simpleName}", e)
                null
            }
        } else {
            null
        }
    }

    override fun setex(
        key: String,
        value: ByteArray,
        timeToLiveSeconds: Long,
    ) {
        val result = valkeyStore.setex(key, value, timeToLiveSeconds)
        handleResponse(key, result)
    }

    private fun handleResponse(
        key: String,
        result: String?,
    ) {
        if (result != null && result.equals("OK", ignoreCase = true)) {
            log.debug("Redis put OK, key=${maskerFnr(key)}")
        } else {
            log.warn("Redis put feilet eller fikk timeout, key=${maskerFnr(key)}")
        }
    }

    companion object {
        private val log = getLogger(ValkeyServiceImpl::class.java)
    }
}

class NoValkeyService : ValkeyService {
    override fun get(
        key: String,
        requestedClass: Class<*>,
    ): Any? {
        return null
    }

    override fun setex(
        key: String,
        value: ByteArray,
        timeToLiveSeconds: Long,
    ) {
        // nothing
    }
}
