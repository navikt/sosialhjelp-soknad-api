package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Utgift
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.ANDRE_UTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.BARNEBIDRAG_BETALER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.DOKUMENTASJON_ANNET_BOUTGIFT
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_ANNET_BARNUTGIFT
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_BARNEHAGE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_FRITIDSAKTIVITET
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_HUSLEIE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_KOMMUNALEAVGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_OPPVARMING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_SFO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_STROM
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_TANNBEHANDLING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.NEDBETALINGSPLAN_AVDRAGSLAN
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UtgiftMapper(private val utgiftRepository: UtgiftRepository) : DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad) {
        json.createChildrenIfNotExists()
        with(json.soknad.data.okonomi) {
            utgiftRepository.findAllBySoknadId(soknadId).forEach { mapFromUtgift(it) }
        }
    }

    private fun JsonOkonomi.mapFromUtgift(utgift: Utgift) {
        when (utgift.type) {
            ANDRE_UTGIFTER,
            DOKUMENTASJON_ANNET_BOUTGIFT,
            FAKTURA_ANNET_BARNUTGIFT,
            FAKTURA_TANNBEHANDLING,
            FAKTURA_KOMMUNALEAVGIFTER,
            FAKTURA_FRITIDSAKTIVITET,
            FAKTURA_OPPVARMING,
            FAKTURA_STROM,
            -> opplysninger.utgift.add(utgift.toJsonOkonomiopplysningUtgift())

            BARNEBIDRAG_BETALER,
            FAKTURA_BARNEHAGE,
            FAKTURA_SFO,
            FAKTURA_HUSLEIE,
            NEDBETALINGSPLAN_AVDRAGSLAN,
            -> oversikt.utgift.add(utgift.toJsonOkonomioversiktUtgift())
        }
    }

    private fun Utgift.toJsonOkonomioversiktUtgift(): JsonOkonomioversiktUtgift {
        return JsonOkonomioversiktUtgift()
            .withKilde(JsonKilde.BRUKER) // alltid bruker
            .withType(type.toSoknadJsonType())
            .withTittel(tittel)
            .withBelop(belop)
    }

    private fun Utgift.toJsonOkonomiopplysningUtgift(): JsonOkonomiOpplysningUtgift {
        return JsonOkonomiOpplysningUtgift()
            .withKilde(JsonKilde.BRUKER) // alltid bruker
            .withType(type.toSoknadJsonType())
            .withTittel(tittel)
            .withBelop(belop)
    }
}
