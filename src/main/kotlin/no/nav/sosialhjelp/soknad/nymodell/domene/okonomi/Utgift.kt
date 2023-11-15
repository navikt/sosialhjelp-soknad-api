package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubbles
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UtgiftRepository: UpsertRepository<Utgift>, BubblesRepository<Utgift>

data class Utgift (
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val type: UtgiftType,
    val tittel: String? = null,
    val belop: Int? = null,
): SoknadBubbles(id, soknadId)

enum class UtgiftType(
    tittel: String = "" // tittel kan brukes for språk-innstillinger
): OkonomiType {
    ANDRE_UTGIFTER("Annen (brukerangitt): "),
    BARNEBIDRAG_BETALER,
    DOKUMENTASJON_ANNET_BOUTGIFT("opplysninger.utgifter.boutgift.andreutgifter"),
    FAKTURA_ANNET_BARNUTGIFT("opplysninger.utgifter.barn.annet"),
    FAKTURA_BARNEHAGE("opplysninger.utgifter.barn.barnehage"),
    FAKTURA_FRITIDSAKTIVITET("opplysninger.utgifter.barn.fritidsaktivitet"),
    FAKTURA_HUSLEIE("opplysninger.utgifter.boutgift.husleie"),
    FAKTURA_KOMMUNALEAVGIFTER("opplysninger.utgifter.boutgift.kommunaleavgifter"),
    FAKTURA_OPPVARMING("opplysninger.utgifter.boutgift.oppvarming"),
    FAKTURA_SFO("opplysninger.utgifter.barn.sfo"),
    FAKTURA_STROM("opplysninger.utgifter.boutgift.strom"),
    FAKTURA_TANNBEHANDLING("opplysninger.utgifter.barn.tannbehandling"),
    NEDBETALINGSPLAN_AVDRAGSLAN("opplysninger.utgifter.boutgift.avdraglaan.boliglanAvdrag");
}