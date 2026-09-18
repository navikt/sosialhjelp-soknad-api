package no.nav.sosialhjelp.soknad.v2.json

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad

fun createEmptyJsonInternalSoknad(
    eier: String,
    kortSoknad: Boolean,
): no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad {
    val json = createValidEmptyJsonInternalSoknad()
    val soknad = requireNotNull(json.soknad)
    return json.copy(
        soknad =
            soknad.copy(
                data =
                    soknad.data.copy(
                        personalia = soknad.data.personalia.copy(personIdentifikator = no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator(no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator.Kilde.SYSTEM, eier)),
                        soknadstype = if (kortSoknad) JsonData.Soknadstype.KORT else JsonData.Soknadstype.STANDARD,
                        arbeid = JsonArbeid(),
                        utdanning = JsonUtdanning(BRUKER),
                        bosituasjon = JsonBosituasjon(no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker.BRUKER),
                        familie = JsonFamilie(JsonForsorgerplikt()),
                        okonomi = JsonOkonomi(JsonOkonomiopplysninger(emptyList(), emptyList(), null, emptyList(), JsonBostotte()), JsonOkonomioversikt(emptyList(), emptyList(), emptyList())),
                    ),
            ),
    )
}
