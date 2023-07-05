package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
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
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper
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

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
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
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val skalBrukeMellomlagring = soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(soknadUnderArbeid)
        return if (skalBrukeMellomlagring) {
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

    private fun hentBasertPaaMellomlagredeVedlegg(behandlingsId: String, eier: String, soknadUnderArbeid: SoknadUnderArbeid): VedleggFrontends {
        val jsonOkonomi = soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.okonomi ?: JsonOkonomi()
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
        val paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknadUnderArbeid.jsonInternalSoknad)

        // *** Utvidet logging ***
        var logString = ""
        jsonVedleggs.forEach {
            if (it.filer.isNotEmpty() && it.status != VedleggStatus.LastetOpp.name) {
                if (!logString.contains("BehandlingsId: ")) {
                    logString += "BehandlingsId: $behandlingsId"
                }
                logString += "Vedlegg har status: ${it.status}, men har ${it.filer.size} filer."
            }
        }
        if (logString != "") {
            log.warn(logString)
        }
        // *** SLUTT UTVIDET LOGGING ***

        // TODO: Ny midlertidig logikk for å bli kvitt feilmeldingene
        val mellomlagredeVedlegg = if (jsonVedleggs.any { it.filer.isNotEmpty() }) {
            mellomlagringService.getAllVedlegg(behandlingsId)
        } else {
            emptyList()
        }

        // TODO: Gammel logikk
//        val mellomlagredeVedlegg = if (jsonVedleggs.any { it.status == Vedleggstatus.LastetOpp.toString() }) {
//            mellomlagringService.getAllVedlegg(behandlingsId)
//        } else {
//            emptyList()
//        }

        val opplastedeVedleggFraJson = jsonVedleggs.filter { it.status == Vedleggstatus.LastetOpp.toString() }.flatMap { it.filer }
        if (opplastedeVedleggFraJson.isNotEmpty() &&
            mellomlagredeVedlegg.isNotEmpty() &&
            opplastedeVedleggFraJson.size != mellomlagredeVedlegg.size
        ) {
            val vedleggString =
                "Ulikt antall vedlegg i vedlegg.json (${opplastedeVedleggFraJson.size}) " +
                    "og mellomlagret hos KS (${mellomlagredeVedlegg.size}) for søknad $behandlingsId " +
                    "Antall filer fra alle statuser - JsonVedlegg: ${jsonVedleggs.flatMap { it.filer }} " +
                    "Antall filer fra alle statuser - Mellomlagrede: ${mellomlagredeVedlegg.size} "

            log.warn(vedleggString)
        }

        // TODO - eksisterende logikk
//        val opplastedeVedleggFraJson = jsonVedleggs.filter { it.status == Vedleggstatus.LastetOpp.toString() }.flatMap { it.filer }
//        if (opplastedeVedleggFraJson.isNotEmpty() &&
//            mellomlagredeVedlegg.isNotEmpty() &&
//            opplastedeVedleggFraJson.size != mellomlagredeVedlegg.size
//        ) {
//            log.warn("Ulikt antall vedlegg i vedlegg.json (${opplastedeVedleggFraJson.size}) og mellomlagret hos KS (${mellomlagredeVedlegg.size}) for søknad $behandlingsId")
//        }

        val slettedeVedlegg = removeIkkePaakrevdeMellomlagredeVedlegg(behandlingsId, jsonVedleggs, paakrevdeVedlegg, mellomlagredeVedlegg)
        addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg)

        soknadUnderArbeid.jsonInternalSoknad?.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        return VedleggFrontends(
            okonomiskeOpplysninger = jsonVedleggs.map {
                mapMellomlagredeVedleggToVedleggFrontend(it, jsonOkonomi, mellomlagredeVedlegg, behandlingsId)
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
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonOkonomi = soknad.jsonInternalSoknad?.soknad?.data?.okonomi ?: return

        if (VedleggTypeToSoknadTypeMapper.isInSoknadJson(vedleggFrontend.type)) {
            val soknadType = vedleggTypeToSoknadType[vedleggFrontend.type.toString()]
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

    private fun setVedleggStatus(vedleggFrontend: VedleggFrontend, soknad: SoknadUnderArbeid) {
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknad)

        // *** START EKSTRA LOGGING ***
        val vedleggStatus = vedleggFrontend.vedleggStatus

        if (vedleggStatus != null &&
            vedleggStatus == VedleggStatus.VedleggKreves &&
            !vedleggFrontend.filer.isNullOrEmpty()
        ) {
            var logString = "BehandlingsId: ${soknad.behandlingsId} "
            logString += "VedleggFrontend har status: $vedleggStatus, men har filer: ${vedleggFrontend.filer.size} "

            logString += jsonVedleggs.firstOrNull { vedleggFrontend.type.toString() == it.type + "|" + it.tilleggsinfo }?.let {
                var internalSoknad = "Internal Soknad Status: ${it.status}, Type: ${it.type}, Filer: "
                it.filer.forEach { fil -> internalSoknad += "${fil.filnavn} " }
                internalSoknad
            }

            log.warn(logString)
        }
        // ** SLUTT EKSTRA LOGGING **

        jsonVedleggs
            .firstOrNull { vedleggFrontend.type.toString() == it.type + "|" + it.tilleggsinfo }
            ?.status = vedleggFrontend.vedleggStatus?.name ?: throw IllegalStateException("Vedlegget finnes ikke")
    }

    data class VedleggFrontends(
        var okonomiskeOpplysninger: List<VedleggFrontend>?,
        var slettedeVedlegg: List<VedleggFrontend>?,
        var isOkonomiskeOpplysningerBekreftet: Boolean
    )

    companion object {
        private val log by logger()
    }
}
