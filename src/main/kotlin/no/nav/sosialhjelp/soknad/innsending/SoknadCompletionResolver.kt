package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.v2.shadow.okonomi.SoknadJsonTypeEnum

class SoknadCompletionResolver(val json: JsonInternalSoknad) {
    fun percentageOfCompletion(): Double {
        val completedFields = getUserFields().values.filterNotNull().size.toDouble()
        val totalFields = getUserFields().size.toDouble()
        return completedFields / totalFields
    }

    private fun getUserFields(): Map<String, String?> {
        return mapOf(
            "kategori" to json.soknad.data.begrunnelse?.hvorforSoke,
            "hvaSokesOm" to json.soknad.data.begrunnelse?.hvaSokesOm,
            "kommentarArbeidsforhol" to json.soknad.data.arbeid?.kommentarTilArbeidsforhold?.verdi,
            "bosituasjon" to json.soknad.data.bosituasjon?.botype?.toString(),
            "antallHusstand" to json.soknad.data.bosituasjon?.botype?.toString(),
            "andreInntekter" to
                json.soknad.data.okonomi?.opplysninger?.bekreftelse
                    ?.find { it.type == SoknadJsonTypeEnum.BEKREFTELSE_UTBETALING.verdi }?.verdi?.toString(),
            // TODO Hva med annet bank (formue)?
            "annetVerdi" to
                json.soknad.data.okonomi?.opplysninger?.bekreftelse
                    ?.find { it.type == SoknadJsonTypeEnum.BEKREFTELSE_VERDI.verdi }?.verdi?.toString(),
            // TODO Hva med boutgifter?
            "utgifterBarn" to
                json.soknad.data.okonomi?.opplysninger?.bekreftelse
                    ?.find { it.type == SoknadJsonTypeEnum.BEKREFTELSE_BARNEUTGIFTER.verdi }?.verdi?.toString(),
        )
            .plus(getCompletionTelefonnummer(json))
            .plus(getCompletionKontonummer(json))
            .plus(getCompletionAnsvar(json))
            .plus(getCompletionSivilstatus(json))
            .plus(getCompletionBarnebidrag(json))
            .plus(getCompletionSamtykkeSkattbarInntekt(json))
            .plus(getCompletionBostotte(json))
            .plus(getCompletionUtdanning(json))
            .plus(getCompletionVedlegg(json))
    }

    private fun getCompletionVedlegg(json: JsonInternalSoknad): List<Pair<String, String?>> {
        return json.vedlegg.vedlegg.map {
            when (it.status) {
                Vedleggstatus.VedleggKreves.name -> it.type to null
                else -> it.type to it.status.toString()
            }
        }
    }

    private fun getCompletionUtdanning(json: JsonInternalSoknad): List<Pair<String, String?>> {
        val utdanning: JsonUtdanning? = json.soknad.data.utdanning

        return when (utdanning?.erStudent) {
            null -> listOf("erStudent" to null)
            false -> listOf("erStudent" to "false")
            true ->
                listOf("erStudent" to "true").run { plus("studentgrad" to utdanning.studentgrad?.toString()) }
        }
    }

    private fun getCompletionBostotte(json: JsonInternalSoknad): List<Pair<String, String?>> {
        val bostotte =
            json.soknad.data.okonomi?.opplysninger?.bekreftelse
                ?.find { it.type == SoknadJsonTypeEnum.BOSTOTTE.verdi }
                ?: return listOf("bostotte" to null)

        return when (bostotte.verdi) {
            null -> listOf("bostotte" to null)
            false -> listOf("bostotte" to "false")
            true -> {
                json.soknad.data.okonomi?.opplysninger?.bekreftelse
                    ?.find { it.type == SoknadJsonTypeEnum.BOSTOTTE_SAMTYKKE.verdi }
                    .let { listOf("bostotte" to "true").plus("samtykkeBostotte" to it?.verdi?.toString()) }
            }
        }
    }

    private fun getCompletionSamtykkeSkattbarInntekt(json: JsonInternalSoknad): List<Pair<String, String?>> {
        return json.soknad.data.okonomi?.opplysninger?.bekreftelse
            ?.find { it.type == SoknadJsonTypeEnum.UTBETALING_SKATTEETATEN_SAMTYKKE.verdi }
            .let { listOf("samtykkeSkattbarInntekt" to it?.verdi?.toString()) }
    }
}

private fun getCompletionTelefonnummer(json: JsonInternalSoknad): List<Pair<String, String?>> {
    val telefonnummer = json.soknad.data.personalia?.telefonnummer

    return if (telefonnummer?.kilde == JsonKilde.SYSTEM) {
        emptyList()
    } else if (telefonnummer == null || telefonnummer.verdi == null) {
        listOf("telefonnummer" to null)
    } else if (telefonnummer.kilde == JsonKilde.BRUKER) {
        listOf("telefonnummer" to telefonnummer.verdi)
    } else {
        emptyList()
    }
}

private fun getCompletionKontonummer(json: JsonInternalSoknad): List<Pair<String, String?>> {
    val kontonummer = json.soknad.data.personalia?.kontonummer

    return if (kontonummer?.kilde == JsonKilde.SYSTEM) {
        emptyList()
    } else if (kontonummer == null || kontonummer.verdi == null) {
        listOf("kontonummer" to null)
    } else if (kontonummer.kilde == JsonKilde.BRUKER) {
        listOf("kontonummer" to kontonummer.verdi)
    } else {
        emptyList()
    }
}

private fun getCompletionAnsvar(json: JsonInternalSoknad): List<Pair<String, String?>> {
    return json.soknad.data?.familie?.forsorgerplikt?.ansvar
        ?.mapIndexed { index, jsonAnsvar -> "ansvar$index" to jsonAnsvar.harDeltBosted?.verdi.toString() }
        ?: emptyList()
}

private fun getCompletionSivilstatus(json: JsonInternalSoknad): List<Pair<String, String?>> {
    val sivilstatus = json.soknad.data?.familie?.sivilstatus

    if (sivilstatus?.kilde == JsonKilde.SYSTEM) {
        return emptyList()
    } else if (sivilstatus == null || sivilstatus.status == null) {
        return listOf("sivilstatus" to null)
    } else if (sivilstatus.kilde == JsonKilde.BRUKER) {
        return listOf("sivilstatus" to sivilstatus.status.toString())
    } else {
        return emptyList()
    }
}

private fun getCompletionBarnebidrag(json: JsonInternalSoknad): List<Pair<String, String?>> {
    return if (json.soknad.data?.familie?.forsorgerplikt?.harForsorgerplikt?.verdi == true) {
        val barnebidrag = json.soknad.data?.familie?.forsorgerplikt?.barnebidrag

        listOf("barnebidrag" to barnebidrag?.verdi?.toString())
    } else {
        emptyList()
    }
}
