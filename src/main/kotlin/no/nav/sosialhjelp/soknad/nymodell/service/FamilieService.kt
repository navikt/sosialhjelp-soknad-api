package no.nav.sosialhjelp.soknad.nymodell.service

import no.nav.sosialhjelp.soknad.nymodell.domene.familie.repository.ForsorgerRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.repository.SivilstandRepository
import org.springframework.stereotype.Service

@Service
class FamilieService (
    private val forsorgerRepository: ForsorgerRepository,
    private val sivilstandRepository: SivilstandRepository
) {


}