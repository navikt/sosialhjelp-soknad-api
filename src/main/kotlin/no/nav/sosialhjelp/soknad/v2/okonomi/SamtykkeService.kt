package no.nav.sosialhjelp.soknad.v2.okonomi

import org.springframework.stereotype.Service
import java.util.UUID

interface SamtykkeService {
    fun hasSamtykkeBostotte(soknadId: UUID): Boolean

    fun hasSamtykkeSkatteetaten(soknadId: UUID): Boolean

    fun updateSamtykkeBostotte(
        soknadId: UUID,
        samtykkeGitt: Boolean,
    )

    fun updateSamtykkeSkatteetaten(
        soknadId: UUID,
        samtykkeGitt: Boolean,
    )
}

@Service
class SamtykkeServiceImpl(
    private val okonomiService: OkonomiService,
) : SamtykkeService {
    override fun hasSamtykkeBostotte(soknadId: UUID): Boolean {
        return okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE }?.verdi
            ?: false
    }

    override fun hasSamtykkeSkatteetaten(soknadId: UUID): Boolean {
        return okonomiService.getBekreftelser(soknadId)
            ?.find { it.type == BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE }?.verdi
            ?: false
    }

    override fun updateSamtykkeBostotte(
        soknadId: UUID,
        samtykkeGitt: Boolean,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = samtykkeGitt)
        if (!samtykkeGitt) okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
    }

    override fun updateSamtykkeSkatteetaten(
        soknadId: UUID,
        samtykkeGitt: Boolean,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE, verdi = samtykkeGitt)
        if (!samtykkeGitt) okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_SKATTEETATEN)
    }
}
