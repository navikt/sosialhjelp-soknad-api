package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.getBekreftelseVerdi
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.getSamtykkeDato
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateBekreftelse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/bostotte", produces = [MediaType.APPLICATION_JSON_VALUE])
class BostotteRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val bostotteSystemdata: BostotteSystemdata,
    private val textService: TextService,
) {
    @GetMapping
    fun hentBostotte(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BostotteFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val isBekreftet = opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BOSTOTTE) == true
        val harSamtykke = opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BOSTOTTE_SAMTYKKE) == true

        return BostotteFrontend(
            bekreftelse = isBekreftet,
            samtykke = isBekreftet && harSamtykke,
            utbetalinger = opplysninger.utbetaling.filter { it.type == SoknadJsonTyper.UTBETALING_HUSBANKEN },
            saker = opplysninger.bostotte.saker.filter { it.type == SoknadJsonTyper.UTBETALING_HUSBANKEN },
            stotteFraHusbankenFeilet = jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet,
            samtykkeTidspunkt = opplysninger.getSamtykkeDato(SoknadJsonTyper.BOSTOTTE_SAMTYKKE),
        )
    }

    @PutMapping
    fun updateBostotte(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody bostotteFrontend: BostotteFrontend,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger

        opplysninger.updateBekreftelse(SoknadJsonTyper.BOSTOTTE, bostotteFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.bostotte"))

        bostotteFrontend.bekreftelse?.let {
            val title = textService.getJsonOkonomiTittel(TitleKeyMapper.soknadTypeToTitleKey[SoknadJsonTyper.BOSTOTTE])
            OkonomiMapper.setUtbetalingInOpplysninger(opplysninger.utbetaling, SoknadJsonTyper.UTBETALING_HUSBANKEN, it, title)
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    @PostMapping("/samtykke")
    fun updateSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykkeFrontend: Boolean,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger

        if (samtykkeFrontend != opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BOSTOTTE_SAMTYKKE)) {
            opplysninger.updateBekreftelse(SoknadJsonTyper.BOSTOTTE, samtykkeFrontend, textService.getJsonOkonomiTittel("inntekt.bostotte"))
            bostotteSystemdata.updateSystemdataIn(soknad, token)
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
        }
    }

    data class BostotteFrontend(
        val bekreftelse: Boolean?,
        val samtykke: Boolean?,
        val utbetalinger: List<JsonOkonomiOpplysningUtbetaling>?,
        val saker: List<JsonBostotteSak>?,
        val stotteFraHusbankenFeilet: Boolean?,
        val samtykkeTidspunkt: String?,
    )
}
