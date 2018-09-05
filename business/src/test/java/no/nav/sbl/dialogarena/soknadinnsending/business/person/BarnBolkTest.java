package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class BarnBolkTest {
    private static final String IDENT = "03076321565";
    private static final String BARN_IDENT = "01010091736";
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_MELLOMNAVN = "Navn";
    private static final String BARN_ETTERNAVN = "Barnet";
    private static final LocalDate BARN_FODSELSDATO = new LocalDate(2000, 1, 1);
    private static final String BARN_FODSELSDATO_TEKST = "2000-01-01";

    private static final String BARN2_IDENT = "01010081336";
    private static final String BARN2_FORNAVN = "Per";
    private static final String BARN2_ETTERNAVN = "Barnet";
    private static final LocalDate BARN2_FODSELSDATO = new LocalDate(2008, 7, 23);
    private static final String BARN2_FODSELSDATO_TEKST = "2008-07-23";
    private static final Long SOKNAD_ID = 21L;

    @InjectMocks
    private BarnBolk barnBolk;

    @Mock
    private PersonService personServiceMock;

    @Test
    public void genererSystemFaktaLagerIkkeFaktaForBarnForBrukerUtenBarn() {
        when(personServiceMock.hentBarn(anyString())).thenReturn(new ArrayList<>());

        List<Faktum> fakta = barnBolk.genererSystemFakta(IDENT, SOKNAD_ID);
        Faktum harBarn = fakta.get(0);

        assertThat(fakta.size(), is(1));
        assertThat(harBarn.getSoknadId(), is(SOKNAD_ID));
        assertThat(harBarn.getKey(), is("system.familie.barn"));
        assertThat(harBarn.getType(), is(SYSTEMREGISTRERT));
        assertThat(harBarn.getValue(), is("false"));
    }

    @Test
    public void genererSystemFaktaLagerToFaktaForBarnForBrukerMedEtBarn() {
        when(personServiceMock.hentBarn(anyString())).thenReturn(lagListeMedBarn(1));

        List<Faktum> fakta = barnBolk.genererSystemFakta(IDENT, SOKNAD_ID);
        Faktum harBarn = fakta.get(0);

        assertThat(fakta.size(), is(2));
        assertThat(harBarn.getSoknadId(), is(SOKNAD_ID));
        assertThat(harBarn.getKey(), is("system.familie.barn"));
        assertThat(harBarn.getType(), is(SYSTEMREGISTRERT));
        assertThat(harBarn.getValue(), is("true"));
    }

    @Test
    public void genererSystemFaktaForBarnLagerFaktaMedKorrektInfoForToBarn() {
        List<Faktum> fakta = barnBolk.genererSystemFaktaForBarn(lagListeMedBarn(2), SOKNAD_ID);
        Map<String, String> barn = fakta.get(0).getProperties();
        Map<String, String> barn2 = fakta.get(1).getProperties();

        assertThat(fakta.size(), is(2));
        assertThat(fakta.get(0).getKey(), is("system.familie.barn.true.barn"));
        assertThat(fakta.get(0).getUnikProperty(), is("fnr"));
        assertThat(barn.get("fnr"), is(BARN_IDENT));
        assertThat(barn.get("fornavn"), is(BARN_FORNAVN));
        assertThat(barn.get("mellomnavn"), is(BARN_MELLOMNAVN));
        assertThat(barn.get("etternavn"), is(BARN_ETTERNAVN));
        assertThat(barn.get("fodselsdato"), is(BARN_FODSELSDATO_TEKST));
        assertThat(barn.get("ikketilgangtilbarn"), is("false"));
        assertThat(barn.get("folkeregistrertsammen"), is("true"));
        assertThat(barn.get("utvandret"), is("false"));

        assertThat(fakta.get(1).getKey(), is("system.familie.barn.true.barn"));
        assertThat(fakta.get(1).getUnikProperty(), is("fnr"));
        assertThat(barn2.get("fnr"), is(BARN2_IDENT));
        assertThat(barn2.get("fornavn"), is(BARN2_FORNAVN));
        assertThat(barn2.get("mellomnavn"), nullValue());
        assertThat(barn2.get("etternavn"), is(BARN2_ETTERNAVN));
        assertThat(barn2.get("fodselsdato"), is(BARN2_FODSELSDATO_TEKST));
        assertThat(barn2.get("ikketilgangtilbarn"), is("false"));
        assertThat(barn2.get("folkeregistrertsammen"), is("false"));
        assertThat(barn2.get("utvandret"), is("true"));
    }

    @Test
    public void genererSystemFaktaForBarnFeilerIkkeHvisBarnErNull() {
        List<Barn> barn = lagListeMedBarn(1);
        barn.add(null);

        List<Faktum> fakta = barnBolk.genererSystemFaktaForBarn(barn, SOKNAD_ID);

        assertThat(fakta.size(), is(1));
    }

    @Test
    public void genererSystemFaktaForBarnLagerRiktigFaktumForBarnMedDiskresjonskode() {
        List<Faktum> fakta = barnBolk.genererSystemFaktaForBarn(lagListeMedEtBarnMedDiskresjonskode(), SOKNAD_ID);
        Map<String, String> barn = fakta.get(0).getProperties();

        assertThat(fakta.size(), is(1));
        assertThat(barn.get("fnr"), nullValue());
        assertThat(barn.get("fornavn"), nullValue());
        assertThat(barn.get("mellomnavn"), nullValue());
        assertThat(barn.get("etternavn"), nullValue());
        assertThat(barn.get("fodselsdato"), nullValue());
        assertThat(barn.get("ikketilgangtilbarn"), is("true"));
        assertThat(barn.get("folkeregistrertsammen"), is("false"));
        assertThat(barn.get("utvandret"), is("false"));
    }

    private List<Barn> lagListeMedBarn(int antallBarn) {
        List<Barn> barn = new ArrayList<>();
        barn.add(new Barn().withFnr(BARN_IDENT)
                .withFornavn(BARN_FORNAVN)
                .withMellomnavn(BARN_MELLOMNAVN)
                .withEtternavn(BARN_ETTERNAVN)
                .withFodselsdato(BARN_FODSELSDATO)
                .withFolkeregistrertsammen(true)
                .withUtvandret(false)
                .withIkkeTilgang(false));

        if (antallBarn == 2) {
            barn.add(new Barn().withFnr(BARN2_IDENT)
                    .withFornavn(BARN2_FORNAVN)
                    .withEtternavn(BARN2_ETTERNAVN)
                    .withFodselsdato(BARN2_FODSELSDATO)
                    .withFolkeregistrertsammen(false)
                    .withUtvandret(true)
                    .withIkkeTilgang(false));
        }
        return barn;
    }

    private List<Barn> lagListeMedEtBarnMedDiskresjonskode() {
        List<Barn> barn = new ArrayList<>();
        barn.add(new Barn().withIkkeTilgang(true));
        return barn;
    }
}
