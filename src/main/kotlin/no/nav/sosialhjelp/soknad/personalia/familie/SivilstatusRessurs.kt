package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.fulltNavn
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.getPersonnummerFromFnr
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.mapToJsonNavn
import no.nav.sosialhjelp.soknad.personalia.familie.dto.EktefelleFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.stereotype.Controller
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/familie/sivilstatus")
@Produces(MediaType.APPLICATION_JSON)
open class SivilstatusRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GET
    open fun hentSivilstatus(@PathParam("behandlingsId") behandlingsId: String): SivilstatusFrontend? {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val jsonSivilstatus = soknad.soknad.data.familie.sivilstatus ?: return null

        return mapToSivilstatusFrontend(jsonSivilstatus)
    }

    @PUT
    open fun updateSivilstatus(
        @PathParam("behandlingsId") behandlingsId: String,
        sivilstatusFrontend: SivilstatusFrontend
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
        } else JsonEktefelle()
            .withNavn(mapToJsonNavn(ektefelle.navn))
            .withFodselsdato(ektefelle.fodselsdato)
            .withPersonIdentifikator(getFnr(ektefelle.fodselsdato, ektefelle.personnummer))
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
            ektefelle = if (jsonSivilstatus.ektefelle == null) null else addEktefelleFrontend(jsonSivilstatus.ektefelle),
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
