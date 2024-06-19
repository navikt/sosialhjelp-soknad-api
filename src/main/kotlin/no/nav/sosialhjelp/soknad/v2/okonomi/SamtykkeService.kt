package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.springframework.stereotype.Service
import java.util.UUID

// Kan utvides til "BekreftelsesService" hvis bekreftelse == false også skal fjerne okonomiElementer
interface SamtykkeService {
    fun hasSamtykkeBostotte(soknadId: UUID): Boolean

    fun updateSamtykkeBostotte(
        soknadId: UUID,
        gitt: Boolean,
    )

    fun hasSamtykkeSkatteetaten(soknadId: UUID): Boolean

    fun updateSamtykkeSkatteetaten(
        soknadId: UUID,
        gitt: Boolean,
    )
}

@Service
class SamtykkeServiceImpl(
    private val okonomiService: OkonomiService,
) : SamtykkeService {
    override fun updateSamtykkeSkatteetaten(
        soknadId: UUID,
        gitt: Boolean,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE, verdi = gitt)
        if (!gitt) okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_SKATTEETATEN)
    }

    override fun hasSamtykkeBostotte(soknadId: UUID): Boolean {
        return okonomiService.getBekreftelser(soknadId)
            ?.find { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE }?.verdi
            ?: false
    }

    override fun updateSamtykkeBostotte(
        soknadId: UUID,
        gitt: Boolean,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = gitt)
        if (!gitt) okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
    }

    override fun hasSamtykkeSkatteetaten(soknadId: UUID): Boolean {
        return okonomiService.getBekreftelser(soknadId)
            ?.find { it.type == BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE }?.verdi
            ?: false
    }
}
