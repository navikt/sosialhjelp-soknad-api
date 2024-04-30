package no.nav.sosialhjelp.soknad.v2.livssituasjon.service

import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Utdanning
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LivssituasjonService(
    private val repository: LivssituasjonRepository,
): BosituasjonService, UtdanningService, ArbeidService, LivssituasjonRegisterService {
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

    override fun updateArbeidsforhold(soknadId: UUID, arbeidsforhold: List<Arbeidsforhold>) {
        findOrCreate(soknadId)
            .run {
                (this.arbeid ?: Arbeid())
                    .let { arb -> this.copy(arbeid = arb.copy(arbeidsforhold = arbeidsforhold)) }
                    .let { livs -> repository.save(livs) }
            }
            .arbeid ?: error("Arbeid kunne ikke lagres")
    }

    private fun findOrCreate(soknadId: UUID) = repository.findByIdOrNull(soknadId)
        ?: repository.save(Livssituasjon(soknadId))
}
