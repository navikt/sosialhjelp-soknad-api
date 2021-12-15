package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TilgangskontrollConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val personService: PersonService
) {

    @Bean
    open fun tilgangskontroll(): Tilgangskontroll {
        return Tilgangskontroll(soknadMetadataRepository, soknadUnderArbeidRepository, personService)
    }
}