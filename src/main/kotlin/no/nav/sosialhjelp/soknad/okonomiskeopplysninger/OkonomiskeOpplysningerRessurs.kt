package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import com.google.common.annotations.VisibleForTesting
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.getVedleggFromInternalSoknad
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
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.getSoknadPath
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.DokumentUpload
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/okonomiskeOpplysninger")
class OkonomiskeOpplysningerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val mellomlagringService: MellomlagringService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val okonomiskeOpplysningerProxy: OkonomiskeOpplysningerProxy,
) {
    @GetMapping
    fun hentOkonomiskeOpplysninger(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): VedleggFrontends {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)

        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            return okonomiskeOpplysningerProxy.getOkonomiskeOpplysninger(behandlingsId)
        } else {
            val personId = personId()
            val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, personId)

            return hentBasertPaaMellomlagredeVedlegg(behandlingsId, personId, soknadUnderArbeid)
        }
    }

    private fun hentBasertPaaMellomlagredeVedlegg(
        behandlingsId: String,
        eier: String,
        soknadUnderArbeid: SoknadUnderArbeid,
    ): VedleggFrontends {
        val jsonOkonomi = soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.okonomi ?: JsonOkonomi()
        val jsonVedleggs = getVedleggFromInternalSoknad(soknadUnderArbeid)
        val mellomlagredeVedlegg =
            when (jsonVedleggs.any { it.status == Vedleggstatus.LastetOpp.toString() }) {
                true -> mellomlagringService.getAllVedlegg(behandlingsId)
                false -> emptyList()
            }
        val opplastedeVedleggFraJson =
            jsonVedleggs.filter { it.status == Vedleggstatus.LastetOpp.toString() }.flatMap { it.filer }

        if (
            opplastedeVedleggFraJson.isNotEmpty() &&
            mellomlagredeVedlegg.isNotEmpty() &&
            opplastedeVedleggFraJson.size != mellomlagredeVedlegg.size
        ) {
            log.info("Ulikt antall vedlegg i vedlegg.json (${opplastedeVedleggFraJson.size}) og mellomlagret hos KS (${mellomlagredeVedlegg.size}) for søknad $behandlingsId")
        }

        // ikke den peneste løsningen - men dette skal returneres
        var vedleggFrontendList = emptyList<VedleggFrontend>()

        // /// ******** UPDATE WITH RETRIES *****
        soknadUnderArbeidService.updateWithRetries(soknadUnderArbeid) {
            val paakrevdeVedlegg =
                VedleggsforventningMaster.finnPaakrevdeVedlegg(soknadUnderArbeid.jsonInternalSoknad)
                    ?: emptyList()

            val slettedeVedlegg =
                removeIkkePaakrevdeMellomlagredeVedlegg(behandlingsId, jsonVedleggs, paakrevdeVedlegg, mellomlagredeVedlegg)

            addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg)

            soknadUnderArbeid.jsonInternalSoknad?.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs)

            // sync lokale filer med mellomlagrede
            vedleggFrontendList =
                jsonVedleggs.map {
                    mapMellomlagredeVedleggToVedleggFrontend(
                        vedlegg = it,
                        jsonOkonomi = jsonOkonomi,
                        mellomlagredeVedlegg = mellomlagredeVedlegg,
                    )
                }
        }

        return VedleggFrontends(
            okonomiskeOpplysninger = vedleggFrontendList,
            // brukes ikke i frontend
            slettedeVedlegg = emptyList(),
            isOkonomiskeOpplysningerBekreftet = isOkonomiskeOpplysningerBekreftet(jsonOkonomi),
        )
    }

    @PutMapping
    fun updateOkonomiskOpplysning(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody vedleggFrontend: VedleggFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            okonomiskeOpplysningerProxy.updateOkonomiskeOpplysninger(behandlingsId, vedleggFrontend)
        } else {
            val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, personId())
            val jsonOkonomi =
                soknad.jsonInternalSoknad
                    ?.soknad
                    ?.data
                    ?.okonomi ?: return

            // TODO Er det riktig at backend skal styre om det skal rendres input-felter ?
            // F.eks. skattemelding|skattemelding finnes ikke i mapperen - og får dermed ingen rader satt...
            // dette gjør at det ikke rendres noe input-felter for typen i frontend
            if (vedleggTypeToSoknadType.containsKey(vedleggFrontend.type)) {
                val rader = vedleggFrontend.rader
                if (rader != null) {
                    val soknadType = vedleggTypeToSoknadType[vedleggFrontend.type]
                    when (getSoknadPath(vedleggFrontend.type)) {
                        "utbetaling" ->
                            if (soknadType.equals(UTBETALING_HUSBANKEN, ignoreCase = true)) {
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
            }

            setVedleggStatus(
                vedleggFrontend = vedleggFrontend,
                vedlegg = JsonVedleggUtils.vedleggByFrontendType(soknad, vedleggFrontend.type),
            )
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, personId())
        }
    }

    private fun removeIkkePaakrevdeMellomlagredeVedlegg(
        behandlingsId: String,
        jsonVedleggs: MutableList<JsonVedlegg>,
        paakrevdeVedlegg: List<JsonVedlegg>,
        mellomlagredeVedlegg: List<MellomlagretVedleggMetadata>,
    ): List<VedleggFrontend> {
        val ikkeLengerPaakrevdeVedlegg =
            jsonVedleggs
                .filter { isNotInList(it, paakrevdeVedlegg) }
                .filter {
                    it.type != "annet" && it.type != "kort"
                }.toMutableList()
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
                        // dokumentId ligger ikke i JSON, så det ville være vanskelig å få tilbake.
                        // Listen slettedeVedlegg blir ikke brukt av frontend, så det er ikke noe krav om at dokumentId er gyldig her.
                        filer = ikkePaakrevdVedlegg.filer.map { DokumentUpload(filename = it.filnavn, dokumentId = "invalid-" + it.filnavn) },
                    ),
                )
            }
        }
        return slettedeVedlegg
    }

    private fun addPaakrevdeVedlegg(
        jsonVedleggs: MutableList<JsonVedlegg>,
        paakrevdeVedlegg: List<JsonVedlegg>,
    ) {
        jsonVedleggs.addAll(
            paakrevdeVedlegg
                .filter { isNotInList(it, jsonVedleggs) }
                .map { it.withStatus(Vedleggstatus.VedleggKreves.toString()) },
        )
    }

    private fun isNotInList(
        vedlegg: JsonVedlegg,
        jsonVedleggs: List<JsonVedlegg>,
    ): Boolean = jsonVedleggs.none { it.type == vedlegg.type && it.tilleggsinfo == vedlegg.tilleggsinfo }

    private fun setVedleggStatus(
        vedleggFrontend: VedleggFrontend,
        vedlegg: JsonVedlegg,
    ) {
        vedlegg.status =
            determineVedleggStatus(
                alleredeLevert = vedleggFrontend.alleredeLevert ?: false,
                vedleggStatus = vedleggFrontend.vedleggStatus,
                hasFiles = vedlegg.filer.isNotEmpty(),
            ).name
    }

    // Faren er at vedlegg får annen status - endrer status på et senere tidspunkt...
    // ..og at vi sender dette til FSL med referanser til disse filene (som ikke finnes hos KS lenger)
    private fun removeFilesIfStatusNotLastetOpp(vedlegg: JsonVedlegg) {
        if (vedlegg.filer.isNotEmpty() && vedlegg.status != Vedleggstatus.LastetOpp.toString()) {
            log.warn("Vedlegg ${vedlegg.status} ${vedlegg.tilleggsinfo} har filer. Fjerner disse. Dette bør ikke skje.")
            vedlegg.filer = emptyList()
        }
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
        hasFiles: Boolean,
    ): VedleggStatus =
        when {
            // Bruker indikerer at vedlegg allerede er sendt vha. ny frontend-kode
            alleredeLevert == true -> VedleggStatus.VedleggAlleredeSendt
            // Bruker indikerer at vedlegg er allerede sendt vha. gammel frontend-kode
            vedleggStatus == VedleggStatus.VedleggAlleredeSendt -> VedleggStatus.VedleggAlleredeSendt
            // Bruker har ikke indikert at vedlegg allerede er sendt.
            hasFiles -> VedleggStatus.LastetOpp
            else -> VedleggStatus.VedleggKreves
        }

    companion object {
        private val log by logger()
    }
}

data class VedleggFrontends(
    var okonomiskeOpplysninger: List<VedleggFrontend>?,
    // TODO Hvorfor må frontend ha oversikt over slettede vedlegg? Høre med Tore
    var slettedeVedlegg: List<VedleggFrontend>?,
    // TODO Hvorfor trenger frontend et eget flagg for dette? Høre med Tore
    @Schema(description = "True dersom bruker har oppgitt noen økonomiske opplysninger", readOnly = true)
    var isOkonomiskeOpplysningerBekreftet: Boolean,
)
