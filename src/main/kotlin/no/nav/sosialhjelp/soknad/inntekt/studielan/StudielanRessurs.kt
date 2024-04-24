package no.nav.sosialhjelp.soknad.inntekt.studielan

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/studielan", produces = [MediaType.APPLICATION_JSON_VALUE])
class StudielanRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService,
) {
    @GetMapping
    fun hentStudielanBekreftelse(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): StudielanFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        return getStudielan(behandlingsId)
    }

    @PutMapping
    fun updateStudielan(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody studielanFrontend: StudielanInputDTO,
    ): StudielanFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad =
            soknad.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val inntekter = jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt

        OkonomiMapper.setBekreftelse(
            opplysninger,
            STUDIELAN,
            studielanFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("inntekt.student"),
        )
        if (studielanFrontend.bekreftelse != null) {
            OkonomiMapper.setInntektInOversikt(
                inntekter,
                STUDIELAN,
                textService.getJsonOkonomiTittel(TitleKeyMapper.soknadTypeToTitleKey[STUDIELAN]),
                studielanFrontend.bekreftelse,
            )
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
        return getStudielan(behandlingsId)
    }

    private fun getStudielan(behandlingsId: String): StudielanFrontend {
        val soknad =
            soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier()).jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        val erStudent = soknad.soknad.data.utdanning.erStudent ?: false
        val harStudielan =
            soknad.soknad.data.okonomi.opplysninger.bekreftelse
                ?.firstOrNull { it.type == STUDIELAN }
                ?.verdi

        return StudielanFrontend(erStudent, harStudielan.takeIf { erStudent })
    }

    data class StudielanFrontend(
        /** Søker er student */
        val skalVises: Boolean,
        /** Søker mottar lån eller stipend fra Lånekassen */
        val bekreftelse: Boolean?,
    )

    data class StudielanInputDTO(
        /** Søker mottar lån eller stipend fra Lånekassen */
        val bekreftelse: Boolean?,
    )
}
