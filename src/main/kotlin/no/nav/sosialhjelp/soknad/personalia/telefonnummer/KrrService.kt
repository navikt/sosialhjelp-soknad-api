package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class KrrService(
    private val krrClient: KrrClient,
) {
    @Cacheable(value = ["KRR-kontaktInfo"], key = "#ident")
    fun getDigitalKontaktinformasjon(ident: String): DigitalKontaktinformasjon? = krrClient.getDigitalKontaktinformasjon(ident)
}
