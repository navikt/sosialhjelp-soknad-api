package no.nav.sosialhjelp.soknad.v2.livssituasjon.service

import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Utdanning
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

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

@Service
class LivssituasjonServiceImpl(
    private val repository: LivssituasjonRepository,
) : BosituasjonService, UtdanningService, ArbeidService {
    override fun findBosituasjon(soknadId: UUID) = repository.findByIdOrNull(soknadId)?.bosituasjon

    override fun updateBosituasjon(
        soknadId: UUID,
        botype: Botype?,
        antallHusstand: Int?,
    ): Bosituasjon {
        return findOrCreate(soknadId)
            .copy(bosituasjon = Bosituasjon(botype = botype, antallHusstand = antallHusstand))
            .also { repository.save(it) }
            .bosituasjon!!
    }

    override fun findUtdanning(soknadId: UUID) = repository.findByIdOrNull(soknadId)?.utdanning

    override fun updateUtdanning(
        soknadId: UUID,
        erStudent: Boolean,
        studentgrad: Studentgrad?,
    ): Utdanning {
        return findOrCreate(soknadId)
            .run { copy(utdanning = Utdanning(erStudent = erStudent, studentgrad = studentgrad)) }
            .also { repository.save(it) }
            .utdanning!!
    }

    override fun findArbeid(soknadId: UUID) = repository.findByIdOrNull(soknadId)?.arbeid

    override fun updateKommentarTilArbeid(
        soknadId: UUID,
        kommentarTilArbeidsforhold: String,
    ): Arbeid {
        return findOrCreate(soknadId)
            .copy(arbeid = Arbeid(kommentar = kommentarTilArbeidsforhold))
            .let { repository.save(it) }
            .arbeid!!
    }



    private fun findOrCreate(soknadId: UUID) =
        repository.findByIdOrNull(soknadId)
            ?: repository.save(Livssituasjon(soknadId))
}
