package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggRadFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType
import no.nav.sosialhjelp.soknad.vedlegg.dto.DokumentUpload
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata

object VedleggMapper {
    private val log by logger()

    fun mapMellomlagredeVedleggToVedleggFrontend(
        vedlegg: JsonVedlegg,
        jsonOkonomi: JsonOkonomi,
        mellomlagredeVedlegg: List<MellomlagretVedleggMetadata>,
    ): VedleggFrontend {
        syncLocalFilesWithMellomlagrede(vedlegg, mellomlagredeVedlegg)
        val filer =
            if (vedlegg.filer.isEmpty()) {
                emptyList()
            } else {
                mapJsonFilerAndMellomlagredVedleggToFilerFrontend(vedlegg, mellomlagredeVedlegg)
            }

        val vedleggType = getVedleggType(vedlegg)
        val rader = getRader(jsonOkonomi, vedleggType)
        return VedleggFrontend(
            type = vedleggType,
            gruppe = OkonomiskGruppeMapper.getGruppe(vedleggType),
            rader = rader,
            vedleggStatus = vedlegg.status?.let { VedleggStatus.valueOf(it) } ?: VedleggStatus.VedleggKreves,
            filer = filer,
        )
    }

    // En grisete midlertidig løsning for å sikre synk mellom våre referanser og filer på mellomlager
    private fun syncLocalFilesWithMellomlagrede(
        vedlegg: JsonVedlegg,
        mellomlagredeVedlegg: List<MellomlagretVedleggMetadata>,
    ) {
        if (vedlegg.filer.isNotEmpty() && vedlegg.status != Vedleggstatus.LastetOpp.toString()) {
            log.warn("JsonVedlegg med status=${vedlegg.status} (!= LastetOpp) - men har filer? Fjerner filer")

            vedlegg.filer = emptyList()
            return
        }

        vedlegg.filer =
            vedlegg.filer
                .filter { fil ->
                    if (mellomlagredeVedlegg.find { it.filnavn == fil.filnavn } != null) {
                        true
                    } else {
                        log.error("Vedlegg har status ${vedlegg.status}, men fil finnes ikke på mellomlager. Fjerner")
                        false
                    }
                }

        if (vedlegg.filer.isEmpty() && vedlegg.status == VedleggStatus.LastetOpp.toString()) {
            vedlegg.status = VedleggStatus.VedleggKreves.toString()
        }
    }

    private fun getRader(
        jsonOkonomi: JsonOkonomi,
        vedleggType: VedleggType,
    ): List<VedleggRadFrontend> {
        if (!vedleggTypeToSoknadType.containsKey(vedleggType)) return emptyList()
        val soknadType = vedleggTypeToSoknadType[vedleggType]
        val soknadPath = VedleggTypeToSoknadTypeMapper.getSoknadPath(vedleggType)

        // Spesialtilfelle for avdrag og renter
        if (soknadType == UTGIFTER_BOLIGLAN_AVDRAG) {
            return getRadListWithAvdragAndRenter(jsonOkonomi)
        }
        return when (soknadPath) {
            "utbetaling" -> getRadListFromUtbetaling(jsonOkonomi, soknadType)
            "opplysningerUtgift" -> getRadListFromOpplysningerUtgift(jsonOkonomi, soknadType)
            "oversiktUtgift" -> getRadListFromOversiktUtgift(jsonOkonomi, soknadType)
            "formue" -> getRadListFromFormue(jsonOkonomi, soknadType)
            "inntekt" -> getRadListFromInntekt(jsonOkonomi, soknadType)
            else -> emptyList()
        }
    }

    private fun getRadListWithAvdragAndRenter(jsonOkonomi: JsonOkonomi): List<VedleggRadFrontend> {
        val avdragRad = getRadListFromOversiktUtgift(jsonOkonomi, UTGIFTER_BOLIGLAN_AVDRAG)
        val renterRad = getRadListFromOversiktUtgift(jsonOkonomi, SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER)
        for (i in avdragRad.indices) {
            avdragRad[i].renter = renterRad[i].renter
        }
        return avdragRad
    }

    private fun getRadListFromUtbetaling(
        jsonOkonomi: JsonOkonomi,
        soknadType: String?,
    ): List<VedleggRadFrontend> =
        if (jsonOkonomi.opplysninger.utbetaling.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.opplysninger.utbetaling
                .filter { it.type == soknadType }
                .map { getRadFromUtbetaling(it) }
        }

    private fun getRadListFromOpplysningerUtgift(
        jsonOkonomi: JsonOkonomi,
        soknadType: String?,
    ): List<VedleggRadFrontend> {
        val radList =
            if (jsonOkonomi.opplysninger.utgift.isEmpty()) {
                mutableListOf(VedleggRadFrontend())
            } else {
                jsonOkonomi.opplysninger.utgift
                    .filter { it.type == soknadType }
                    .map { getRadFromOpplysningerUtgift(it, soknadType) }
            }
        return if (radList.isEmpty() && soknadType == UTGIFTER_ANDRE_UTGIFTER) {
            listOf(VedleggRadFrontend())
        } else {
            radList
        }
    }

    private fun getRadListFromInntekt(
        jsonOkonomi: JsonOkonomi,
        soknadType: String?,
    ): List<VedleggRadFrontend> =
        if (jsonOkonomi.oversikt.inntekt.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.oversikt.inntekt
                .filter { it.type == soknadType }
                .map { getRadFromInntekt(it, soknadType) }
        }

    private fun getRadListFromOversiktUtgift(
        jsonOkonomi: JsonOkonomi,
        soknadType: String?,
    ): List<VedleggRadFrontend> =
        if (jsonOkonomi.oversikt.utgift.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.oversikt.utgift
                .filter { it.type == soknadType }
                .map { getRadFromOversiktUtgift(it, soknadType) }
        }

    private fun getRadListFromFormue(
        jsonOkonomi: JsonOkonomi,
        soknadType: String?,
    ): List<VedleggRadFrontend> =
        if (jsonOkonomi.oversikt.formue.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.oversikt.formue
                .filter { it.type == soknadType }
                .map { VedleggRadFrontend(belop = it.belop) }
        }

    private fun getRadFromUtbetaling(utbetaling: JsonOkonomiOpplysningUtbetaling): VedleggRadFrontend =
        when {
            utbetaling.belop != null -> VedleggRadFrontend(belop = utbetaling.belop)
            utbetaling.brutto != null -> VedleggRadFrontend(belop = Integer.valueOf(utbetaling.brutto.toString()))
            utbetaling.netto != null -> VedleggRadFrontend(belop = utbetaling.netto.toInt())
            else -> VedleggRadFrontend()
        }

    private fun getRadFromOpplysningerUtgift(
        utgift: JsonOkonomiOpplysningUtgift,
        soknadType: String?,
    ): VedleggRadFrontend =
        when (soknadType) {
            UTGIFTER_ANDRE_UTGIFTER, UTGIFTER_ANNET_BARN, UTGIFTER_ANNET_BO, UTGIFTER_BARN_FRITIDSAKTIVITETER -> {
                VedleggRadFrontend(
                    belop = utgift.belop,
                    beskrivelse = utgift.tittel.substring(utgift.tittel.indexOf(":") + 1) + " ",
                )
            }

            else -> VedleggRadFrontend(belop = utgift.belop)
        }

    private fun getRadFromInntekt(
        inntekt: JsonOkonomioversiktInntekt,
        soknadType: String?,
    ): VedleggRadFrontend {
        if (soknadType == JOBB) {
            return VedleggRadFrontend(
                brutto = inntekt.brutto,
                netto = inntekt.netto,
            )
        }
        if (inntekt.brutto != null) {
            return VedleggRadFrontend(belop = inntekt.brutto)
        } else if (inntekt.netto != null) {
            return VedleggRadFrontend(belop = inntekt.netto)
        }
        return VedleggRadFrontend()
    }

    private fun getRadFromOversiktUtgift(
        utgift: JsonOkonomioversiktUtgift,
        soknadType: String?,
    ): VedleggRadFrontend {
        if (soknadType == UTGIFTER_BOLIGLAN_AVDRAG) {
            return VedleggRadFrontend(avdrag = utgift.belop)
        } else if (soknadType == SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER) {
            return VedleggRadFrontend(renter = utgift.belop)
        }
        return VedleggRadFrontend(belop = utgift.belop)
    }

    private fun mapJsonFilerAndMellomlagredVedleggToFilerFrontend(
        jsonVedlegg: JsonVedlegg,
        mellomlagredeVedlegg: List<MellomlagretVedleggMetadata>,
    ): List<DokumentUpload> =
        jsonVedlegg.filer
            .map { fil: JsonFiler ->
                mellomlagredeVedlegg
                    .firstOrNull { it.filnavn == fil.filnavn }
                    ?.let { DokumentUpload.fromMellomlagretVedleggMetadata(it) }
                    ?: throw IllegalStateException(
                        "Vedlegget finnes ikke. vedlegg type=${jsonVedlegg.type} " +
                            "tilleggsinfo=${jsonVedlegg.tilleggsinfo} status=${jsonVedlegg.status}",
                    )
            }

    private fun getVedleggType(vedlegg: JsonVedlegg): VedleggType = VedleggType[vedlegg.type + "|" + vedlegg.tilleggsinfo]
}
