package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerRessurs
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.TelefonnummerRessurs
import no.nav.sosialhjelp.soknad.utdanning.UtdanningRessurs

interface ControllerAdapter {
    fun updateArbeid(soknadId: String, arbeidFrontend: ArbeidRessurs.ArbeidsforholdRequest)
    fun updateBegrunnelse(soknadId: String, begrunnelseFrontend: BegrunnelseRessurs.BegrunnelseFrontend)
    fun updateBosituasjon(soknadId: String, bosituasjonFrontend: BosituasjonRessurs.BosituasjonFrontend)
    fun updateKontonummer(soknadId: String, kontoInputDto: KontonummerRessurs.KontonummerInputDTO)
    fun updateTelefonnummer(soknadId: String, telefonnummerFrontend: TelefonnummerRessurs.TelefonnummerFrontend)
    fun updateUtdanning(soknadId: String, utdanningFrontend: UtdanningRessurs.UtdanningFrontend)
}
