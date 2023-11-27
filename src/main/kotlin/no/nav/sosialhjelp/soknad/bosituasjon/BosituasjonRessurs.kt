package no.nav.sosialhjelp.soknad.bosituasjon

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
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
@RequestMapping("/soknader/{behandlingsId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GetMapping
    fun hentBosituasjon(@PathVariable("behandlingsId") behandlingsId: String): BosituasjonFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        return getBosituasjonFromSoknad(behandlingsId)
    }

    private fun getBosituasjonFromSoknad(behandlingsId: String): BosituasjonFrontend {
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier()).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val bosituasjon = soknad.soknad.data.bosituasjon
        return BosituasjonFrontend(bosituasjon.botype, bosituasjon.antallPersoner)
    }

    @PutMapping
    fun updateBosituasjon(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody bosituasjonFrontend: BosituasjonFrontend
    ): BosituasjonFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = eier()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val bosituasjon = jsonInternalSoknad.soknad.data.bosituasjon
        bosituasjon.kilde = JsonKildeBruker.BRUKER
        if (bosituasjonFrontend.botype != null) {
            bosituasjon.botype = bosituasjonFrontend.botype
        }
        bosituasjon.antallPersoner = bosituasjonFrontend.antallPersoner
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        return getBosituasjonFromSoknad(behandlingsId)
    }

    data class BosituasjonFrontend(
        var botype: Botype?,
        var antallPersoner: Int?
    )
}
