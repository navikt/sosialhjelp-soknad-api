package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.person.domain.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.service.systemdata.BasisPersonaliaSystemdata.PDL_UKJENT_STATSBORGERSKAP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasisPersonaliaSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String FORNAVN = "Aragorn";
    private static final String MELLOMNAVN = "Elessar";
    private static final String ETTERNAVN = "Telcontar";
    private static final String NORSK_STATSBORGERSKAP = "NOR";
    private static final String NORDISK_STATSBORGERSKAP = "FIN";
    private static final String IKKE_NORDISK_STATSBORGERSKAP = "GER";

    @Mock
    private PersonService personService;

    @InjectMocks
    private BasisPersonaliaSystemdata basisPersonaliaSystemdata;

    @Test
    void skalIkkeOppdatereDersomPersonaliaErNull() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(null);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getPersonIdentifikator().getKilde()).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM);
        assertThat(jsonPersonalia.getPersonIdentifikator().getVerdi()).isEqualTo(EIER);
        assertThat(jsonPersonalia.getNavn().getKilde()).isEqualTo(JsonSokernavn.Kilde.SYSTEM);
        assertThat(jsonPersonalia.getNavn().getFornavn()).isBlank();
        assertThat(jsonPersonalia.getNavn().getMellomnavn()).isBlank();
        assertThat(jsonPersonalia.getNavn().getEtternavn()).isBlank();
        assertThat(jsonPersonalia.getStatsborgerskap()).isNull();
        assertThat(jsonPersonalia.getNordiskBorger()).isNull();
    }

    @Test
    void skalOppdatereNordiskPersonalia() {
        var person = new Person(FORNAVN, MELLOMNAVN, ETTERNAVN, EIER, "ugift", List.of(NORSK_STATSBORGERSKAP), null, null, null, null);
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getPersonIdentifikator().getKilde()).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM);
        assertThat(jsonPersonalia.getPersonIdentifikator().getVerdi()).isEqualTo(EIER);
        assertThat(jsonPersonalia.getNavn().getKilde()).isEqualTo(JsonSokernavn.Kilde.SYSTEM);
        assertThat(jsonPersonalia.getNavn().getFornavn()).isEqualTo(FORNAVN);
        assertThat(jsonPersonalia.getNavn().getMellomnavn()).isEqualTo(MELLOMNAVN);
        assertThat(jsonPersonalia.getNavn().getEtternavn()).isEqualTo(ETTERNAVN);
        assertThat(jsonPersonalia.getStatsborgerskap().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi()).isEqualTo(NORSK_STATSBORGERSKAP);
        assertThat(jsonPersonalia.getNordiskBorger().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi()).isTrue();
    }

    @Test
    void skalPrioritereNorskOverNordiskStatsborgerskap() {
        var person = new Person(FORNAVN, MELLOMNAVN, ETTERNAVN, EIER, "ugift", List.of(NORDISK_STATSBORGERSKAP, NORSK_STATSBORGERSKAP), null, null, null, null);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getStatsborgerskap().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi()).isEqualTo(NORSK_STATSBORGERSKAP);
        assertThat(jsonPersonalia.getNordiskBorger().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi()).isTrue();
    }

    @Test
    void skalPrioritereNordiskStatsborgerskap() {
        var person = new Person(FORNAVN, MELLOMNAVN, ETTERNAVN, EIER, "ugift", List.of(IKKE_NORDISK_STATSBORGERSKAP, NORDISK_STATSBORGERSKAP), null, null, null, null);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getStatsborgerskap().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi()).isEqualTo(NORDISK_STATSBORGERSKAP);
        assertThat(jsonPersonalia.getNordiskBorger().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi()).isTrue();
    }

    @Test
    void skalOppdatereIkkeNordiskPersonalia() {
        var person = new Person(FORNAVN, MELLOMNAVN, ETTERNAVN, EIER, "ugift", List.of(IKKE_NORDISK_STATSBORGERSKAP), null, null, null, null);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getPersonIdentifikator().getKilde()).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM);
        assertThat(jsonPersonalia.getPersonIdentifikator().getVerdi()).isEqualTo(EIER);
        assertThat(jsonPersonalia.getNavn().getKilde()).isEqualTo(JsonSokernavn.Kilde.SYSTEM);
        assertThat(jsonPersonalia.getNavn().getFornavn()).isEqualTo(FORNAVN);
        assertThat(jsonPersonalia.getNavn().getMellomnavn()).isEqualTo(MELLOMNAVN);
        assertThat(jsonPersonalia.getNavn().getEtternavn()).isEqualTo(ETTERNAVN);
        assertThat(jsonPersonalia.getStatsborgerskap().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi()).isEqualTo(IKKE_NORDISK_STATSBORGERSKAP);
        assertThat(jsonPersonalia.getNordiskBorger().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi()).isFalse();
    }

    //Denne skal fjernes når TPS har oppdatert til å bruke ukjent som XXX istedenfor ???
    @Test
    void skalikkeSendeMedStatsborgerskapForUkjent_TPS() {
        var person = new Person(FORNAVN, MELLOMNAVN, ETTERNAVN, EIER, "ugift", List.of("???"), null, null, null, null);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getStatsborgerskap()).isNull();
        assertThat(jsonPersonalia.getNordiskBorger()).isNull();
    }

    @Test
    void skalikkeSendeMedStatsborgerskapForUkjent_PDL() {
        var person = new Person(FORNAVN, MELLOMNAVN, ETTERNAVN, EIER, "ugift", List.of(PDL_UKJENT_STATSBORGERSKAP), null, null, null, null);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getStatsborgerskap()).isNull();
        assertThat(jsonPersonalia.getNordiskBorger()).isNull();
    }

    @Test
    void skalSetteRiktigNordiskBorger() {
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger(null)).isNull();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("NOR")).isTrue();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("SWE")).isTrue();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("FRO")).isTrue();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("ISL")).isTrue();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("DNK")).isTrue();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("FIN")).isTrue();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("RUS")).isFalse();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("DEU")).isFalse();
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("GBR")).isFalse();
    }
}
