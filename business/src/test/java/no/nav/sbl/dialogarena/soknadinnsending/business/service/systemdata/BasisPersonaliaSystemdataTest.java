package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson.PersonSammenligner;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BasisPersonaliaSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String FORNAVN = "Aragorn";
    private static final String MELLOMNAVN = "Elessar";
    private static final String ETTERNAVN = "Telcontar";
    private static final String NORSK_STATSBORGERSKAP = "NOR";
    private static final String NORDISK_STATSBORGERSKAP = "FIN";
    private static final String IKKE_NORDISK_STATSBORGERSKAP = "GER";

    @Mock
    private PersonService personService;

    @Mock
    private PdlService pdlService;

    @Mock
    private PersonSammenligner personSammenligner;

    @InjectMocks
    private BasisPersonaliaSystemdata basisPersonaliaSystemdata;

    @Test
    public void skalIkkeOppdatereDersomPersonaliaErNull() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(null);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getPersonIdentifikator().getKilde(), is(JsonPersonIdentifikator.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getPersonIdentifikator().getVerdi(), is(EIER));
        assertThat(jsonPersonalia.getNavn().getKilde(), is(JsonSokernavn.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getNavn().getFornavn(), is(""));
        assertThat(jsonPersonalia.getNavn().getMellomnavn(), is(""));
        assertThat(jsonPersonalia.getNavn().getEtternavn(), is(""));
        assertThat(jsonPersonalia.getStatsborgerskap(), nullValue());
        assertThat(jsonPersonalia.getNordiskBorger(), nullValue());
    }

    @Test
    public void skalOppdatereNordiskPersonalia() {
        Person person = new Person()
                .withFornavn(FORNAVN)
                .withMellomnavn(MELLOMNAVN)
                .withEtternavn(ETTERNAVN)
                .withStatsborgerskap(List.of(NORSK_STATSBORGERSKAP));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getPersonIdentifikator().getKilde(), is(JsonPersonIdentifikator.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getPersonIdentifikator().getVerdi(), is(EIER));
        assertThat(jsonPersonalia.getNavn().getKilde(), is(JsonSokernavn.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getNavn().getFornavn(), is(FORNAVN));
        assertThat(jsonPersonalia.getNavn().getMellomnavn(), is(MELLOMNAVN));
        assertThat(jsonPersonalia.getNavn().getEtternavn(), is(ETTERNAVN));
        assertThat(jsonPersonalia.getStatsborgerskap().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi(), is(NORSK_STATSBORGERSKAP));
        assertThat(jsonPersonalia.getNordiskBorger().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi(), is(true));
    }

    @Test
    public void skalPrioritereNorskOverNordiskStatsborgerskap() {
        Person person = new Person()
                .withFornavn(FORNAVN)
                .withMellomnavn(MELLOMNAVN)
                .withEtternavn(ETTERNAVN)
                .withStatsborgerskap(List.of(NORDISK_STATSBORGERSKAP, NORSK_STATSBORGERSKAP));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getStatsborgerskap().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi(), is(NORSK_STATSBORGERSKAP));
        assertThat(jsonPersonalia.getNordiskBorger().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi(), is(true));
    }

    @Test
    public void skalPrioritereNordiskStatsborgerskap() {
        Person person = new Person()
                .withFornavn(FORNAVN)
                .withMellomnavn(MELLOMNAVN)
                .withEtternavn(ETTERNAVN)
                .withStatsborgerskap(List.of(IKKE_NORDISK_STATSBORGERSKAP, NORDISK_STATSBORGERSKAP));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getStatsborgerskap().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi(), is(NORDISK_STATSBORGERSKAP));
        assertThat(jsonPersonalia.getNordiskBorger().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi(), is(true));
    }

    @Test
    public void skalOppdatereIkkeNordiskPersonalia() {
        Person person = new Person()
                .withFornavn(FORNAVN)
                .withMellomnavn(MELLOMNAVN)
                .withEtternavn(ETTERNAVN)
                .withStatsborgerskap(List.of(IKKE_NORDISK_STATSBORGERSKAP));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getPersonIdentifikator().getKilde(), is(JsonPersonIdentifikator.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getPersonIdentifikator().getVerdi(), is(EIER));
        assertThat(jsonPersonalia.getNavn().getKilde(), is(JsonSokernavn.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getNavn().getFornavn(), is(FORNAVN));
        assertThat(jsonPersonalia.getNavn().getMellomnavn(), is(MELLOMNAVN));
        assertThat(jsonPersonalia.getNavn().getEtternavn(), is(ETTERNAVN));
        assertThat(jsonPersonalia.getStatsborgerskap().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getStatsborgerskap().getVerdi(), is(IKKE_NORDISK_STATSBORGERSKAP));
        assertThat(jsonPersonalia.getNordiskBorger().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getNordiskBorger().getVerdi(), is(false));
    }

    //Denne skal fjernes når TPS har oppdatert til å bruke ukjent som XXX istedenfor ???
    @Test
    public void skalikkeSendeMedStatsborgerskapForUkjent() {
        Person person = new Person()
                .withFornavn(FORNAVN)
                .withMellomnavn(MELLOMNAVN)
                .withEtternavn(ETTERNAVN)
                .withStatsborgerskap(List.of("???"));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentPerson(anyString())).thenReturn(person);

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getPersonIdentifikator().getKilde(), is(JsonPersonIdentifikator.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getPersonIdentifikator().getVerdi(), is(EIER));
        assertThat(jsonPersonalia.getNavn().getKilde(), is(JsonSokernavn.Kilde.SYSTEM));
        assertThat(jsonPersonalia.getNavn().getFornavn(), is(FORNAVN));
        assertThat(jsonPersonalia.getNavn().getMellomnavn(), is(MELLOMNAVN));
        assertThat(jsonPersonalia.getNavn().getEtternavn(), is(ETTERNAVN));
        assertThat(jsonPersonalia.getStatsborgerskap(), nullValue());
        assertThat(jsonPersonalia.getNordiskBorger(), nullValue());
    }

    @Test
    public void skalSetteRiktigNordiskBorger() {
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger(null), nullValue());
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("NOR"), is(true));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("SWE"), is(true));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("FRO"), is(true));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("ISL"), is(true));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("DNK"), is(true));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("FIN"), is(true));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("RUS"), is(false));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("DEU"), is(false));
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("GBR"), is(false));
    }
}
