package no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type


enum class Stillingstype {
    FAST, VARIABEL
}

enum class Botype {
    EIER,
    LEIER,
    KOMMUNAL,
    INGEN,
    INSTITUSJON,
    KRISESENTER,
    FENGSEL,
    VENNER,
    FORELDRE,
    FAMILIE,
    ANNET;
}

enum class Studentgrad {
    HELTID, DELTID
}