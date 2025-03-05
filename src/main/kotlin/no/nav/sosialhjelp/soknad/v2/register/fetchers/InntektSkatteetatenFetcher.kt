package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addInntektIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeInntektIfPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeUtbetalingIfPresentInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling as V2Utbetaling

// TODO Dette gjøres on demand - så trenger det egentlig være en RegisterDataFetcher?
@Component
class InntektSkatteetatenFetcher(
    private val okonomiService: OkonomiService,
    // TODO Unødvendig mellomledd?
    private val skattbarInntektService: SkattbarInntektService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val organisasjonService: OrganisasjonService,
    private val dokumentasjonService: DokumentasjonService,
) : RegisterDataFetcher {
    override fun fetchAndSave(soknadId: UUID) {
        // dobbeltsjekke at samtykke er satt
        okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE }
            ?.let { if (it.verdi) getAndSaveSkattbarInntekt(soknadId) else null }
            ?: okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_SKATTEETATEN)
    }

    private fun getAndSaveSkattbarInntekt(soknadId: UUID) {
        skattbarInntektService.hentUtbetalinger(getUserIdFromToken())?.let { utbetalinger ->
            setIntegrasjonStatus(soknadId, feilet = false)

            if (utbetalinger.isNotEmpty()) {
                saveUtbetalinger(soknadId, utbetalinger)
                okonomiService.removeElementFromOkonomi(soknadId, InntektType.JOBB)
                dokumentasjonService.fjernForventetDokumentasjon(soknadId, InntektType.JOBB)
            }
        }
            ?: setIntegrasjonStatus(soknadId, feilet = true)
    }

    private fun saveUtbetalinger(
        soknadId: UUID,
        utbetalinger: List<Utbetaling>,
    ) {
        Inntekt(
            type = InntektType.UTBETALING_SKATTEETATEN,
            inntektDetaljer = OkonomiDetaljer(utbetalinger.map { it.toUtbetaling() }),
        )
            .also { okonomiService.addElementToOkonomi(soknadId, it) }
    }

    private fun setIntegrasjonStatus(
        soknadId: UUID,
        feilet: Boolean,
    ) {
        integrasjonStatusService.setInntektSkatteetatenStatus(soknadId, feilet)
    }

    private fun Utbetaling.toUtbetaling() =
        V2Utbetaling(
            brutto = brutto,
            skattetrekk = skattetrekk,
            periodeFom = periodeFom,
            periodeTom = periodeTom,
            tittel = tittel,
            organisasjon =
                Organisasjon(
                    navn = organisasjonService.hentOrgNavn(orgnummer),
                    orgnummer = orgnummer,
                ),
        )

    // TODO Rydd opp / implementer logikk
    // Dette kan påvirke hvilke forventinger vi har til arbeidsforhold:
    fun updateVedleggForventninger(
        internalSoknad: JsonInternalSoknad,
        textService: TextService,
    ) {
        if (internalSoknad.soknad.data.soknadstype == JsonData.Soknadstype.KORT) {
            return
        }
        val utbetalinger = internalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        val inntekter = internalSoknad.soknad.data.okonomi.oversikt.inntekt
        val jsonVedleggs = VedleggsforventningMaster.finnPaakrevdeVedleggForArbeid(internalSoknad)
        if (typeIsInList(jsonVedleggs, "sluttoppgjor")) {
            val tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[SLUTTOPPGJOER])
            addUtbetalingIfNotPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER, tittel)
        } else {
            removeUtbetalingIfPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER)
        }
        if (typeIsInList(jsonVedleggs, "lonnslipp")) {
            val tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[JOBB])
            addInntektIfNotPresentInOversikt(inntekter, JOBB, tittel)
        } else {
            removeInntektIfPresentInOversikt(inntekter, JOBB)
        }
    }

    private fun typeIsInList(
        jsonVedleggs: List<JsonVedlegg>,
        vedleggstype: String,
    ): Boolean = jsonVedleggs.any { it.type == vedleggstype }
}
