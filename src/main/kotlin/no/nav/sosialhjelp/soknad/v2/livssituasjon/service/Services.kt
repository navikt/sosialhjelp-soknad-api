package no.nav.sosialhjelp.soknad.v2.livssituasjon.service

import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Utdanning
import java.util.UUID

interface ArbeidService {
    fun findArbeid(soknadId: UUID): Arbeid?

    fun updateKommentarTilArbeid(
        soknadId: UUID,
        kommentarTilArbeidsforhold: String,
    ): Arbeid
}

interface UtdanningService {
    fun findUtdanning(soknadId: UUID): Utdanning?

    fun updateUtdanning(
        soknadId: UUID,
        erStudent: Boolean,
        studentgrad: Studentgrad?,
    ): Utdanning
}

interface BosituasjonService {
    fun findBosituasjon(soknadId: UUID): Bosituasjon?

    fun updateBosituasjon(
        soknadId: UUID,
        botype: Botype?,
        antallHusstand: Int?,
    ): Bosituasjon
}

interface LivssituasjonRegisterService {
    fun updateArbeidsforhold(
        soknadId: UUID,
        arbeidsforhold: List<Arbeidsforhold>,
    )
}
