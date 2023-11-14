package no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata


interface BrukerdataKey {
    val name: String
}

enum class GenerelleDataKey: BrukerdataKey {
    TELEFONNUMMER,
    KOMMENTAR_ARBEIDSFORHOLD,
    KONTONUMMER;
}

enum class BegrunnelseKey: BrukerdataKey {
    HVORFOR_SOKE,
    HVA_SOKES_OM;
}

enum class BeskrivelseAvAnnetKey: BrukerdataKey {
    BARNEUTGIFTER,
    VERDI,
    SPARING,
    UTBETALING,
    BOUTGIFTER;
}

