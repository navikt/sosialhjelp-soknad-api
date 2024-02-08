package no.nav.sosialhjelp.soknad.v2.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Order(Ordered.HIGHEST_PRECEDENCE) // Sørger for at denne mapperen er den første som kjører
@Component
class SoknadToJsonMapper(
    private val soknadRepository: SoknadRepository
) : DomainToJsonMapper {
    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {

        soknadRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
            ?: throw IkkeFunnetException("Soknad finnes ikke")
    }

    internal companion object SoknadMapper {
        fun doMapping(domainSoknad: Soknad, json: JsonInternalSoknad) {
            with(json) {
                initializeObjects()
                soknad.innsendingstidspunkt = domainSoknad.innsendingstidspunkt.toString()
                soknad.data.personalia = domainSoknad.eier.toJsonPersonalia()
                soknad.data.arbeid = domainSoknad.toJsonArbeid()
                mottaker = domainSoknad.toJsonSoknadsMottaker1()
                soknad.mottaker = domainSoknad.toJsonSoknadsMottaker2()
            }
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            soknad.data.personalia ?: soknad.data.withPersonalia(JsonPersonalia())
        }

        private fun Eier.toJsonPersonalia(): JsonPersonalia {
            return JsonPersonalia()
                .also {
                    it.personIdentifikator = toJsonPersonIdentifikator()
                    it.navn = toJsonNavn()
                    it.nordiskBorger = toJsonNordiskBorger()
                    it.statsborgerskap = toJsonStatsborgerskap()
                    it.kontonummer = toJsonKontonummer()
                    it.telefonnummer = toJsonTelefonnummer()
                }
        }

        private fun Eier.toJsonPersonIdentifikator(): JsonPersonIdentifikator {
            return JsonPersonIdentifikator()
                .withVerdi(personId)
        }

        private fun Eier.toJsonNavn(): JsonSokernavn {
            return JsonSokernavn()
                .withFornavn(navn.fornavn)
                .withMellomnavn(navn.mellomnavn)
                .withEtternavn(navn.etternavn)
        }

        private fun Eier.toJsonNordiskBorger(): JsonNordiskBorger {
            return JsonNordiskBorger()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(nordiskBorger)
        }

        private fun Eier.toJsonStatsborgerskap(): JsonStatsborgerskap {
            return JsonStatsborgerskap()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(statsborgerskap)
        }

        private fun Eier.toJsonKontonummer(): JsonKontonummer {
            return JsonKontonummer()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(kontonummer)
        }

        private fun Eier.toJsonTelefonnummer(): JsonTelefonnummer {
            return JsonTelefonnummer()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(telefonnummer)
        }

        private fun Soknad.toJsonArbeid(): JsonArbeid? {
            return JsonArbeid()
                .withForhold(arbeidsForhold.map { it.toJsonArbeidsforhold() })
        }

        private fun Arbeidsforhold.toJsonArbeidsforhold(): JsonArbeidsforhold {
            return JsonArbeidsforhold()
                .withKilde(JsonKilde.SYSTEM)
                .withArbeidsgivernavn(arbeidsgivernavn)
                .withStillingstype(harFastStilling?.toJsonArbeidsforholdStillingtype())
                .withStillingsprosent(fastStillingsprosent)
                .withFom(start)
                .withTom(slutt)
        }

        private fun Boolean.toJsonArbeidsforholdStillingtype(): JsonArbeidsforhold.Stillingstype {
            return if (this) JsonArbeidsforhold.Stillingstype.FAST else JsonArbeidsforhold.Stillingstype.VARIABEL
        }

        private fun Soknad.toJsonSoknadsMottaker1(): no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker {
            return navEnhet?.let {
                JsonSoknadsmottaker()
                    .withOrganisasjonsnummer(it.orgnummer)
                    .withNavEnhetsnavn(it.enhetsnavn)
            } ?: throw IllegalStateException("NavEnhet finnes ikke på soknad.")
        }

        private fun Soknad.toJsonSoknadsMottaker2(): no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker {
            return navEnhet?.let {
                no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker()
                    .withEnhetsnummer(it.enhetsnummer)
                    .withKommunenummer(it.kommunenummer)
                    .withNavEnhetsnavn(it.enhetsnavn)
            } ?: throw IllegalStateException("NavEnhet finnes ikke på soknad.")
        }
    }
}
