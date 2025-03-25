package no.nav.sosialhjelp.soknad.valkey

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.valkey.ValkeyUtils.valkeyObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

internal class ValkeyServiceImplTest {
    private val valkeyStore = mockk<ValkeyStore>()
    private val valkeyService = ValkeyServiceImpl(valkeyStore)

    private val kommuneInfo = KommuneInfo("1234", true, true, true, true, null, true, null)

    @Test
    fun skalHenteFraCache() {
        every { valkeyStore.get(KOMMUNEINFO_CACHE_KEY) } returns valkeyObjectMapper.writeValueAsBytes(kommuneInfo)
        val cached = valkeyService.get(KOMMUNEINFO_CACHE_KEY, KommuneInfo::class.java) as KommuneInfo?
        assertThat(cached).isEqualTo(kommuneInfo)
    }

    @Test
    fun skalHenteAlleKommuneInfos() {
        val bytes = valkeyObjectMapper.writeValueAsBytes(listOf(kommuneInfo))
        every { valkeyStore.get(KOMMUNEINFO_CACHE_KEY) } returns bytes
        val cached = valkeyService.getKommuneInfos()
        assertThat(cached).containsKey(kommuneInfo.kommunenummer)
        assertThat(cached).containsValue(kommuneInfo)
    }

    @Test
    fun ingenKommuneInfos() {
        every { valkeyStore.get(KOMMUNEINFO_CACHE_KEY) } returns null
        val map = valkeyService.getKommuneInfos()
        assertThat(map).isNull()
    }

    @Test
    fun skalHandtereNullFraValkeyStore() {
        val key = "key"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        every { valkeyStore.set(key, value) } returns null
        valkeyService.set("key", "value".toByteArray(StandardCharsets.UTF_8))
    }
}
