package no.nav.sosialhjelp.soknad.v2.shadow.adapter

import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import org.springframework.stereotype.Component

@Component
class FamilieAdapter(
    private val familieRepository: FamilieRepository
)
