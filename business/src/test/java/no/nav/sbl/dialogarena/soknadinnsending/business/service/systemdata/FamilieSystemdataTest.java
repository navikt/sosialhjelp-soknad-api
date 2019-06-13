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
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Sivilstand;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Sivilstander;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.finnSivilstatus;
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

    private static final String FORNAVN_BARN = "Rudolf";
    private static final String MELLOMNAVN_BARN = "Rød På";
    private static final String ETTERNAVN_BARN = "Nesen";
    private static final LocalDate FODSELSDATO_BARN = LocalDate.parse("2001-02-03");
    private static final String FNR_BARN = "03020154321";
    private static final boolean ER_FOLKEREGISTRERT_SAMMEN_BARN = true;
    private static final boolean HAR_DELT_BOSTED_BARN = true;

    private static final String FORNAVN_BARN_2 = "Unna";
    private static final String MELLOMNAVN_BARN_2 = "Vei";
    private static final String ETTERNAVN_BARN_2= "Herkommerjeg";
    private static final LocalDate FODSELSDATO_BARN_2 = LocalDate.parse("2003-02-01");
    private static final String FNR_BARN_2 = "01020312345";
    private static final boolean ER_FOLKEREGISTRERT_SAMMEN_BARN_2 = false;
    private static final Integer SAMVARSGRAD_BARN_2 = 25;

    private static final Barn BARN = new Barn()
            .withFornavn(FORNAVN_BARN)
            .withMellomnavn(MELLOMNAVN_BARN)
            .withEtternavn(ETTERNAVN_BARN)
            .withFodselsdato(FODSELSDATO_BARN)
            .withFnr(FNR_BARN)
            .withFolkeregistrertsammen(ER_FOLKEREGISTRERT_SAMMEN_BARN)
            .withIkkeTilgang(false);

    private static final Barn BARN_2 = new Barn()
            .withFornavn(FORNAVN_BARN_2)
            .withMellomnavn(MELLOMNAVN_BARN_2)
            .withEtternavn(ETTERNAVN_BARN_2)
            .withFodselsdato(FODSELSDATO_BARN_2)
            .withFnr(FNR_BARN_2)
            .withFolkeregistrertsammen(ER_FOLKEREGISTRERT_SAMMEN_BARN_2)
            .withIkkeTilgang(false);

    private static final JsonAnsvar JSON_ANSVAR = new JsonAnsvar()
            .withBarn(new JsonBarn()
                    .withKilde(JsonKilde.SYSTEM)
                    .withNavn(new JsonNavn()
                            .withFornavn(FORNAVN_BARN)
                            .withMellomnavn(MELLOMNAVN_BARN)
                            .withEtternavn(ETTERNAVN_BARN))
                    .withFodselsdato(FODSELSDATO_BARN.toString())
                    .withPersonIdentifikator(FNR_BARN))
            .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen()
                    .withKilde(JsonKildeSystem.SYSTEM)
                    .withVerdi(ER_FOLKEREGISTRERT_SAMMEN_BARN))
            .withHarDeltBosted(new JsonHarDeltBosted()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(HAR_DELT_BOSTED_BARN));

    private static final JsonAnsvar JSON_ANSVAR_2 = new JsonAnsvar()
            .withBarn(new JsonBarn()
                    .withKilde(JsonKilde.SYSTEM)
                    .withNavn(new JsonNavn()
                            .withFornavn(FORNAVN_BARN_2)
                            .withMellomnavn(MELLOMNAVN_BARN_2)
                            .withEtternavn(ETTERNAVN_BARN_2))
                    .withFodselsdato(FODSELSDATO_BARN_2.toString())
                    .withPersonIdentifikator(FNR_BARN_2))
            .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen()
                    .withKilde(JsonKildeSystem.SYSTEM)
                    .withVerdi(ER_FOLKEREGISTRERT_SAMMEN_BARN_2))
            .withSamvarsgrad(new JsonSamvarsgrad()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(SAMVARSGRAD_BARN_2));

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
    public void skalIkkeSetteSivilstatusDersomEktefelleMangler() throws JsonProcessingException {
        Personalia personalia = new Personalia();
        personalia.setSivilstatus(GIFT.toString());
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus, nullValue());
    }

    @Test
    public void skalIkkeSetteSivilstatusDersomAnnetEnnGift() throws JsonProcessingException {
        sivilstatusSkalIkkeSettes(UGIFT, null);
        sivilstatusSkalIkkeSettes(SAMBOER, null);
        sivilstatusSkalIkkeSettes(ENKE, null);
        sivilstatusSkalIkkeSettes(SKILT, null);
        sivilstatusSkalIkkeSettes(SEPARERT, null);
        sivilstatusSkalIkkeSettes(UGIFT, EKTEFELLE);
        sivilstatusSkalIkkeSettes(SAMBOER, EKTEFELLE);
        sivilstatusSkalIkkeSettes(ENKE, EKTEFELLE);
        sivilstatusSkalIkkeSettes(SKILT, EKTEFELLE);
        sivilstatusSkalIkkeSettes(SEPARERT, EKTEFELLE);
    }

    @Test
    public void alleStatuserFraTPSBlirMappetTilJsonSivilstatus() {
        List<String> muligeTPSKoder = new ArrayList<>(Arrays.asList("GIFT", "GLAD", "REPA", "SAMB", "UGIF", "ENKE", "GJPA", "SEPA", "SEPR", "SKIL", "SKPA"));
        String status;
        Sivilstand sivilstand = new Sivilstand();
        Sivilstander sivilstander = new Sivilstander();
        Person person = new Person();
        for (String tpsKode : muligeTPSKoder){
            sivilstander.setValue(tpsKode);
            sivilstand.setSivilstand(sivilstander);
            person.setSivilstand(sivilstand);
            status = finnSivilstatus(person);
            JsonSivilstatus.Status.fromValue(status);
        }
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
    public void skalIkkeOverskriveSamvaersgradOgHarDeltBostedOgBarnebidrag() throws JsonProcessingException {
        when(personService.hentBarn(anyString())).thenReturn(Arrays.asList(BARN, BARN_2));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithBarnWithUserFilledInfo());

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi(), is(true));
        assertThat(forsorgerplikt.getBarnebidrag().getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(forsorgerplikt.getBarnebidrag().getVerdi(), is(JsonBarnebidrag.Verdi.BEGGE));
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        JsonAnsvar ansvar = ansvarList.get(0);
        JsonAnsvar ansvar_2 = ansvarList.get(1);
        assertThat(ansvar.getBarn().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(ansvar_2.getBarn().getKilde(), is(JsonKilde.SYSTEM));
        assertThatAnsvarIsCorrectlyConverted(BARN, JSON_ANSVAR);
        assertThatAnsvarIsCorrectlyConverted(BARN_2, JSON_ANSVAR_2);
    }

    private void sivilstatusSkalIkkeSettes(JsonSivilstatus.Status status, Ektefelle ektefelle) throws JsonProcessingException {
        Personalia personalia = new Personalia();
        personalia.setSivilstatus(status.toString());
        personalia.setEktefelle(ektefelle);
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus, nullValue());
    }

    private JsonInternalSoknad createJsonInternalSoknadWithBarnWithUserFilledInfo() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(true))
                .withAnsvar(Arrays.asList(JSON_ANSVAR, JSON_ANSVAR_2))
                .withBarnebidrag(new JsonBarnebidrag()
                        .withKilde(JsonKildeBruker.BRUKER)
                        .withVerdi(JsonBarnebidrag.Verdi.BEGGE));
        return jsonInternalSoknad;
    }

    private void assertThatAnsvarIsCorrectlyConverted(Barn barn, JsonAnsvar jsonAnsvar) {
        JsonBarn jsonBarn = jsonAnsvar.getBarn();

        assertThat("erFolkeregistrertSammen", barn.erFolkeregistrertsammen(),
                is(jsonAnsvar.getErFolkeregistrertSammen() == null ? null : jsonAnsvar.getErFolkeregistrertSammen().getVerdi()));

        assertThat("fodselsnummer", barn.getFnr(), is(jsonBarn.getPersonIdentifikator()));
        if (barn.getFodselsdato() != null){
            assertThat("FODSELSDATO_BARN", barn.getFodselsdato().toString(), is(jsonBarn.getFodselsdato()));
        } else {
            assertThat("FODSELSDATO_BARN", jsonBarn.getFodselsdato(), nullValue());
        }
        assertThat("fornavn", barn.getFornavn(), is(jsonBarn.getNavn().getFornavn()));
        assertThat("mellomnavn", barn.getMellomnavn(), is(jsonBarn.getNavn().getMellomnavn()));
        assertThat("etternavn", barn.getEtternavn(), is(jsonBarn.getNavn().getEtternavn()));
    }

    private void assertThatEktefelleIsCorrectlyConverted(Ektefelle ektefelle, JsonEktefelle jsonEktefelle) {
        if (ektefelle.getFodselsdato() != null){
            assertThat("FODSELSDATO_BARN", ektefelle.getFodselsdato().toString(), is(jsonEktefelle.getFodselsdato()));
        } else {
            assertThat("FODSELSDATO_BARN", jsonEktefelle.getFodselsdato(), nullValue());
        }
        assertThat("fnr", ektefelle.getFnr(), is(jsonEktefelle.getPersonIdentifikator()));
        assertThat("fornavn", ektefelle.getFornavn(), is(jsonEktefelle.getNavn().getFornavn()));
        assertThat("mellomnavn", ektefelle.getMellomnavn(), is(jsonEktefelle.getNavn().getMellomnavn()));
        assertThat("etternavn", ektefelle.getEtternavn(), is(jsonEktefelle.getNavn().getEtternavn()));
    }
}
