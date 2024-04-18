package no.nav.sosialhjelp.soknad.v2.livssituasjon

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LivssituasjonService(
    private val repository: LivssituasjonRepository,
) {
    fun getLivssituasjon(soknadId: UUID): Livssituasjon? {
        return repository.findByIdOrNull(soknadId)
    }

    fun updateBosituasjon(
        soknadId: UUID,
        botype: Botype?,
        antallHusstand: Int?,
    ): Bosituasjon {
        return getOrCreateLivssituasjon(soknadId)
            .copy(bosituasjon = Bosituasjon(botype = botype, antallHusstand = antallHusstand))
            .also { repository.save(it) }
            .bosituasjon!!
    }

    fun updateUtdanning(
        soknadId: UUID,
        erStudent: Boolean,
        studentgrad: Studentgrad?,
    ): Utdanning {
        return getOrCreateLivssituasjon(soknadId)
            .run { copy(utdanning = Utdanning(erStudent = erStudent, studentgrad = studentgrad)) }
            .also { repository.save(it) }
            .utdanning!!
    }

    fun updateKommentarTilArbeid(
        soknadId: UUID,
        kommentarTilArbeidsforhold: String,
    ): Arbeid {
        return getOrCreateLivssituasjon(soknadId)
            .copy(arbeid = Arbeid(kommentar = kommentarTilArbeidsforhold))
            .let { repository.save(it) }
            .arbeid!!
    }

    fun updateArbeidsforhold(
        soknadId: UUID,
        arbeidsforhold: List<Arbeidsforhold>,
    ): Arbeid {
        return getOrCreateLivssituasjon(soknadId)
            .run {
                (this.arbeid ?: Arbeid())
                    .let { arb -> this.copy(arbeid = arb.copy(arbeidsforhold = arbeidsforhold)) }
                    .let { livs -> repository.save(livs) }
            }
            .arbeid ?: error("Arbeid kunne ikke lagres")
    }

    private fun getOrCreateLivssituasjon(soknadId: UUID) = repository.findByIdOrNull(soknadId) ?: repository.save(Livssituasjon(soknadId))
}
