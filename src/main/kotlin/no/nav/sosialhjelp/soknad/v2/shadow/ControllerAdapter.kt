package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontendInput
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerInputDTO
import no.nav.sosialhjelp.soknad.utdanning.UtdanningFrontend

interface ControllerAdapter {
    fun updateArbeid(soknadId: String, arbeidFrontend: ArbeidRessurs.ArbeidsforholdRequest)
    fun updateBegrunnelse(soknadId: String, begrunnelseFrontend: BegrunnelseRessurs.BegrunnelseFrontend)
    fun updateBosituasjon(soknadId: String, bosituasjonFrontend: BosituasjonRessurs.BosituasjonFrontend)
    fun updateKontonummer(soknadId: String, kontoInputDto: KontonummerInputDTO)
    fun updateTelefonnummer(soknadId: String, telefonnummerBruker: String?)
    fun updateUtdanning(soknadId: String, utdanningFrontend: UtdanningFrontend)
    fun updateSivilstand(soknadId: String, familieFrontend: SivilstatusFrontend)
    fun updateForsorger(soknadId: String, forsorgerpliktFrontend: ForsorgerpliktFrontend)
    fun updateAdresseOgNavEnhet(soknadId: String, adresser: AdresserFrontendInput, navEnhet: NavEnhetFrontend?)
}
