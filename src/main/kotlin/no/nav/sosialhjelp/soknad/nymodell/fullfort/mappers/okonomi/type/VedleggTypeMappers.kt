package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.okonomi.type

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.FormueType.*
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.GenerellOkonomiType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.InntektType.*
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.UtgiftType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.UtgiftType.*


data class TypeOgTilleggsinfo (
    val type: String,
    val tilleggsinfo: String
)

fun InntektType.toTypeAndTilleggsinfo(): TypeOgTilleggsinfo {
    return when (this) {
        BARNEBIDRAG_MOTTAR -> wrap("barnebidrag", "mottar")
        DOKUMENTASJON_ANNET_INNTEKTER -> wrap("dokumentasjon", "annetinntekter")
        DOKUMENTASJON_FORSIKRINGSUTBETALING -> wrap("dokumentasjon", "forsikringsutbetaling")
        DOKUMENTASJON_UTBYTTE -> wrap("dokumentasjon", "utbytte")
        HUSBANKEN_VEDTAK -> wrap("husbanken", "vedtak")
        LONNSLIPP_ARBEID -> wrap("lonnslipp", "arbeid")
        SALGSOPPGJOR_EIENDOM -> wrap("salgsoppgjor", "eiendom")
        SLUTTOPPGJOR_ARBEID -> wrap("sluttoppgjor", "arbeid")
        STUDENT_VEDTAK -> wrap("student", "vedtak")
    }
}

fun UtgiftType.toTypeAndTilleggsinfo(): TypeOgTilleggsinfo {
    return when (this) {
        ANDRE_UTGIFTER -> wrap("annet", "annet")
        BARNEBIDRAG_BETALER -> wrap("barnebidrag", "betaler")
        DOKUMENTASJON_ANNET_BOUTGIFT -> wrap("dokumentasjon", "annetboutgift")
        FAKTURA_ANNET_BARNUTGIFT -> wrap("faktura", "annetbarnutgift")
        FAKTURA_BARNEHAGE -> wrap("faktura", "barnehage")
        FAKTURA_FRITIDSAKTIVITET -> wrap("faktura", "fritidsaktivitet")
        FAKTURA_HUSLEIE -> wrap("faktura", "husleie")
        FAKTURA_KOMMUNALEAVGIFTER -> wrap("faktura", "kommunaleavgifter")
        FAKTURA_OPPVARMING -> wrap("faktura", "oppvarming")
        FAKTURA_SFO -> wrap("faktura", "sfo")
        FAKTURA_STROM -> wrap("faktura", "strom")
        FAKTURA_TANNBEHANDLING -> wrap("faktura", "tannbehandling")
        NEDBETALINGSPLAN_AVDRAGSLAN -> wrap("nedbetalingsplan", "avdraglaan");
    }
}

fun FormueType.toTypeAndTilleggsinfo(): TypeOgTilleggsinfo {
    return when (this) {
        KONTOOVERSIKT_AKSJER -> wrap("kontooversikt", "aksjer")
        KONTOOVERSIKT_ANNET -> wrap("kontooversikt", "annet")
        KONTOOVERSIKT_BRUKSKONTO -> wrap("kontooversikt", "brukskonto")
        KONTOOVERSIKT_BSU -> wrap("kontooversikt", "bsu")
        KONTOOVERSIKT_LIVSFORSIKRING -> wrap("kontooversikt", "livsforsikring")
        KONTOOVERSIKT_SPAREKONTO -> wrap("kontooversikt", "sparekonto")
    }
}

fun GenerellOkonomiType.toTypeOgTillegsinfo(): TypeOgTilleggsinfo {
    return when (this) {
        GenerellOkonomiType.SKATTEMELDING_SKATTEMELDING -> wrap("skattemelding", "skattemelding")
    }
}

private fun wrap(type: String, tilleggsinfo: String): TypeOgTilleggsinfo = TypeOgTilleggsinfo(type, tilleggsinfo)
