package no.nav.sosialhjelp.soknad.nymodell.producer.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.GateAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BegrunnelseKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataKeyValue
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataKeyValueStore
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.BrukerdataMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.adresse.toJsonAdresseValg
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(BrukerdataMapper::class)
class BrukerdataMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var brukerdataRepository: BrukerdataRepository

    @Autowired
    private lateinit var brukerdataMapper: BrukerdataMapper

    @Test
    fun `Map Brukerdata til JsonInternalSoknad`() {
        val lagretSoknad = opprettSoknad()

        Brukerdata(
            soknadId = lagretSoknad.id,
            valgtAdresse = AdresseValg.SOKNAD,
            oppholdsadresse = createAdresseObject(),
            keyValueStore = BrukerdataKeyValueStore(createSetOfKeyValuePairs())
        ).also { brukerdataRepository.save(it) }

        val jsonInternalSoknad = JsonInternalSoknad().apply {
            createChildrenIfNotExists()
            brukerdataMapper.mapDomainToJson(lagretSoknad.id, this)
        }

        with (jsonInternalSoknad) {
            soknad.data.personalia.let {
                assertThat(it.oppholdsadresse.type).isEqualTo(JsonAdresse.Type.GATEADRESSE)
                assertThat(it.oppholdsadresse.adresseValg).isEqualTo(AdresseValg.SOKNAD.toJsonAdresseValg())
                assertThat(it.telefonnummer.verdi).isEqualTo("42332944")
            }

            soknad.data.begrunnelse.let {
                assertThat(it.hvaSokesOm).isEqualTo("Penger")
                assertThat(it.hvorforSoke).isEqualTo("Jeg må!")
            }

            soknad.data.okonomi.opplysninger.beskrivelseAvAnnet.let {
                assertThat(it.verdi).isEqualTo("Verdier")
                assertThat(it.sparing).isEqualTo("Har spart mye penger!")
                assertThat(it.boutgifter).isEqualTo("Har masse hus")
            }
        }
    }
    fun createAdresseObject(): AdresseObject {
        return GateAdresseObject(
            kommunenummer = "2320",
            husnummer = "17",
            gatenavn = "Denneveien"
        )
    }

    fun createSetOfKeyValuePairs(): MutableSet<BrukerdataKeyValue> {
        return mutableSetOf(
            BrukerdataKeyValue(GenerelleDataKey.TELEFONNUMMER, "42332944"),
            BrukerdataKeyValue(BegrunnelseKey.HVA_SOKES_OM, "Penger"),
            BrukerdataKeyValue(BegrunnelseKey.HVORFOR_SOKE, "Jeg må!"),
            BrukerdataKeyValue(BeskrivelseAvAnnetKey.SPARING, "Har spart mye penger!"),
            BrukerdataKeyValue(BeskrivelseAvAnnetKey.VERDI, "Verdier"),
            BrukerdataKeyValue(BeskrivelseAvAnnetKey.BOUTGIFTER, "Har masse hus")
        )
    }
}
