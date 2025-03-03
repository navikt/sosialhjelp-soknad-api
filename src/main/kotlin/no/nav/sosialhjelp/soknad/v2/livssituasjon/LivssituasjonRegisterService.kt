package no.nav.sosialhjelp.soknad.v2.livssituasjon

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

// TODO Denne kjører med Prop.NESTED fordi den ikke må ødelegge for annen skriving
@Transactional(propagation = Propagation.NESTED)
@Service
class LivssituasjonRegisterService(
    private val repository: LivssituasjonRepository,
    private val okonomiService: OkonomiService,
) {
    private val logger by logger()

    fun updateArbeidsforhold(
        soknadId: UUID,
        arbeidsforhold: List<Arbeidsforhold>,
    ) {
        removeInntektFromOkonomi(soknadId)

        findOrCreate(soknadId)
            .run { copy(arbeid = arbeid.copy(arbeidsforhold = arbeidsforhold)) }
            .let { repository.save(it) }
            .also { logger.info("Lagret arbeidsforhold fra Aa-registeret") }
            .also { leggTilInntekt(soknadId, it.arbeid.arbeidsforhold) }
    }

    private fun removeInntektFromOkonomi(soknadId: UUID) {
        okonomiService.removeElementFromOkonomi(soknadId, InntektType.JOBB)
        okonomiService.removeElementFromOkonomi(soknadId, InntektType.SLUTTOPPGJOER)
    }

    private fun leggTilInntekt(
        soknadId: UUID,
        arbeidsforhold: List<Arbeidsforhold>,
    ) {
        if (arbeidsforhold.hasLonnslipp()) okonomiService.addElementToOkonomi(soknadId, InntektType.JOBB)
        if (arbeidsforhold.hasSluttoppgjor()) okonomiService.addElementToOkonomi(soknadId, InntektType.SLUTTOPPGJOER)
    }

    private fun findOrCreate(soknadId: UUID) =
        repository.findByIdOrNull(soknadId)
            ?: repository.save(Livssituasjon(soknadId))
}

private fun List<Arbeidsforhold>.hasLonnslipp(): Boolean {
    return any { it.slutt == null || !isBeforeNextMonth(it.slutt) }
}

private fun List<Arbeidsforhold>.hasSluttoppgjor(): Boolean {
    return any { it.slutt != null && isBeforeNextMonth(it.slutt) }
}

// VedleggforventningMaster#isWithinOneMonthAheadInTime
private fun isBeforeNextMonth(date: LocalDate): Boolean {
    return date.isBefore(LocalDate.now().plusMonths(1).plusDays(1))
}
