package no.nav.sosialhjelp.soknad.business.service.systemdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.personalia.person.PersonService;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.ENKE;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.GIFT;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.SAMBOER;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.SEPARERT;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.SKILT;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.UGIFT;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilieSystemdataTest {

    private static final String EIER = "12345678901";
    private static final Ektefelle EKTEFELLE = new Ektefelle("Av", "Og", "På", LocalDate.parse("1993-02-01"), "11111111111", false, false);
    private static final Ektefelle TOM_EKTEFELLE = new Ektefelle("", "", "", null, null, false, true);
    private static final Ektefelle EKTEFELLE_MED_DISKRESJONSKODE = new Ektefelle(true);

    private static final String FORNAVN_BARN = "Rudolf";
    private static final String MELLOMNAVN_BARN = "Rød På";
    private static final String ETTERNAVN_BARN = "Nesen";
    private static final LocalDate FODSELSDATO_BARN = LocalDate.parse("2001-02-03");
    private static final String FNR_BARN = "22222222222";
    private static final boolean ER_FOLKEREGISTRERT_SAMMEN_BARN = true;
    private static final boolean HAR_DELT_BOSTED_BARN = true;

    private static final String FORNAVN_BARN_2 = "Unna";
    private static final String MELLOMNAVN_BARN_2 = "Vei";
    private static final String ETTERNAVN_BARN_2= "Herkommerjeg";
    private static final LocalDate FODSELSDATO_BARN_2 = LocalDate.parse("2003-02-01");
    private static final String FNR_BARN_2 = "33333333333";
    private static final boolean ER_FOLKEREGISTRERT_SAMMEN_BARN_2 = false;
    private static final Integer SAMVARSGRAD_BARN_2 = 25;

    private static final String FORNAVN_BARN_3 = "Jula";
    private static final String MELLOMNAVN_BARN_3 = "Varer Helt Til";
    private static final String ETTERNAVN_BARN_3= "Påske";
    private static final LocalDate FODSELSDATO_BARN_3 = LocalDate.parse("2003-02-05");
    private static final Integer SAMVARSGRAD_BARN_3 = 30;

    private static final Barn BARN = new Barn(FORNAVN_BARN, MELLOMNAVN_BARN, ETTERNAVN_BARN, FNR_BARN, FODSELSDATO_BARN, ER_FOLKEREGISTRERT_SAMMEN_BARN);
    private static final Barn BARN_2 = new Barn(FORNAVN_BARN_2, MELLOMNAVN_BARN_2, ETTERNAVN_BARN_2, FNR_BARN_2, FODSELSDATO_BARN_2, ER_FOLKEREGISTRERT_SAMMEN_BARN_2);

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

    private static final JsonAnsvar JSON_ANSVAR_3_BRUKERREGISTRERT = new JsonAnsvar()
            .withBarn(new JsonBarn()
                    .withKilde(JsonKilde.BRUKER)
                    .withNavn(new JsonNavn()
                            .withFornavn(FORNAVN_BARN_3)
                            .withMellomnavn(MELLOMNAVN_BARN_3)
                            .withEtternavn(ETTERNAVN_BARN_3))
                    .withFodselsdato(FODSELSDATO_BARN_3.toString()))
            .withBorSammenMed(new JsonBorSammenMed()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(false))
            .withSamvarsgrad(new JsonSamvarsgrad()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(SAMVARSGRAD_BARN_3));

    private final ObjectWriter writer;
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @Mock
    private PersonService personService;

    @InjectMocks
    private FamilieSystemdata familieSystemdata;

    @Test
    void skalSetteSivilstatusGiftMedEktefelle() throws JsonProcessingException {
        Person person = createPerson(GIFT.toString(), EKTEFELLE);
        when(personService.hentPerson(anyString())).thenReturn(person);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(sivilstatus.getStatus()).isEqualTo(GIFT);
        assertThatEktefelleIsCorrectlyConverted(EKTEFELLE, sivilstatus.getEktefelle());
        assertThat(sivilstatus.getEktefelleHarDiskresjonskode()).isFalse();
        assertThat(sivilstatus.getFolkeregistrertMedEktefelle()).isFalse();
        assertThat(sivilstatus.getBorSammenMed()).isNull();
    }

    @Test
    void skalIkkeSetteSivilstatusDersomEktefelleMangler() throws JsonProcessingException {
        Person person = createPerson(GIFT.toString(), null);
        when(personService.hentPerson(anyString())).thenReturn(person);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus).isNull();
    }

    @Test
    void skalIkkeSetteSivilstatusDersomAnnetEnnGift() throws JsonProcessingException {
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
    void skalSetteSivilstatusGiftMedTomEktefelleDersomEktefelleHarDiskresjonskode() throws JsonProcessingException {
        Person person = createPerson(GIFT.toString(), EKTEFELLE_MED_DISKRESJONSKODE);
        when(personService.hentPerson(anyString())).thenReturn(person);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(sivilstatus.getStatus()).isEqualTo(GIFT);
        assertThatEktefelleIsCorrectlyConverted(TOM_EKTEFELLE, sivilstatus.getEktefelle());
        assertThat(sivilstatus.getEktefelleHarDiskresjonskode()).isTrue();
        assertThat(sivilstatus.getFolkeregistrertMedEktefelle()).isFalse();
        assertThat(sivilstatus.getBorSammenMed()).isNull();
    }

    @Test
    void skalSetteForsorgerpliktMedFlereBarn() throws JsonProcessingException {
        when(personService.hentBarnForPerson(anyString())).thenReturn(asList(BARN, BARN_2));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isTrue();
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        JsonAnsvar ansvar = ansvarList.get(0);
        JsonAnsvar ansvar_2 = ansvarList.get(1);
        assertThat(ansvar.getBarn().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(ansvar_2.getBarn().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatAnsvarIsCorrectlyConverted(BARN, ansvar);
        assertThatAnsvarIsCorrectlyConverted(BARN_2, ansvar_2);
    }

    @Test
    void skalIkkeSetteForsorgerplikt() throws JsonProcessingException {
        when(personService.hentBarnForPerson(anyString())).thenReturn(emptyList());
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isFalse();
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        assertThat(ansvarList.isEmpty()).isTrue();
    }

    @Test
    void skalIkkeOverskriveBrukerregistrerteBarnNaarDetFinnesSystemBarn() throws JsonProcessingException {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(true))
                .withAnsvar(asList(JSON_ANSVAR, JSON_ANSVAR_2, JSON_ANSVAR_3_BRUKERREGISTRERT));
        when(personService.hentBarnForPerson(anyString())).thenReturn(asList(BARN, BARN_2));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(jsonInternalSoknad);

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isTrue();
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        JsonAnsvar ansvar = ansvarList.get(0);
        JsonAnsvar ansvar_2 = ansvarList.get(1);
        JsonAnsvar ansvar_3 = ansvarList.get(2);
        assertThat(ansvar.getBarn().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(ansvar_2.getBarn().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(ansvar_3.getBarn().getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThatAnsvarIsCorrectlyConverted(BARN, JSON_ANSVAR);
        assertThatAnsvarIsCorrectlyConverted(BARN_2, JSON_ANSVAR_2);
        JsonBarn jsonBarn = ansvar_3.getBarn();
        assertThat(jsonBarn.getNavn().getFornavn()).isEqualTo(FORNAVN_BARN_3);
        assertThat(jsonBarn.getNavn().getMellomnavn()).isEqualTo(MELLOMNAVN_BARN_3);
        assertThat(jsonBarn.getNavn().getEtternavn()).isEqualTo(ETTERNAVN_BARN_3);
    }

    @Test
    void skalIkkeOverskriveBrukerregistrerteBarnEllerForsorgerpliktVerdiNaarDetIkkeFinnesSystemBarn() throws JsonProcessingException {
        when(personService.hentBarnForPerson(anyString())).thenReturn(emptyList());
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                        .withKilde(JsonKilde.BRUKER)
                        .withVerdi(true))
                .withAnsvar(asList(JSON_ANSVAR_3_BRUKERREGISTRERT));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(jsonInternalSoknad);

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isTrue();
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        JsonAnsvar ansvar = ansvarList.get(0);
        assertThat(ansvar.getBarn().getKilde()).isEqualTo(JsonKilde.BRUKER);
        JsonBarn jsonBarn = ansvar.getBarn();
        assertThat(jsonBarn.getNavn().getFornavn()).isEqualTo(FORNAVN_BARN_3);
        assertThat(jsonBarn.getNavn().getMellomnavn()).isEqualTo(MELLOMNAVN_BARN_3);
        assertThat(jsonBarn.getNavn().getEtternavn()).isEqualTo(ETTERNAVN_BARN_3);
    }

    @Test
    void skalIkkeOverskriveSamvaersgradOgHarDeltBostedOgBarnebidrag() throws JsonProcessingException {
        when(personService.hentBarnForPerson(anyString())).thenReturn(asList(BARN, BARN_2));
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithBarnWithUserFilledInfoOnSystemBarn());

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isTrue();
        assertThat(forsorgerplikt.getBarnebidrag().getKilde()).isEqualTo(JsonKildeBruker.BRUKER);
        assertThat(forsorgerplikt.getBarnebidrag().getVerdi()).isEqualTo(JsonBarnebidrag.Verdi.BEGGE);
        List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
        JsonAnsvar ansvar = ansvarList.get(0);
        JsonAnsvar ansvar_2 = ansvarList.get(1);
        assertThat(ansvar.getBarn().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(ansvar_2.getBarn().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatAnsvarIsCorrectlyConverted(BARN, JSON_ANSVAR);
        assertThatAnsvarIsCorrectlyConverted(BARN_2, JSON_ANSVAR_2);
    }

    private void sivilstatusSkalIkkeSettes(JsonSivilstatus.Status status, Ektefelle ektefelle) throws JsonProcessingException {
        Person person = createPerson(status.toString(), ektefelle);

        when(personService.hentPerson(anyString())).thenReturn(person);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        familieSystemdata.updateSystemdataIn(soknadUnderArbeid);

        String internalSoknad = writer.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad());
        ensureValidInternalSoknad(internalSoknad);

        JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();

        assertThat(sivilstatus).isNull();
    }

    private JsonInternalSoknad createJsonInternalSoknadWithBarnWithUserFilledInfoOnSystemBarn() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(true))
                .withAnsvar(asList(JSON_ANSVAR, JSON_ANSVAR_2))
                .withBarnebidrag(new JsonBarnebidrag()
                        .withKilde(JsonKildeBruker.BRUKER)
                        .withVerdi(JsonBarnebidrag.Verdi.BEGGE));
        return jsonInternalSoknad;
    }

    private JsonInternalSoknad createJsonInternalSoknadWithBarn(List<JsonAnsvar> ansvar) {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(true))
                .withAnsvar(ansvar);
        return jsonInternalSoknad;
    }

    private void assertThatAnsvarIsCorrectlyConverted(Barn barn, JsonAnsvar jsonAnsvar) {
        JsonBarn jsonBarn = jsonAnsvar.getBarn();

        assertThat(barn.getFolkeregistrertSammen()).isEqualTo(jsonAnsvar.getErFolkeregistrertSammen() == null ? null : jsonAnsvar.getErFolkeregistrertSammen().getVerdi());

        assertThat(barn.getFnr()).isEqualTo(jsonBarn.getPersonIdentifikator());
        if (barn.getFodselsdato() != null){
            assertThat(barn.getFodselsdato()).hasToString(jsonBarn.getFodselsdato());
        } else {
            assertThat(jsonBarn.getFodselsdato()).isNull();
        }
        assertThat(barn.getFornavn()).isEqualTo(jsonBarn.getNavn().getFornavn());
        assertThat(barn.getMellomnavn()).isEqualTo(jsonBarn.getNavn().getMellomnavn());
        assertThat(barn.getEtternavn()).isEqualTo(jsonBarn.getNavn().getEtternavn());
    }

    private void assertThatEktefelleIsCorrectlyConverted(Ektefelle ektefelle, JsonEktefelle jsonEktefelle) {
        if (ektefelle.getFodselsdato() != null){
            assertThat(ektefelle.getFodselsdato()).hasToString(jsonEktefelle.getFodselsdato());
        } else {
            assertThat(jsonEktefelle.getFodselsdato()).isNull();
        }
        assertThat(ektefelle.getFnr()).isEqualTo(jsonEktefelle.getPersonIdentifikator());
        assertThat(ektefelle.getFornavn()).isEqualTo(jsonEktefelle.getNavn().getFornavn());
        assertThat(ektefelle.getMellomnavn()).isEqualTo(jsonEktefelle.getNavn().getMellomnavn());
        assertThat(ektefelle.getEtternavn()).isEqualTo(jsonEktefelle.getNavn().getEtternavn());
    }

    private Person createPerson(String sivilstatus, Ektefelle ektefelle) {
        return new Person("fornavn", "mellomnavn", "etternavn", EIER, sivilstatus, emptyList(), ektefelle, null, null, null);
    }
}
