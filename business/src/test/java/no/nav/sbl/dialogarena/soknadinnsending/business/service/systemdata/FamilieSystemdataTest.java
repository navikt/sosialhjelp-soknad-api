package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FamilieSystemdataTest {

    private static final String EIER = "12345678901";
    private static final Ektefelle EKTEFELLE = new Ektefelle()
            .withFornavn("Av")
            .withMellomnavn("Og")
            .withEtternavn("På")
            .withFnr("01029312345")
            .withFodselsdato(LocalDate.parse("1993-02-01"));

    private static final Ektefelle TOM_EKTEFELLE = new Ektefelle()
            .withFornavn("")
            .withMellomnavn("")
            .withEtternavn("");

    private static final Ektefelle EKTEFELLE_MED_DISKRESJONSKODE = new Ektefelle().withIkketilgangtilektefelle(true);

    private static final Barn BARN = new Barn()
            .withFornavn("Rudolf")
            .withMellomnavn("Rød På")
            .withEtternavn("Nesen")
            .withFodselsdato(LocalDate.parse("2001-02-03"))
            .withFnr("03020154321")
            .withFolkeregistrertsammen(true)
            .withIkkeTilgang(false);

    private static final Barn BARN_2 = new Barn()
            .withFornavn("Unna")
            .withMellomnavn("Vei")
            .withEtternavn("Herkommerjeg")
            .withFodselsdato(LocalDate.parse("2003-02-01"))
            .withFnr("01020312345")
            .withFolkeregistrertsammen(false)
            .withIkkeTilgang(false);

    private static final Barn BARN_MED_DISKRESJONSKODE = new Barn().withIkkeTilgang(true);

    private static final Barn TOMT_BARN_MED_DISKRESJONSKODE = new Barn().withIkkeTilgang(true)
            .withFornavn("")
            .withMellomnavn("")
            .withEtternavn("");

    private final ObjectWriter writer;
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @Mock
    private PersonaliaFletter personaliaFletter;

    @Mock
    private PersonService personService;

    @InjectMocks
    private FamilieSystemdata familieSystemdata;

    @Test
    public void skalSetteSivilstatusGiftMedEktefelle() throws JsonProcessingException {
        Personalia personalia = new Personalia();
        personalia.setSivilstatus(GIFT.toString());
        personalia.setEktefelle(EKTEFELLE);
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(sivilstatus.getStatus(), is(GIFT));
        assertThatEktefelleIsCorrectlyConverted(EKTEFELLE, sivilstatus.getEktefelle());
        assertThat(sivilstatus.getEktefelleHarDiskresjonskode(), is(false));
        assertThat(sivilstatus.getFolkeregistrertMedEktefelle(), is(false));
        assertThat(sivilstatus.getBorSammenMed(), nullValue());
    }

    @Test
    public void skalSetteSivilstatusGiftMedTomEktefelleDersomEktefelleMangler() throws JsonProcessingException {
        Personalia personalia = new Personalia();
        personalia.setSivilstatus(GIFT.toString());
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(sivilstatus.getStatus(), is(GIFT));
        assertThatEktefelleIsCorrectlyConverted(TOM_EKTEFELLE, sivilstatus.getEktefelle());
        assertThat(sivilstatus.getEktefelleHarDiskresjonskode(), nullValue());
        assertThat(sivilstatus.getFolkeregistrertMedEktefelle(), nullValue());
        assertThat(sivilstatus.getBorSammenMed(), nullValue());
    }

    @Test
    public void skalSetteSivilstatusGiftMedTomEktefelleDersomEktefelleHarDiskresjonskode() throws JsonProcessingException {
        Personalia personalia = new Personalia();
        personalia.setSivilstatus(GIFT.toString());
        personalia.setEktefelle(EKTEFELLE_MED_DISKRESJONSKODE);
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(sivilstatus.getStatus(), is(GIFT));
        assertThatEktefelleIsCorrectlyConverted(TOM_EKTEFELLE, sivilstatus.getEktefelle());
        assertThat(sivilstatus.getEktefelleHarDiskresjonskode(), is(true));
        assertThat(sivilstatus.getFolkeregistrertMedEktefelle(), is(false));
        assertThat(sivilstatus.getBorSammenMed(), nullValue());
    }

    @Test
    public void skalSetteAndreSivilstatuserEnnGift() throws JsonProcessingException {
        skalSetteSivilstatusSomIkkeErGift(UGIFT, null);
        skalSetteSivilstatusSomIkkeErGift(SAMBOER, null);
        skalSetteSivilstatusSomIkkeErGift(ENKE, null);
        skalSetteSivilstatusSomIkkeErGift(SKILT, null);
        skalSetteSivilstatusSomIkkeErGift(SEPARERT, null);
    }

    @Test
    public void skalIkkeHaMedEktefelleForAndreSivilstatuserEnnGift() throws JsonProcessingException {
        skalSetteSivilstatusSomIkkeErGift(UGIFT, EKTEFELLE);
        skalSetteSivilstatusSomIkkeErGift(SAMBOER, EKTEFELLE);
        skalSetteSivilstatusSomIkkeErGift(ENKE, EKTEFELLE);
        skalSetteSivilstatusSomIkkeErGift(SKILT, EKTEFELLE);
        skalSetteSivilstatusSomIkkeErGift(SEPARERT, EKTEFELLE);
    }

    @Test
    public void skalSetteForsorgerpliktMedFlereBarn() throws JsonProcessingException {
        when(personService.hentBarn(anyString())).thenReturn(Arrays.asList(BARN, BARN_2));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi(), is(true));
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        JsonAnsvar ansvar = ansvarList.get(0);
        JsonAnsvar ansvar_2 = ansvarList.get(1);
        assertThat(ansvar.getBarn().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(ansvar_2.getBarn().getKilde(), is(JsonKilde.SYSTEM));
        assertThatAnsvarIsCorrectlyConverted(BARN, ansvar);
        assertThatAnsvarIsCorrectlyConverted(BARN_2, ansvar_2);
    }

    @Test
    public void skalIkkeSetteForsorgerplikt() throws JsonProcessingException {
        when(personService.hentBarn(anyString())).thenReturn(Collections.emptyList());
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi(), is(false));
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        assertThat(ansvarList.isEmpty(), is(true));
    }

    @Test
    public void skalSetteTomtBarnDersomDiskresjonskode() throws JsonProcessingException {
        when(personService.hentBarn(anyString())).thenReturn(Collections.singletonList(BARN_MED_DISKRESJONSKODE));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi(), is(true));
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        JsonAnsvar ansvar = ansvarList.get(0);
        assertThat(ansvar.getBarn().getKilde(), is(JsonKilde.SYSTEM));
        assertThatAnsvarIsCorrectlyConverted(TOMT_BARN_MED_DISKRESJONSKODE, ansvar);
    }

    private void assertThatAnsvarIsCorrectlyConverted(Barn barn, JsonAnsvar jsonAnsvar) {
        JsonBarn jsonBarn = jsonAnsvar.getBarn();

        assertThat("harDiskresjonskode", barn.harIkkeTilgang(), is(jsonBarn.getHarDiskresjonskode()));
        assertThat("erFolkeregistrertSammen", barn.erFolkeregistrertsammen(),
                is(jsonAnsvar.getErFolkeregistrertSammen() == null ? null : jsonAnsvar.getErFolkeregistrertSammen().getVerdi()));

        assertThat("fodselsnummer", barn.getFnr(), is(jsonBarn.getPersonIdentifikator()));
        if (barn.getFodselsdato() != null){
            assertThat("fodselsdato", barn.getFodselsdato().toString(), is(jsonBarn.getFodselsdato()));
        } else {
            assertThat("fodselsdato", jsonBarn.getFodselsdato(), nullValue());
        }
        assertThat("fornavn", barn.getFornavn(), is(jsonBarn.getNavn().getFornavn()));
        assertThat("mellomnavn", barn.getMellomnavn(), is(jsonBarn.getNavn().getMellomnavn()));
        assertThat("etternavn", barn.getEtternavn(), is(jsonBarn.getNavn().getEtternavn()));
    }

    private void skalSetteSivilstatusSomIkkeErGift(JsonSivilstatus.Status status, Ektefelle ektefelle) throws JsonProcessingException {
        Personalia personalia = new Personalia();
        personalia.setSivilstatus(status.toString());
        personalia.setEktefelle(ektefelle);
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(sivilstatus.getStatus(), is(status));
        assertThat(sivilstatus.getEktefelle(), nullValue());
        assertThat(sivilstatus.getEktefelleHarDiskresjonskode(), nullValue());
        assertThat(sivilstatus.getFolkeregistrertMedEktefelle(), nullValue());
        assertThat(sivilstatus.getBorSammenMed(), nullValue());
    }

    private void assertThatEktefelleIsCorrectlyConverted(Ektefelle ektefelle, JsonEktefelle jsonEktefelle) {
        if (ektefelle.getFodselsdato() != null){
            assertThat("fodselsdato", ektefelle.getFodselsdato().toString(), is(jsonEktefelle.getFodselsdato()));
        } else {
            assertThat("fodselsdato", jsonEktefelle.getFodselsdato(), nullValue());
        }
        assertThat("fnr", ektefelle.getFnr(), is(jsonEktefelle.getPersonIdentifikator()));
        assertThat("fornavn", ektefelle.getFornavn(), is(jsonEktefelle.getNavn().getFornavn()));
        assertThat("mellomnavn", ektefelle.getMellomnavn(), is(jsonEktefelle.getNavn().getMellomnavn()));
        assertThat("etternavn", ektefelle.getEtternavn(), is(jsonEktefelle.getNavn().getEtternavn()));
    }
}
