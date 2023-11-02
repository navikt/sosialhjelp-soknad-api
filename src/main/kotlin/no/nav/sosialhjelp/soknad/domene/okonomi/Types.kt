package no.nav.sosialhjelp.soknad.domene.okonomi


enum class OkonomiGruppe {
    ANDRE_UTGIFTER,
    ARBEID,
    BOSITUASJON,
    FAMILIE,
    GENERELLE_VEDLEGG,
    INNTEKT,
    STATSBORGERSKAP,
    UTGIFTER;
}

enum class BostotteStatus {
    UNDER_BEHANDLING, VEDTATT
}


enum class Vedtaksstatus {
    INNVILGET, AVSLAG, AVVIST;
}
