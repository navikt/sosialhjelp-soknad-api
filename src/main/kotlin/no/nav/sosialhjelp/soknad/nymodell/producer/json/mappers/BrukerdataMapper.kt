package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BegrunnelseKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BegrunnelseKey.HVA_SOKES_OM
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BegrunnelseKey.HVORFOR_SOKE
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey.BARNEUTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey.BOUTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey.SPARING
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey.UTBETALING
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey.VERDI
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataKeyValue
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey.KOMMENTAR_ARBEIDSFORHOLD
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey.KONTONUMMER
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey.TELEFONNUMMER
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.Samtykke
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.SamtykkeType
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.adresse.toJsonAdresseValg
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.adresse.toTypedJsonAdresse
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import org.springframework.stereotype.Component
import java.util.*

@Component
class BrukerdataMapper(
    private val brukerdataRepository: BrukerdataRepository
) : DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad) {
        val brukerdata = brukerdataRepository.findById(soknadId).get()
        brukerdata.also {
            if (it.valgtAdresse == AdresseValg.SOKNAD) {
                json.addOppholdsadresse(it.toJsonAdresse())
            }
            it.mapSamtykker(json)
            it.mapKeyValueData(json)
        }
    }

    private fun JsonInternalSoknad.addOppholdsadresse(jsonAdresse: JsonAdresse) {
        soknad.data.apply {
            if (personalia == null) personalia = JsonPersonalia()
            personalia.withOppholdsadresse(jsonAdresse)
        }
    }

    private fun Brukerdata.toJsonAdresse() = oppholdsadresse
        ?.run {
            toTypedJsonAdresse()
                .withKilde(JsonKilde.BRUKER)
                .withAdresseValg(valgtAdresse?.toJsonAdresseValg())
        } ?: throw IllegalStateException("Valgt adresse er SOKNAD, men adresse er tom.")

    private fun Brukerdata.mapSamtykker(json: JsonInternalSoknad) {
        samtykker.map { map ->
            json.soknad.data.okonomi.opplysninger
                .bekreftelse.add(map.value.toJsonOkonomibekreftelse(map.key))
        }
    }

    private fun Samtykke.toJsonOkonomibekreftelse(type: SamtykkeType) = JsonOkonomibekreftelse()
        .withKilde(JsonKilde.BRUKER)
        .withType(type.toSoknadJsonType())
        .withVerdi(verdi)
        .withBekreftelsesDato(dato.toString())

    private fun Brukerdata.mapKeyValueData(json: JsonInternalSoknad) {
        keyValueStoreSet.forEach {
            when (it.key) {
                is GenerelleDataKey -> it.mapGenerelleDataKeyValue(json)
                is BegrunnelseKey -> json.addJsonBegrunnelse(it)
                is BeskrivelseAvAnnetKey -> json.addJsonBeskrivelserAvAnnet(it)
                else -> throw IllegalArgumentException("MapToJsonInternalSoknad - BrukerdataKey finnes ikke")
            }
        }
    }

    private fun BrukerdataKeyValue.mapGenerelleDataKeyValue(json: JsonInternalSoknad) {
        when (key as GenerelleDataKey) {
            TELEFONNUMMER -> json.soknad.data.personalia.telefonnummer = toJsonTelefonnummer()
            KONTONUMMER -> json.soknad.data.personalia.kontonummer = toJsonKontonummer()
            KOMMENTAR_ARBEIDSFORHOLD ->
                json.soknad.data.arbeid.kommentarTilArbeidsforhold = toJsonKommentarArbeidsforhold()
        }
    }

    private fun BrukerdataKeyValue.toJsonTelefonnummer() = JsonTelefonnummer()
        .withKilde(JsonKilde.BRUKER)
        .withVerdi(value)

    private fun BrukerdataKeyValue.toJsonKontonummer() = JsonKontonummer()
        .withKilde(JsonKilde.BRUKER)
        .withVerdi(value)

    private fun BrukerdataKeyValue.toJsonKommentarArbeidsforhold() =
        JsonKommentarTilArbeidsforhold().withVerdi(value)

    private fun JsonInternalSoknad.addJsonBegrunnelse(keyValue: BrukerdataKeyValue) {
        soknad.data.apply {
            if (begrunnelse == null) withBegrunnelse(JsonBegrunnelse())
            when (keyValue.key as BegrunnelseKey) {
                HVORFOR_SOKE -> begrunnelse.withHvorforSoke(keyValue.value)
                HVA_SOKES_OM -> begrunnelse.withHvaSokesOm(keyValue.value)
            }
        }
    }

    private fun JsonInternalSoknad.addJsonBeskrivelserAvAnnet(keyValue: BrukerdataKeyValue) {
        soknad.data.okonomi.opplysninger.apply {
            if (beskrivelseAvAnnet == null) withBeskrivelseAvAnnet(JsonOkonomibeskrivelserAvAnnet())
            beskrivelseAvAnnet.apply {
                when (keyValue.key as BeskrivelseAvAnnetKey) {
                    BARNEUTGIFTER -> barneutgifter = keyValue.value
                    BOUTGIFTER -> boutgifter = keyValue.value
                    SPARING -> sparing = keyValue.value
                    VERDI -> verdi = keyValue.value
                    UTBETALING -> utbetaling = keyValue.value
                }
            }
        }
    }
}
