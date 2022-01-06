package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.util.JsonOkonomiUtils
import no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskGruppeMapper
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllFormuerToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllInntekterToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllInntekterToJsonOkonomiUtbetalinger
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllOpplysningUtgifterToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllOversiktUtgifterToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllUtbetalingerToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggMapper.mapToVedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.getSoknadPath
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/okonomiskeOpplysninger")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class OkonomiskeOpplysningerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository
) {
    @GET
    open fun hentOkonomiskeOpplysninger(@PathParam("behandlingsId") behandlingsId: String): VedleggFrontends {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonOkonomi = soknad.jsonInternalSoknad.soknad.data.okonomi
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknad)
        val paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknad.jsonInternalSoknad)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknad.soknadId, soknad.eier)

        val slettedeVedlegg = removeIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, opplastedeVedlegg)
        addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg)

        soknad.jsonInternalSoknad.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)

        return VedleggFrontends(
            okonomiskeOpplysninger = jsonVedleggs.map { mapToVedleggFrontend(it, jsonOkonomi, opplastedeVedlegg) },
            slettedeVedlegg = slettedeVedlegg,
            isOkonomiskeOpplysningerBekreftet = JsonOkonomiUtils.isOkonomiskeOpplysningerBekreftet(jsonOkonomi)
        )
    }

    @PUT
    open fun updateOkonomiskOpplysning(
        @PathParam("behandlingsId") behandlingsId: String,
        vedleggFrontend: VedleggFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonOkonomi = soknad.jsonInternalSoknad.soknad.data.okonomi

        if (VedleggTypeToSoknadTypeMapper.isInSoknadJson(vedleggFrontend.type)) {
            val soknadType = vedleggTypeToSoknadType[vedleggFrontend.type]
            when (getSoknadPath(vedleggFrontend.type)) {
                "utbetaling" -> if (soknadType.equals(UTBETALING_HUSBANKEN, ignoreCase = true)) {
                    addAllInntekterToJsonOkonomiUtbetalinger(vedleggFrontend, jsonOkonomi, UTBETALING_HUSBANKEN)
                } else {
                    addAllUtbetalingerToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType)
                }
                "opplysningerUtgift" -> addAllOpplysningUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType)
                "oversiktUtgift" -> addAllOversiktUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType)
                "formue" -> addAllFormuerToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType)
                "inntekt" -> addAllInntekterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType)
            }
        }

        setVedleggStatus(vedleggFrontend, soknad)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun removeIkkePaakrevdeVedlegg(
        jsonVedleggs: MutableList<JsonVedlegg>,
        paakrevdeVedlegg: List<JsonVedlegg>,
        opplastedeVedlegg: List<OpplastetVedlegg>
    ): List<VedleggFrontend> {
        val ikkeLengerPaakrevdeVedlegg = jsonVedleggs.filter { isNotInList(it, paakrevdeVedlegg) }.toMutableList()
        excludeTypeAnnetAnnetFromList(ikkeLengerPaakrevdeVedlegg)
        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg)
        val slettedeVedlegg: MutableList<VedleggFrontend> = ArrayList()
        for (ikkePaakrevdVedlegg in ikkeLengerPaakrevdeVedlegg) {
            for (oVedlegg in opplastedeVedlegg) {
                if (isSameType(ikkePaakrevdVedlegg, oVedlegg)) {
                    opplastetVedleggRepository.slettVedlegg(oVedlegg.uuid, oVedlegg.eier)
                }
            }
            if (ikkePaakrevdVedlegg.filer != null && ikkePaakrevdVedlegg.filer.isNotEmpty()) {
                val vedleggstype = ikkePaakrevdVedlegg.type + "|" + ikkePaakrevdVedlegg.tilleggsinfo
                slettedeVedlegg.add(
                    VedleggFrontend(
                        type = vedleggstype,
                        gruppe = OkonomiskGruppeMapper.getGruppe(vedleggstype),
                        filer = ikkePaakrevdVedlegg.filer.map { FilFrontend(filNavn = it.filnavn) }
                    )
                )
            }
        }
        return slettedeVedlegg
    }

    private fun excludeTypeAnnetAnnetFromList(jsonVedleggs: MutableList<JsonVedlegg>) {
        jsonVedleggs.removeAll(jsonVedleggs.filter { it.type == "annet" && it.tilleggsinfo == "annet" })
    }

    private fun isSameType(jsonVedlegg: JsonVedlegg, opplastetVedlegg: OpplastetVedlegg): Boolean {
        return opplastetVedlegg.vedleggType.sammensattType == jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo
    }

    private fun addPaakrevdeVedlegg(jsonVedleggs: MutableList<JsonVedlegg>, paakrevdeVedlegg: List<JsonVedlegg>) {
        jsonVedleggs.addAll(
            paakrevdeVedlegg
                .filter { isNotInList(it, jsonVedleggs) }
                .map { it.withStatus(Vedleggstatus.VedleggKreves.toString()) }
        )
    }

    private fun isNotInList(vedlegg: JsonVedlegg, jsonVedleggs: List<JsonVedlegg>): Boolean {
        return jsonVedleggs.none { it.type == vedlegg.type && it.tilleggsinfo == vedlegg.tilleggsinfo }
    }

    private fun setVedleggStatus(vedleggFrontend: VedleggFrontend, soknad: SoknadUnderArbeid) {
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknad)
        jsonVedleggs.firstOrNull { vedleggFrontend.type == it.type + "|" + it.tilleggsinfo }
            ?.status = vedleggFrontend.vedleggStatus ?: throw IllegalStateException("Vedlegget finnes ikke")
    }

    data class VedleggFrontends(
        var okonomiskeOpplysninger: List<VedleggFrontend>?,
        var slettedeVedlegg: List<VedleggFrontend>?,
        var isOkonomiskeOpplysningerBekreftet: Boolean
    )
}
