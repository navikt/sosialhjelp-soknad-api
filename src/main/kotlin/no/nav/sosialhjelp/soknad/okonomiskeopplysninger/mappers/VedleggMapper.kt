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
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.ettersending.dto.EttersendtVedlegg
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.EttersendelseUtils.soknadSendtForMindreEnn30DagerSiden
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggRadFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata
import java.time.LocalDateTime
import java.util.SortedMap
import java.util.TreeMap

object VedleggMapper {
    private const val ANNET_ANNET = "annet|annet"
    private const val LASTET_OPP = "LastetOpp"

    private val log by logger()

    fun mapToVedleggFrontend(
        vedlegg: JsonVedlegg,
        jsonOkonomi: JsonOkonomi,
        opplastedeVedlegg: List<OpplastetVedlegg>,
    ): VedleggFrontend {
        val filer = mapJsonFilerAndOpplastedeVedleggToFilerFrontend(vedlegg.filer, opplastedeVedlegg)
        val vedleggType = getVedleggType(vedlegg)
        val rader = getRader(jsonOkonomi, vedleggType)
        return VedleggFrontend(
            type = vedleggType,
            gruppe = OkonomiskGruppeMapper.getGruppe(vedleggType),
            rader = rader,
            vedleggStatus = VedleggStatus.valueOf(vedlegg.status),
            filer = filer,
        )
    }

    fun mapMellomlagredeVedleggToVedleggFrontend(
        vedlegg: JsonVedlegg,
        jsonOkonomi: JsonOkonomi,
        mellomlagredeVedlegg: List<MellomlagretVedleggMetadata>,
    ): VedleggFrontend {
        val filer = mapJsonFilerAndMellomlagredVedleggToFilerFrontend(vedlegg, mellomlagredeVedlegg)
        val vedleggType = getVedleggType(vedlegg)
        val rader = getRader(jsonOkonomi, vedleggType)
        return VedleggFrontend(
            type = vedleggType,
            gruppe = OkonomiskGruppeMapper.getGruppe(vedleggType),
            rader = rader,
            vedleggStatus = VedleggStatus.valueOf(vedlegg.status),
            filer = filer,
        )
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
        when (soknadPath) {
            "utbetaling" -> return getRadListFromUtbetaling(jsonOkonomi, soknadType)
            "opplysningerUtgift" -> return getRadListFromOpplysningerUtgift(jsonOkonomi, soknadType)
            "oversiktUtgift" -> return getRadListFromOversiktUtgift(jsonOkonomi, soknadType)
            "formue" -> return getRadListFromFormue(jsonOkonomi, soknadType)
            "inntekt" -> return getRadListFromInntekt(jsonOkonomi, soknadType)
        }
        return emptyList()
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
    ): List<VedleggRadFrontend> {
        return if (jsonOkonomi.opplysninger.utbetaling.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.opplysninger.utbetaling
                .filter { it.type == soknadType }
                .map { getRadFromUtbetaling(it) }
        }
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
    ): List<VedleggRadFrontend> {
        return if (jsonOkonomi.oversikt.inntekt.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.oversikt.inntekt
                .filter { it.type == soknadType }
                .map { getRadFromInntekt(it, soknadType) }
        }
    }

    private fun getRadListFromOversiktUtgift(
        jsonOkonomi: JsonOkonomi,
        soknadType: String?,
    ): List<VedleggRadFrontend> {
        return if (jsonOkonomi.oversikt.utgift.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.oversikt.utgift
                .filter { it.type == soknadType }
                .map { getRadFromOversiktUtgift(it, soknadType) }
        }
    }

    private fun getRadListFromFormue(
        jsonOkonomi: JsonOkonomi,
        soknadType: String?,
    ): List<VedleggRadFrontend> {
        return if (jsonOkonomi.oversikt.formue.isEmpty()) {
            mutableListOf(VedleggRadFrontend())
        } else {
            jsonOkonomi.oversikt.formue
                .filter { it.type == soknadType }
                .map { VedleggRadFrontend(belop = it.belop) }
        }
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
    ): VedleggRadFrontend {
        return when (soknadType) {
            UTGIFTER_ANDRE_UTGIFTER, UTGIFTER_ANNET_BARN, UTGIFTER_ANNET_BO, UTGIFTER_BARN_FRITIDSAKTIVITETER -> {
                VedleggRadFrontend(
                    belop = utgift.belop,
                    beskrivelse = utgift.tittel.substring(utgift.tittel.indexOf(":") + 1) + " ",
                )
            }

            else -> VedleggRadFrontend(belop = utgift.belop)
        }
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

    private fun mapJsonFilerAndOpplastedeVedleggToFilerFrontend(
        filer: List<JsonFiler>,
        opplastedeVedlegg: List<OpplastetVedlegg>,
    ): List<FilFrontend> {
        return filer
            .map { fil: JsonFiler ->
                opplastedeVedlegg
                    .firstOrNull { it.filnavn == fil.filnavn }
                    ?.let { FilFrontend(fil.filnavn, it.uuid) }
                    ?: throw IllegalStateException("Vedlegget finnes ikke")
            }
    }

    private fun mapJsonFilerAndMellomlagredVedleggToFilerFrontend(
        jsonVedlegg: JsonVedlegg,
        mellomlagredeVedlegg: List<MellomlagretVedleggMetadata>,
    ): List<FilFrontend> {
        return jsonVedlegg.filer
            .map { fil: JsonFiler ->
                if (jsonVedlegg.status != Vedleggstatus.LastetOpp.toString()) {
                    log.info("JsonVedlegg med status=${jsonVedlegg.status} (!= LastetOpp) - men har filer? Burde undersøkes")
                }
                mellomlagredeVedlegg
                    .firstOrNull { it.filnavn == fil.filnavn }
                    ?.let { FilFrontend(fil.filnavn, it.filId) }
                    ?: throw IllegalStateException("Vedlegget finnes ikke. vedlegg type=${jsonVedlegg.type} tilleggsinfo=${jsonVedlegg.tilleggsinfo} status=${jsonVedlegg.status}")
            }
    }

    fun mapVedleggToSortedListOfEttersendteVedlegg(
        innsendingstidspunkt: LocalDateTime?,
        opplastedeVedlegg: List<OpplastetVedlegg>,
        originaleVedlegg: List<JsonVedlegg>,
    ): List<EttersendtVedlegg> {
        val ettersendteVedlegg: SortedMap<String, EttersendtVedlegg> =
            TreeMap(
                sortAlphabeticallyAndPutTypeAnnetLast(),
            )
        originaleVedlegg
            .filter { filterGittInnsendingstidspunkt(innsendingstidspunkt, it) }
            .forEach { vedlegg: JsonVedlegg ->
                val sammensattNavn = getVedleggType(vedlegg).toString()
                if (!ettersendteVedlegg.containsKey(sammensattNavn)) {
                    val filerFrontend =
                        if (vedlegg.status == LASTET_OPP) {
                            mapJsonFilerAndOpplastedeVedleggToFilerFrontend(vedlegg.filer, opplastedeVedlegg)
                        } else {
                            listOf()
                        }
                    ettersendteVedlegg[sammensattNavn] =
                        EttersendtVedlegg(
                            type = sammensattNavn,
                            vedleggStatus = vedlegg.status,
                            filer = filerFrontend,
                        )
                }
            }
        return ettersendteVedlegg.values.toList()
    }

    private fun filterGittInnsendingstidspunkt(
        innsendingstidspunkt: LocalDateTime?,
        vedlegg: JsonVedlegg,
    ): Boolean {
        return if (innsendingstidspunkt != null && soknadSendtForMindreEnn30DagerSiden(innsendingstidspunkt.toLocalDate())) {
            true
        } else {
            vedlegg.status == LASTET_OPP || getVedleggType(vedlegg) == VedleggType.AnnetAnnet
        }
    }

    private fun getVedleggType(vedlegg: JsonVedlegg): VedleggType {
        return VedleggType[vedlegg.type + "|" + vedlegg.tilleggsinfo]
    }

    private fun sortAlphabeticallyAndPutTypeAnnetLast(): Comparator<String> =
        Comparator { o1, o2 ->
            when {
                o1 == o2 -> return@Comparator 0
                o1 == ANNET_ANNET -> return@Comparator 1
                o2 == ANNET_ANNET -> return@Comparator -1
                else -> o1.compareTo(o2)
            }
        }
}
