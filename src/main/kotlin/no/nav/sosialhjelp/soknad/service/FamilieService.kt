package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.domene.familie.Familie
import no.nav.sosialhjelp.soknad.domene.personalia.PersonForSoknad
import no.nav.sosialhjelp.soknad.domene.soknad.FamilieRepository
import no.nav.sosialhjelp.soknad.domene.personalia.repository.PersonRepository
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