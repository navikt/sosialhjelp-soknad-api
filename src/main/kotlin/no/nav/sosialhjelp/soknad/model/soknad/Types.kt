package no.nav.sosialhjelp.soknad.model.soknad

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg

// TODO Brukes alle?
/**
 * @see [no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype]
 */
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

    companion object {
        fun fromValue(value: String?): Botype? = value?.let { Botype.valueOf(it) }
    }
}

enum class VedleggType(val type: String, val tilleggsinfo: String) {
    BARNEBIDRAG("barnebidrag", "utgift")
}

enum class VedleggStatus {
    KREVES, LASTET_OPP, LEVERT
}

enum class VedleggHendelseType(value: String?) {
    DOKUMENTASJON_ETTERSPURT("dokumentasjonEtterspurt"),
    DOKUMENTASJONKRAV("dokumentasjonkrav"),
    SOKNAD("soknad"),
    BRUKER("bruker");

    fun toJsonVedleggHendelseType(): JsonVedlegg.HendelseType {
        return JsonVedlegg.HendelseType.valueOf(this.name)
    }
}

enum class Stillingstype {
    FAST, VARIABEL
}