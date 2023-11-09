package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.OkonomiObject
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Utgift
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.repository.FormueRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.repository.InntektRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.repository.UtgiftRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.UtgiftType
import org.springframework.stereotype.Component
import java.util.*

@Component
class InntektUtgiftFormueMapper(
    private val inntektRepository: InntektRepository,
    private val utgiftRepository: UtgiftRepository,
    private val formueRepository: FormueRepository
): DomainToJsonMapper {

    override fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val inntekterForSoknad = inntektRepository.findAllBySoknadId(soknadId)
        val utgifterForSoknad = utgiftRepository.findAllBySoknadId(soknadId)
        val formueForSoknad = formueRepository.findAllBySoknadId(soknadId)

        val okonomiObjects = listOf(inntekterForSoknad, utgifterForSoknad, formueForSoknad).flatten()
        okonomiObjects.forEach {
            jsonInternalSoknad.soknad.data.okonomi.mapFromDomainObject(it)
        }
    }
}

fun JsonOkonomi.initChildren() {
    if (opplysninger == null) withOpplysninger(JsonOkonomiopplysninger())
    if (oversikt == null) withOversikt(JsonOkonomioversikt())
}

fun JsonOkonomi.mapFromDomainObject(domain: OkonomiObject) {
    initChildren()
    when (domain.type) {
        InntektType.DOKUMENTASJON_FORSIKRINGSUTBETALING,
        InntektType.DOKUMENTASJON_ANNET_INNTEKTER,
        InntektType.DOKUMENTASJON_UTBYTTE,
        InntektType.HUSBANKEN_VEDTAK,
        InntektType.SALGSOPPGJOR_EIENDOM,
        InntektType.SLUTTOPPGJOR_ARBEID,
        -> with (domain as Inntekt) { opplysninger.utbetaling.add(this.toJsonOkonomiOpplysningUtbetaling()) }

        InntektType.BARNEBIDRAG_MOTTAR,
        InntektType.LONNSLIPP_ARBEID,
        InntektType.STUDENT_VEDTAK
        -> with (domain as Inntekt) { oversikt.inntekt.add(this.toJsonOkonomioversiktInntekt()) }

        UtgiftType.ANDRE_UTGIFTER,
        UtgiftType.DOKUMENTASJON_ANNET_BOUTGIFT,
        UtgiftType.FAKTURA_ANNET_BARNUTGIFT,
        UtgiftType.FAKTURA_TANNBEHANDLING,
        UtgiftType.FAKTURA_KOMMUNALEAVGIFTER,
        UtgiftType.FAKTURA_FRITIDSAKTIVITET,
        UtgiftType.FAKTURA_OPPVARMING,
        UtgiftType.FAKTURA_STROM,
        -> with (domain as Utgift) { opplysninger.utgift.add(this.toJsonOkonomiopplysningUtgift()) }

        UtgiftType.BARNEBIDRAG_BETALER,
        UtgiftType.FAKTURA_BARNEHAGE,
        UtgiftType.FAKTURA_SFO,
        UtgiftType.FAKTURA_HUSLEIE,
        UtgiftType.NEDBETALINGSPLAN_AVDRAGSLAN
        -> with (domain as Utgift) { oversikt.utgift.add(this.toJsonOkonomioversiktUtgift()) }

        FormueType.KONTOOVERSIKT_AKSJER,
        FormueType.KONTOOVERSIKT_ANNET,
        FormueType.KONTOOVERSIKT_BRUKSKONTO,
        FormueType.KONTOOVERSIKT_BSU,
        FormueType.KONTOOVERSIKT_LIVSFORSIKRING,
        FormueType.KONTOOVERSIKT_SPAREKONTO
        -> with (domain as Formue) { oversikt.formue.add(this.toJsonOkonomioversiktFormue()) }

        else -> throw IllegalStateException("OkonomiTypen mangler mapping til Json-objekt")
    }
}
