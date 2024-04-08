package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.fulltNavn
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.getPersonnummerFromFnr
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.mapToJsonNavn
import no.nav.sosialhjelp.soknad.personalia.familie.dto.EktefelleFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.shadow.ControllerAdapter
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DateFormat
import java.text.SimpleDateFormat

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknader/{behandlingsId}/familie/sivilstatus", produces = [MediaType.APPLICATION_JSON_VALUE])
class SivilstatusRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val controllerAdapter: ControllerAdapter,
) {

    private val log by logger()

    @GetMapping
    fun hentSivilstatus(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): SivilstatusFrontend? {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val jsonSivilstatus = soknad.soknad.data.familie.sivilstatus ?: return null

        return mapToSivilstatusFrontend(jsonSivilstatus)
    }

    @PutMapping
    fun updateSivilstatus(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody sivilstatusFrontend: SivilstatusFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val familie = jsonInternalSoknad.soknad.data.familie
        if (familie.sivilstatus == null) {
            jsonInternalSoknad.soknad.data.familie.sivilstatus = JsonSivilstatus()
        }
        val sivilstatus = familie.sivilstatus
        sivilstatus.kilde = JsonKilde.BRUKER
        sivilstatus.status = sivilstatusFrontend.sivilstatus
        sivilstatus.ektefelle = mapToJsonEktefelle(sivilstatusFrontend.ektefelle)
        sivilstatus.borSammenMed = sivilstatusFrontend.borSammenMed

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        kotlin.runCatching {
            controllerAdapter.updateSivilstand(behandlingsId, sivilstatusFrontend)
        }.onFailure {
            log.error("Noe feilet under oppdatering av sivilstatus i ny datamodell", it)
        }
    }

    private fun addEktefelleFrontend(jsonEktefelle: JsonEktefelle): EktefelleFrontend {
        val navn = jsonEktefelle.navn
        return EktefelleFrontend(
            navn = NavnFrontend(navn.fornavn, navn.mellomnavn, navn.etternavn, fulltNavn(navn)),
            fodselsdato = jsonEktefelle.fodselsdato,
            personnummer = getPersonnummerFromFnr(jsonEktefelle.personIdentifikator)
        )
    }

    private fun mapToJsonEktefelle(ektefelle: EktefelleFrontend?): JsonEktefelle? {
        return if (ektefelle == null) {
            null
        } else {
            JsonEktefelle()
                .withNavn(mapToJsonNavn(ektefelle.navn))
                .withFodselsdato(ektefelle.fodselsdato)
                .withPersonIdentifikator(getFnr(ektefelle.fodselsdato, ektefelle.personnummer))
        }
    }

    private fun getFnr(fodselsdato: String?, personnummer: String?): String? {
        if (fodselsdato == null || personnummer == null) {
            return null
        }
        val originalFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val targetFormat: DateFormat = SimpleDateFormat("ddMMyy")
        val date = originalFormat.parse(fodselsdato)
        return targetFormat.format(date) + personnummer
    }

    private fun mapToSivilstatusFrontend(jsonSivilstatus: JsonSivilstatus): SivilstatusFrontend {
        return SivilstatusFrontend(
            kildeErSystem = mapToSystemBoolean(jsonSivilstatus.kilde),
            sivilstatus = jsonSivilstatus.status,
            ektefelle = jsonSivilstatus.ektefelle?.let { addEktefelleFrontend(it) },
            harDiskresjonskode = jsonSivilstatus.ektefelleHarDiskresjonskode,
            borSammenMed = jsonSivilstatus.borSammenMed,
            erFolkeregistrertSammen = jsonSivilstatus.folkeregistrertMedEktefelle
        )
    }

    private fun mapToSystemBoolean(kilde: JsonKilde): Boolean? {
        return when (kilde) {
            JsonKilde.SYSTEM -> true
            JsonKilde.BRUKER -> false
            else -> null
        }
    }
}
