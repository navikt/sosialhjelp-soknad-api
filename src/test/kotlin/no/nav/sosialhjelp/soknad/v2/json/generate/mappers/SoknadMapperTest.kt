package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype.FAST
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype.VARIABEL
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.NavEnhet
import no.nav.sosialhjelp.soknad.v2.soknad.Navn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SoknadMapperTest {

    private val mapper = SoknadToJsonMapper.SoknadMapper

    @Test
    fun `Soknad-data skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val soknad = createSoknad(sendtInn = now)

        mapper.doMapping(soknad, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)

        jsonInternalSoknad.assertNavEnhet(soknad.navEnhet!!)
        jsonInternalSoknad.assertArbeidsforholdList(soknad.arbeidsForhold)

        jsonInternalSoknad.soknad.data.personalia
            .run {
                assertKilder()
                assertEier(soknad.eier)
                assertNavn(soknad.eier.navn)
            }
    }

    private fun JsonInternalSoknad.assertInnsendingstidspunkt(tidspunkt: LocalDateTime) {
        assertThat(soknad.innsendingstidspunkt).isEqualTo(tidspunkt.toString())
    }

    private fun JsonPersonalia.assertKilder() {
        assertThat(personIdentifikator.kilde).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM)
        assertThat(navn.kilde).isEqualTo(JsonSokernavn.Kilde.SYSTEM)
        assertThat(statsborgerskap.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(kontonummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(nordiskBorger.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(telefonnummer.kilde).isEqualTo(JsonKilde.SYSTEM)
    }

    private fun JsonPersonalia.assertEier(eier: Eier) {
        assertThat(personIdentifikator.verdi).isEqualTo(eier.personId)
        assertThat(statsborgerskap.verdi).isEqualTo(eier.statsborgerskap)
        assertThat(nordiskBorger.verdi).isEqualTo(eier.nordiskBorger)
        assertThat(kontonummer.verdi).isEqualTo(eier.kontonummer)
        assertThat(telefonnummer.verdi).isEqualTo(eier.telefonnummer)
    }

    private fun JsonPersonalia.assertNavn(navnEier: Navn) {
        assertThat(navn.fornavn).isEqualTo(navnEier.fornavn)
        assertThat(navn.mellomnavn).isEqualTo(navnEier.mellomnavn)
        assertThat(navn.etternavn).isEqualTo(navnEier.etternavn)
    }

    private fun JsonInternalSoknad.assertNavEnhet(navEnhet: NavEnhet) {
        assertThat(mottaker.navEnhetsnavn).isEqualTo(navEnhet.enhetsnavn)
        assertThat(mottaker.organisasjonsnummer).isEqualTo(navEnhet.orgnummer)

        assertThat(soknad.mottaker.navEnhetsnavn).isEqualTo(navEnhet.enhetsnavn)
        assertThat(soknad.mottaker.enhetsnummer).isEqualTo(navEnhet.enhetsnummer)
        assertThat(soknad.mottaker.kommunenummer).isEqualTo(navEnhet.kommunenummer)
    }

    private fun JsonInternalSoknad.assertArbeidsforholdList(arbeidsForhold: List<Arbeidsforhold>) {
        val arbeid = soknad.data.arbeid
        arbeid.forhold.forEachIndexed { index, jsonArbeidsforhold ->
            jsonArbeidsforhold.assertArbeidsforhold(arbeidsForhold[index])
        }
    }

    private fun JsonArbeidsforhold.assertArbeidsforhold(arbeidsforhold: Arbeidsforhold) {
        assertThat(kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(arbeidsgivernavn).isEqualTo(arbeidsforhold.arbeidsgivernavn)
        assertThat(fom).isEqualTo(arbeidsforhold.start)
        assertThat(tom).isEqualTo(arbeidsforhold.slutt)
        assertThat(stillingsprosent).isEqualTo(arbeidsforhold.fastStillingsprosent)
        // TODO Er denne logikken egentlig riktig?
        when (arbeidsforhold.harFastStilling) {
            null -> assertThat(stillingstype).isNull()
            true -> assertThat(stillingstype).isEqualTo(FAST)
            else -> assertThat(stillingstype).isEqualTo(VARIABEL)
        }
    }
}
