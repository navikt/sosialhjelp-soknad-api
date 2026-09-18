package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Kategori
import no.nav.sosialhjelp.soknad.v2.soknad.Kategorier
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Order(Ordered.HIGHEST_PRECEDENCE) // Sørger for at denne mapperen er den første som kjører
@Component
class SoknadToJsonMapper(
    private val soknadRepository: SoknadRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad {
        val metadata = soknadMetadataRepository.findByIdOrNull(soknadId) ?: error("Metadata $soknadId finnes ikke")

        val soknad = soknadRepository.findByIdOrNull(soknadId) ?: throw IkkeFunnetException("Soknad finnes ikke")
        return doMapping(soknad, metadata, jsonInternalSoknad)
    }

    internal companion object Mapper {
        fun doMapping(
            domainSoknad: Soknad,
            metadata: SoknadMetadata,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad {
            val personalia =
                JsonPersonalia(
                    personIdentifikator = domainSoknad.toJsonPersonIdentifikator(),
                    navn = JsonSokernavn(kilde = JsonSokernavn.Kilde.SYSTEM, fornavn = "", mellomnavn = "", etternavn = ""),
                    kontonummer = JsonKontonummer(kilde = JsonKilde.SYSTEM),
                )
            val data =
                JsonData(
                    personalia = personalia,
                    begrunnelse = domainSoknad.begrunnelse.toJsonBegrunnelse(),
                    okonomi =
                        JsonOkonomi(
                            opplysninger =
                                JsonOkonomiopplysninger(
                                    utbetaling = emptyList(),
                                    bekreftelse = emptyList(),
                                    beskrivelseAvAnnet = JsonOkonomibeskrivelserAvAnnet(JsonKildeBruker.BRUKER, "", "", "", "", ""),
                                    utgift = null,
                                ),
                            oversikt = JsonOkonomioversikt(inntekt = emptyList(), utgift = emptyList(), formue = emptyList()),
                        ),
                )
            return json.copy(
                soknad =
                    JsonSoknad(
                        version = FORMAT_VERSION,
                        data = data.copy(soknadstype = metadata.soknadType.toJsonSoknadType()),
                        mottaker = JsonSoknadsmottaker(),
                        driftsinformasjon = JsonDriftsinformasjon(inntektFraSkatteetatenFeilet = false),
                        kompatibilitet = emptyList(),
                        innsendingstidspunkt = metadata.tidspunkt.sendtInn?.let(TimestampUtil::convertToOffsettDateTimeUTCString),
                    ),
            )
        }

        private fun Soknad.toJsonPersonIdentifikator() = JsonPersonIdentifikator(JsonPersonIdentifikator.Kilde.SYSTEM, eierPersonId)

        private const val FORMAT_VERSION = "1.0.11"

        private fun Begrunnelse.toJsonBegrunnelse(): JsonBegrunnelse =
            JsonBegrunnelse(JsonKildeBruker.BRUKER, handleKategorier(), hvorforSoke)
    }
}

private fun SoknadType.toJsonSoknadType(): JsonData.Soknadstype =
    when (this == SoknadType.KORT) {
        true -> JsonData.Soknadstype.KORT
        false -> JsonData.Soknadstype.STANDARD
    }

private fun Begrunnelse.handleKategorier(): String =
    if (kategorier.isNotEmpty()) KategorierStringBuilder(kategorier).writeString() else hvaSokesOm

private fun Kategorier.isNotEmpty() = definerte.isNotEmpty() || annet.isNotEmpty()

private class KategorierStringBuilder(kategorier: Kategorier) {
    private val nodhjelpList = kategorier.definerte.filter { it.isNodhjelp() }
    private val resten: List<Kategori> = kategorier.definerte.filter { !it.isNodhjelp() }
    private val annet: String? = kategorier.annet.let { it.ifEmpty { null } }

    fun writeString(): String {
        return """
            |${if (nodhjelpList.isNotEmpty()) writeList("Nødhjelp", nodhjelpList) else "" }
            |${if (resten.isNotEmpty()) writeList("Jeg søker om penger til", resten) else "" }
            |${annet?.let { "Annet:\n $annet " } ?: "" }
            """.trimMargin()
    }

    private fun writeList(
        type: String,
        list: List<Kategori>,
    ): String {
        return "$type: ${list.joinToString(", ") { it.mapToString() }}"
    }
}

private fun Kategori.mapToString(): String {
    return when (this) {
        Kategori.HUSLEIE -> "Husleie"
        Kategori.LIVSOPPHOLD -> "Livsopphold"
        Kategori.STROM_OPPVARMING -> "Strom og oppvarming"
        Kategori.NODHJELP_IKKE_BOSTED -> "Har ikke bosted"
        Kategori.NODHJELP_IKKE_MAT -> "Har ikke mat"
        Kategori.NODHJELP_IKKE_STROM -> "Har ikke strøm"
    }
}
