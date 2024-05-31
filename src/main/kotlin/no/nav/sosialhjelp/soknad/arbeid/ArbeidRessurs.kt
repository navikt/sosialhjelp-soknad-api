package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.shadow.V2ControllerAdapter
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
@RequestMapping("/soknader/{behandlingsId}/arbeid", produces = [MediaType.APPLICATION_JSON_VALUE])
class ArbeidRessurs(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val tilgangskontroll: Tilgangskontroll,
    private val controllerAdapter: V2ControllerAdapter,
) {
    @GetMapping
    fun hentArbeid(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): ArbeidsforholdResponse {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        return getArbeidFromSoknad(behandlingsId)
    }

    private fun getArbeidFromSoknad(behandlingsId: String): ArbeidsforholdResponse {
        val intern =
            soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier()).jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val kommentarTilArbeidsforhold = intern.soknad.data.arbeid.kommentarTilArbeidsforhold?.verdi
        val forhold = intern.soknad.data.arbeid?.forhold?.map { mapToArbeidsforholdFrontend(it) } ?: emptyList()

        return ArbeidsforholdResponse(forhold, kommentarTilArbeidsforhold)
    }

    @PutMapping
    fun updateArbeid(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody arbeidFrontend: ArbeidsforholdRequest,
    ): ArbeidsforholdResponse {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad =
            soknad.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val arbeid = jsonInternalSoknad.soknad.data.arbeid

        arbeid.kommentarTilArbeidsforhold =
            arbeidFrontend.kommentarTilArbeidsforhold?.takeIf { it.isNotBlank() }?.let {
                JsonKommentarTilArbeidsforhold().apply {
                    kilde = JsonKildeBruker.BRUKER
                    verdi = it
                }
            }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())

        // NyModell
        kotlin.runCatching { controllerAdapter.updateArbeid(behandlingsId, arbeidFrontend) }
            .onFailure { }

        return getArbeidFromSoknad(behandlingsId)
    }

    private fun mapToArbeidsforholdFrontend(arbeidsforhold: JsonArbeidsforhold) =
        ArbeidsforholdFrontend(
            arbeidsforhold.arbeidsgivernavn,
            arbeidsforhold.fom,
            arbeidsforhold.tom,
            arbeidsforhold.stillingstype?.let { isStillingstypeHeltid(it) },
            arbeidsforhold.stillingsprosent,
            java.lang.Boolean.FALSE,
        )

    data class ArbeidsforholdResponse(
        val arbeidsforhold: List<ArbeidsforholdFrontend>,
        val kommentarTilArbeidsforhold: String?,
    )

    data class ArbeidsforholdRequest(
        val kommentarTilArbeidsforhold: String?,
    )

    data class ArbeidsforholdFrontend(
        var arbeidsgivernavn: String?,
        var fom: String?,
        var tom: String?,
        var stillingstypeErHeltid: Boolean?,
        var stillingsprosent: Int?,
        var overstyrtAvBruker: Boolean?,
    )

    companion object {
        private fun isStillingstypeHeltid(stillingstype: Stillingstype) = (stillingstype == Stillingstype.FAST)
    }
}
