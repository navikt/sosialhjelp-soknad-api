package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

object OkonomiskGruppeMapper {

    fun getGruppe(vedleggType: String?): String {
        when (vedleggType) {
            "barnebidrag|mottar", "barnebidrag|betaler", "samvarsavtale|barn" -> return "familie"
            "husleiekontrakt|husleiekontrakt", "husleiekontrakt|kommunal" -> return "bosituasjon"
            "sluttoppgjor|arbeid", "lonnslipp|arbeid", "student|vedtak" -> return "arbeid"
            "annet|annet" -> return "andre utgifter"
            "skattemelding|skattemelding" -> return "generelle vedlegg"
            "oppholdstillatel|oppholdstillatel" -> return "statsborgerskap"
            else -> {
                val soknadPath = VedleggTypeToSoknadTypeMapper.getSoknadPath(vedleggType)
                if (soknadPath == "utbetaling" || soknadPath == "formue" || soknadPath == "inntekt") {
                    return "inntekt"
                }
                if (soknadPath == "opplysningerUtgift" || soknadPath == "oversiktUtgift") {
                    return "utgifter"
                }
            }
        }
        throw IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping")
    }
}
