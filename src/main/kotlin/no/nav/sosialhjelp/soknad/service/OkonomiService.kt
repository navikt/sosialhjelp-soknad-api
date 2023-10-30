package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.domene.okonomi.BostotteRepository
import no.nav.sosialhjelp.soknad.domene.okonomi.FormueRepository
import no.nav.sosialhjelp.soknad.domene.okonomi.InntektRepository
import no.nav.sosialhjelp.soknad.domene.okonomi.UtgiftRepository
import org.springframework.stereotype.Service

@Service
class OkonomiService (
    private val inntektRepository: InntektRepository,
    private val utgiftRepository: UtgiftRepository,
    private val formueRepository: FormueRepository,
    private val bostotteRepository: BostotteRepository
) {







}