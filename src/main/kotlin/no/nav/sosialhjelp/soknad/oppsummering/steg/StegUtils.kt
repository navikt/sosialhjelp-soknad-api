package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Svar
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import java.util.Optional
import java.util.stream.Collectors
import java.util.stream.Stream

object StegUtils {
    fun fulltnavn(navn: JsonNavn): String {
        val optionalFornavn = Optional.ofNullable(navn.fornavn)
        val optionalMellomnavn = Optional.ofNullable(navn.mellomnavn)
        val optionalEtternavn = Optional.ofNullable(navn.etternavn)
        return Stream.of(optionalFornavn, optionalMellomnavn, optionalEtternavn)
            .map { opt: Optional<String> ->
                opt.orElse(
                    ""
                )
            }
            .filter { s: String -> s.isNotBlank() }
            .collect(Collectors.joining(" "))
    }

    fun isNotNullOrEmtpy(s: String?): Boolean {
        return s != null && s.isNotEmpty()
    }

    fun integerVerdiSporsmalMedTittel(tittel: String?, key: String?, verdi: Int?): Sporsmal {
        return Sporsmal(
            tittel = tittel,
            erUtfylt = verdi != null,
            felt = verdi?.let {
                listOf(
                    Felt(
                        label = key,
                        svar = createSvar(it.toString(), SvarType.TEKST),
                        type = Type.TEKST
                    )
                )
            }
        )
    }

    fun booleanVerdiFelt(harSvartJa: Boolean, keyTrue: String, keyFalse: String): List<Felt> {
        return listOf(
            Felt(
                type = Type.CHECKBOX,
                svar = createSvar(if (harSvartJa) keyTrue else keyFalse, SvarType.LOCALE_TEKST)
            )
        )
    }

    fun harSystemRegistrerteBarn(forsorgerplikt: JsonForsorgerplikt): Boolean {
        val harForsorgerplikt = forsorgerplikt.harForsorgerplikt != null && forsorgerplikt.harForsorgerplikt.verdi == java.lang.Boolean.TRUE
        return harForsorgerplikt && forsorgerplikt.harForsorgerplikt.kilde == JsonKilde.SYSTEM && forsorgerplikt.ansvar != null && forsorgerplikt.ansvar
            .any { it.barn.kilde == JsonKilde.SYSTEM }
    }

    fun createSvar(value: String?, type: SvarType): Svar {
        return Svar(value, type)
    }
}
