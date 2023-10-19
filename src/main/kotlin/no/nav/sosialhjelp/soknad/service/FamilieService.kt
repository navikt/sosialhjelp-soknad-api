package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.model.familie.Familie
import no.nav.sosialhjelp.soknad.model.personalia.PersonForSoknad
import no.nav.sosialhjelp.soknad.repository.FamilieRepository
import no.nav.sosialhjelp.soknad.repository.PersonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FamilieService (
    private val familieRepository: FamilieRepository,
    private val personRepository: PersonRepository
) {

    @Transactional
    fun leggTilFamilie(familieInput: Familie, personForSoknad: PersonForSoknad) {



    }

}