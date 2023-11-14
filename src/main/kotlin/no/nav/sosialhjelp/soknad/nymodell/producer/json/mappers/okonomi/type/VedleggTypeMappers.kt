package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_AKSJER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_ANNET
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_BRUKSKONTO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_BSU
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_LIVSFORSIKRING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_SPAREKONTO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.GenerellOkonomiType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.BARNEBIDRAG_MOTTAR
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.DOKUMENTASJON_ANNET_INNTEKTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.DOKUMENTASJON_FORSIKRINGSUTBETALING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.DOKUMENTASJON_UTBYTTE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.HUSBANKEN_VEDTAK
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.LONNSLIPP_ARBEID
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.SALGSOPPGJOR_EIENDOM
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.SLUTTOPPGJOR_ARBEID
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.STUDENT_VEDTAK
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.ANDRE_UTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.BARNEBIDRAG_BETALER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.DOKUMENTASJON_ANNET_BOUTGIFT
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_ANNET_BARNUTGIFT
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_BARNEHAGE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_FRITIDSAKTIVITET
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_HUSLEIE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_KOMMUNALEAVGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_OPPVARMING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_SFO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_STROM
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_TANNBEHANDLING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.NEDBETALINGSPLAN_AVDRAGSLAN

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
