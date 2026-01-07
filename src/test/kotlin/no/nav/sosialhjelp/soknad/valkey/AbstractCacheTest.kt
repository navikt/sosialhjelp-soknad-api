package no.nav.sosialhjelp.soknad.valkey

import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer

abstract class AbstractCacheTest() {
    @BeforeEach
    open fun setup() {
        clearAllMocks()
    }

    abstract fun `Verdi skal lagres i cache`()

    abstract fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`()

    abstract fun `Skal ikke hente fra client hvis verdi finnes i cache`()

    abstract fun `Hvis put til cache feiler skal fortsatt innhentet verdi returneres`()

    protected companion object ValkeyContainer : GenericContainer<ValkeyContainer>("valkey/valkey:7.2.8-alpine") {
        init {
            withExposedPorts(6379)
//            addFixedExposedPort(6379, 6379)
            withReuse(true)
        }

        @BeforeAll
        @JvmStatic
        fun startContainer() {
            start()
            System.setProperty("spring.data.redis.port", this.getMappedPort(6379).toString())
        }

        @AfterAll
        @JvmStatic
        fun stopContainer() {
            stop()
        }
    }
}
