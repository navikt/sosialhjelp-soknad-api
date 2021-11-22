package no.nav.sosialhjelp.soknad.person

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumer
import no.nav.sosialhjelp.soknad.person.domain.MapperHelper
import no.nav.sosialhjelp.soknad.person.domain.PdlDtoMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class PersonConfig(
    private val pdlHentPersonConsumer: PdlHentPersonConsumer,
    kodeverkService: KodeverkService
) {

    private val helper: MapperHelper = MapperHelper()
    private val mapper = PdlDtoMapper(kodeverkService, helper)

    @Bean
    open fun personService(): PersonService {
        return PersonService(pdlHentPersonConsumer, helper, mapper)
    }
}
