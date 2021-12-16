package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(AdresseRessurs::class)
open class AdresseConfig(
    private val personService: PersonService
) {

    @Bean
    open fun adresseSystemdata(): AdresseSystemdata {
        return AdresseSystemdata(personService)
    }
}
