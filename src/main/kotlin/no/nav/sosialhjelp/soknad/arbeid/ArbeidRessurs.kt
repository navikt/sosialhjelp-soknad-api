package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.apache.commons.lang3.StringUtils
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknader/{behandlingsId}/arbeid", produces = [MediaType.APPLICATION_JSON_VALUE])
class ArbeidRessurs(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GetMapping
    fun hentArbeid(
        @PathVariable("behandlingsId") behandlingsId: String
    ): ArbeidFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val arbeid = soknad.soknad.data.arbeid
        val kommentarTilArbeidsforhold = soknad.soknad.data.arbeid.kommentarTilArbeidsforhold
        val forhold = arbeid?.forhold?.map { mapToArbeidsforholdFrontend(it) }

        return ArbeidFrontend(forhold, kommentarTilArbeidsforhold?.verdi)
    }

    @PutMapping
    fun updateArbeid(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody arbeidFrontend: ArbeidFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val arbeid = jsonInternalSoknad.soknad.data.arbeid
        if (!StringUtils.isBlank(arbeidFrontend.kommentarTilArbeidsforhold)) {
            arbeid.kommentarTilArbeidsforhold = JsonKommentarTilArbeidsforhold()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(arbeidFrontend.kommentarTilArbeidsforhold)
        } else {
            arbeid.kommentarTilArbeidsforhold = null
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun mapToArbeidsforholdFrontend(arbeidsforhold: JsonArbeidsforhold): ArbeidsforholdFrontend {
        return ArbeidsforholdFrontend(
            arbeidsforhold.arbeidsgivernavn,
            arbeidsforhold.fom,
            arbeidsforhold.tom,
            isStillingstypeErHeltid(arbeidsforhold.stillingstype),
            arbeidsforhold.stillingsprosent,
            java.lang.Boolean.FALSE
        )
    }

    data class ArbeidFrontend(
        val arbeidsforhold: List<ArbeidsforholdFrontend>?,
        val kommentarTilArbeidsforhold: String?
    )

    data class ArbeidsforholdFrontend(
        var arbeidsgivernavn: String?,
        var fom: String?,
        var tom: String?,
        var stillingstypeErHeltid: Boolean?,
        var stillingsprosent: Int?,
        var overstyrtAvBruker: Boolean?
    )

    companion object {
        private fun isStillingstypeErHeltid(stillingstype: Stillingstype?): Boolean? {
            if (stillingstype == null) {
                return null
            }
            return if (stillingstype == Stillingstype.FAST) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE
        }
    }
}
