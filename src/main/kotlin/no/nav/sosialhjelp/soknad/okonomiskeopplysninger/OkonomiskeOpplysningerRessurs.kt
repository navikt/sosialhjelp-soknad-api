package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import com.google.common.annotations.VisibleForTesting
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.JsonOkonomiUtils.isOkonomiskeOpplysningerBekreftet
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskGruppeMapper
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllFormuerToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllInntekterToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllInntekterToJsonOkonomiUtbetalinger
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllOpplysningUtgifterToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllOversiktUtgifterToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.OkonomiskeOpplysningerMapper.addAllUtbetalingerToJsonOkonomi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggMapper.mapMellomlagredeVedleggToVedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggMapper.mapToVedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.getSoknadPath
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as getUser

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true
)
@RequestMapping("/soknader/{behandlingsId}/okonomiskeOpplysninger")
class OkonomiskeOpplysningerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val mellomlagringService: MellomlagringService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService
) {
    @GetMapping
    fun hentOkonomiskeOpplysninger(
        @PathVariable("behandlingsId") behandlingsId: String
    ): VedleggFrontends {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = getUser()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        return if (soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(behandlingsId)) {
            hentBasertPaaMellomlagredeVedlegg(behandlingsId, eier, soknadUnderArbeid)
        } else {
            hentBasertPaaOpplastedeVedlegg(soknadUnderArbeid, eier)
        }
    }

    private fun hentBasertPaaOpplastedeVedlegg(soknad: SoknadUnderArbeid, eier: String): VedleggFrontends {
        val jsonOkonomi = soknad.jsonInternalSoknad?.soknad?.data?.okonomi ?: JsonOkonomi()
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknad)
        val paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknad.jsonInternalSoknad)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknad.soknadId, soknad.eier)

        val slettedeVedlegg = removeIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, opplastedeVedlegg)
        addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg)

        soknad.jsonInternalSoknad?.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)

        return VedleggFrontends(
            okonomiskeOpplysninger = jsonVedleggs.map { mapToVedleggFrontend(it, jsonOkonomi, opplastedeVedlegg) },
            slettedeVedlegg = slettedeVedlegg,
            isOkonomiskeOpplysningerBekreftet = isOkonomiskeOpplysningerBekreftet(jsonOkonomi)
        )
    }

    private fun hentBasertPaaMellomlagredeVedlegg(
        behandlingsId: String,
        eier: String,
        soknadUnderArbeid: SoknadUnderArbeid
    ): VedleggFrontends {
        val jsonOkonomi = soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.okonomi ?: JsonOkonomi()
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
        val paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknadUnderArbeid.jsonInternalSoknad)
        val mellomlagredeVedlegg = if (jsonVedleggs.any { it.status == Vedleggstatus.LastetOpp.toString() }) {
            mellomlagringService.getAllVedlegg(behandlingsId)
        } else {
            emptyList()
        }

        val opplastedeVedleggFraJson =
            jsonVedleggs.filter { it.status == Vedleggstatus.LastetOpp.toString() }.flatMap { it.filer }
        if (opplastedeVedleggFraJson.isNotEmpty() &&
            mellomlagredeVedlegg.isNotEmpty() &&
            opplastedeVedleggFraJson.size != mellomlagredeVedlegg.size
        ) {
            log.info("Ulikt antall vedlegg i vedlegg.json (${opplastedeVedleggFraJson.size}) og mellomlagret hos KS (${mellomlagredeVedlegg.size}) for søknad $behandlingsId")
        }

        val slettedeVedlegg =
            removeIkkePaakrevdeMellomlagredeVedlegg(behandlingsId, jsonVedleggs, paakrevdeVedlegg, mellomlagredeVedlegg)
        addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg)

        soknadUnderArbeid.jsonInternalSoknad?.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        return VedleggFrontends(
            okonomiskeOpplysninger = jsonVedleggs.map {
                mapMellomlagredeVedleggToVedleggFrontend(
                    it,
                    jsonOkonomi,
                    mellomlagredeVedlegg
                )
            },
            slettedeVedlegg = slettedeVedlegg,
            isOkonomiskeOpplysningerBekreftet = isOkonomiskeOpplysningerBekreftet(jsonOkonomi)
        )
    }

    @PutMapping
    fun updateOkonomiskOpplysning(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody vedleggFrontend: VedleggFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = getUser()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonOkonomi = soknad.jsonInternalSoknad?.soknad?.data?.okonomi ?: return

        if (vedleggTypeToSoknadType.containsKey(vedleggFrontend.type)) {
            val rader = vedleggFrontend.rader ?: emptyList()
            val soknadType = vedleggTypeToSoknadType[vedleggFrontend.type]
            when (getSoknadPath(vedleggFrontend.type)) {
                "utbetaling" -> if (soknadType.equals(UTBETALING_HUSBANKEN, ignoreCase = true)) {
                    addAllInntekterToJsonOkonomiUtbetalinger(rader, jsonOkonomi.opplysninger, UTBETALING_HUSBANKEN)
                } else {
                    addAllUtbetalingerToJsonOkonomi(rader, jsonOkonomi.opplysninger, soknadType)
                }

                "opplysningerUtgift" -> addAllOpplysningUtgifterToJsonOkonomi(rader, vedleggFrontend.type, jsonOkonomi.opplysninger, soknadType)
                "oversiktUtgift" -> addAllOversiktUtgifterToJsonOkonomi(rader, jsonOkonomi.oversikt, soknadType)
                "formue" -> addAllFormuerToJsonOkonomi(rader, jsonOkonomi.oversikt, soknadType)
                "inntekt" -> addAllInntekterToJsonOkonomi(rader, jsonOkonomi.oversikt, soknadType)
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
                val vedleggstype = VedleggType[ikkePaakrevdVedlegg.type + "|" + ikkePaakrevdVedlegg.tilleggsinfo]
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

    private fun removeIkkePaakrevdeMellomlagredeVedlegg(
        behandlingsId: String,
        jsonVedleggs: MutableList<JsonVedlegg>,
        paakrevdeVedlegg: List<JsonVedlegg>,
        mellomlagredeVedlegg: List<MellomlagretVedleggMetadata>
    ): List<VedleggFrontend> {
        val ikkeLengerPaakrevdeVedlegg = jsonVedleggs.filter { isNotInList(it, paakrevdeVedlegg) }.toMutableList()
        excludeTypeAnnetAnnetFromList(ikkeLengerPaakrevdeVedlegg)
        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg)
        val slettedeVedlegg: MutableList<VedleggFrontend> = ArrayList()
        for (ikkePaakrevdVedlegg in ikkeLengerPaakrevdeVedlegg) {
            for (mellomlagretVedlegg in mellomlagredeVedlegg) {
                ikkePaakrevdVedlegg.filer.forEach {
                    if (it.filnavn == mellomlagretVedlegg.filnavn) {
                        mellomlagringService.deleteVedlegg(behandlingsId, mellomlagretVedlegg.filId)
                    }
                }
            }
            if (!ikkePaakrevdVedlegg.filer.isNullOrEmpty()) {
                val vedleggstype = VedleggType[ikkePaakrevdVedlegg.type + "|" + ikkePaakrevdVedlegg.tilleggsinfo]
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

    /**
     * Utleder vedleggsstatus på en bakoverkompatibel måte.
     *
     * @param alleredeLevert Bruker indikerer at vedlegget allerede er levert (ny API-revisjon).
     * @param vedleggStatus Status-felt fra gammel API-revisjon (ignoreres om != VedleggAlleredeSendt).
     * @param hasFiles om filer er lastet opp til vedlegget.
     * @return VedleggAlleredeSendt hvis alleredeLevert == true eller vedleggStatus == VedleggAlleredeSendt.
     *         Ellers VedleggKreves hvis hasFiles == false, og LastetOpp hvis hasFiles == true.
     */
    @VisibleForTesting
    internal fun determineVedleggStatus(
        alleredeLevert: Boolean,
        vedleggStatus: VedleggStatus?,
        hasFiles: Boolean
    ): VedleggStatus {
        return when {
            /* Bruker indikerer at vedlegg allerede er sendt vha. ny frontend-kode */
            alleredeLevert == true -> VedleggStatus.VedleggAlleredeSendt
            /* Bruker indikerer at vedlegg er allerede sendt vha. gammel frontend-kode */
            vedleggStatus == VedleggStatus.VedleggAlleredeSendt -> VedleggStatus.VedleggAlleredeSendt
            /* Bruker har ikke indikert at vedlegg allerede er sendt. */
            hasFiles -> VedleggStatus.LastetOpp
            else -> VedleggStatus.VedleggKreves
        }
    }

    private fun setVedleggStatus(vedleggFrontend: VedleggFrontend, soknad: SoknadUnderArbeid) {
        val vedlegg = JsonVedleggUtils.vedleggByFrontendType(soknad, vedleggFrontend.type).firstOrNull()

        requireNotNull(vedlegg) { "Vedlegget finnes ikke" }

        vedlegg.status = determineVedleggStatus(
            alleredeLevert = vedleggFrontend.alleredeLevert ?: false,
            vedleggStatus = vedleggFrontend.vedleggStatus,
            hasFiles = vedlegg.filer.isNotEmpty()
        ).name
    }

    data class VedleggFrontends(
        var okonomiskeOpplysninger: List<VedleggFrontend>?,
        var slettedeVedlegg: List<VedleggFrontend>?,
        @Schema(description = "True dersom bruker har oppgitt noen økonomiske opplysninger", readOnly = true)
        var isOkonomiskeOpplysningerBekreftet: Boolean
    )

    companion object {
        private val log by logger()
    }
}
