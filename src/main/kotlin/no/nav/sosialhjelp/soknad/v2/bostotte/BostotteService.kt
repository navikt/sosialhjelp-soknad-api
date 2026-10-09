package no.nav.sosialhjelp.soknad.v2.bostotte

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface BostotteService {
    fun getBostotteInfo(soknadId: UUID): BostotteInfo

    fun addForventetDokumentasjon(soknadId: UUID)

    fun saveDataFromHusbanken(
        soknadId: UUID,
        saker: List<BostotteSak>,
        utbetalinger: Inntekt?,
    )
}

@Service
class BostotteServiceImpl(
    private val okonomiService: OkonomiService,
    private val dokumentasjonService: DokumentasjonService,
) : BostotteService {
    @Transactional(readOnly = true)
    override fun getBostotteInfo(soknadId: UUID): BostotteInfo {
        return BostotteInfo(
            saker = okonomiService.getBostotteSaker(soknadId),
            utbetalinger = okonomiService.getInntekter(soknadId).filter { it.type == InntektType.UTBETALING_HUSBANKEN },
        )
    }

    @Transactional
    override fun saveDataFromHusbanken(
        soknadId: UUID,
        saker: List<BostotteSak>,
        utbetalinger: Inntekt?,
    ) {
        if (saker.isNotEmpty()) okonomiService.addBostotteSaker(soknadId, saker)
        utbetalinger?.let { okonomiService.addElementToOkonomi(soknadId, it) }
    }

    @Transactional
    override fun addForventetDokumentasjon(soknadId: UUID) {
        okonomiService.addElementToOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
    }
}

data class BostotteInfo(
    val saker: List<BostotteSak>,
    val utbetalinger: List<Inntekt>,
)

data class UpdateBostotteException(
    override val message: String?,
    val soknadId: UUID? = null,
) : SosialhjelpSoknadApiException(
        message = message,
        cause = null,
        id = soknadId?.toString(),
    )
