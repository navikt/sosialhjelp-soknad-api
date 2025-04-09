package no.nav.sosialhjelp.soknad.v2.livssituasjon

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.AnnenDokumentasjonType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface ArbeidService {
    fun findArbeid(soknadId: UUID): Arbeid?

    fun updateKommentarTilArbeid(
        soknadId: UUID,
        kommentarTilArbeidsforhold: String?,
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
    private val dokumentasjonService: DokumentasjonService,
) : BosituasjonService, UtdanningService, ArbeidService {
    override fun findBosituasjon(soknadId: UUID) = repository.findByIdOrNull(soknadId)?.bosituasjon

    override fun updateBosituasjon(
        soknadId: UUID,
        botype: Botype?,
        antallHusstand: Int?,
    ): Bosituasjon {
        val livssituasjon = findOrCreate(soknadId)

        return livssituasjon
            .copy(bosituasjon = Bosituasjon(botype = botype, antallHusstand = antallHusstand))
            .let { repository.save(it) }
            .also {
                handleDokumentasjonskrav(
                    soknadId = soknadId,
                    oldBotype = livssituasjon.bosituasjon?.botype,
                    newBotype = botype,
                )
            }
            .bosituasjon!!
    }

    private fun handleDokumentasjonskrav(
        soknadId: UUID,
        oldBotype: Botype?,
        newBotype: Botype?,
    ) {
        if (oldBotype != newBotype) {
            if (oldBotype.hasDokumentasjon()) {
                dokumentasjonService.fjernForventetDokumentasjon(soknadId, oldBotype.toUgiftType())
            }

            if (newBotype.hasDokumentasjon()) {
                dokumentasjonService.opprettDokumentasjon(soknadId, newBotype.toUgiftType())
            }
        }
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
        kommentarTilArbeidsforhold: String?,
    ): Arbeid {
        return findOrCreate(soknadId)
            .run { copy(arbeid = arbeid.copy(kommentar = kommentarTilArbeidsforhold)) }
            .let { repository.save(it) }
            .arbeid
    }

    private fun findOrCreate(soknadId: UUID) =
        repository.findByIdOrNull(soknadId)
            ?: repository.save(Livssituasjon(soknadId))
}

private fun Botype?.hasDokumentasjon() = this == Botype.LEIER || this == Botype.KOMMUNAL

private fun Botype?.toUgiftType(): AnnenDokumentasjonType {
    return when (this) {
        Botype.LEIER -> AnnenDokumentasjonType.HUSLEIEKONTRAKT
        Botype.KOMMUNAL -> AnnenDokumentasjonType.HUSLEIEKONTRAKT_KOMMUNAL
        else -> error("Botype $this har ingen tilsvarende UtgiftType")
    }
}
