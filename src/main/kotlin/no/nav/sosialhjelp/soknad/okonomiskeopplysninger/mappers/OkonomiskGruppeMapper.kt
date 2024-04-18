package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType

object OkonomiskGruppeMapper {
    fun getGruppe(vedleggType: VedleggType): VedleggGruppe {
        when (vedleggType) {
            VedleggType.BarnebidragMottar, VedleggType.BarnebidragBetaler, VedleggType.SamvarsavtaleBarn -> return VedleggGruppe.Familie
            VedleggType.HusleiekontraktHusleiekontrakt, VedleggType.HusleiekontraktKommunal -> return VedleggGruppe.Bosituasjon
            VedleggType.SluttoppgjorArbeid, VedleggType.LonnslippArbeid, VedleggType.StudentVedtak -> return VedleggGruppe.Arbeid
            VedleggType.AnnetAnnet -> return VedleggGruppe.AndreUtgifter
            VedleggType.SkattemeldingSkattemelding -> return VedleggGruppe.GenerelleVedlegg
            VedleggType.OppholdstillatelOppholdstillatel -> return VedleggGruppe.Statsborgerskap
            else -> {
                val soknadPath = VedleggTypeToSoknadTypeMapper.getSoknadPath(vedleggType)
                if (soknadPath == "utbetaling" || soknadPath == "formue" || soknadPath == "inntekt") {
                    return VedleggGruppe.Inntekt
                }
                if (soknadPath == "opplysningerUtgift" || soknadPath == "oversiktUtgift") {
                    return VedleggGruppe.Utgifter
                }
            }
        }
        throw IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping")
    }
}
