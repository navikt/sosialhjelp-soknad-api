package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class BarnBolkTest {
    private static final String BARN_IDENT = "***REMOVED***";
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";

    private static final String BARN2_IDENT = "***REMOVED***";
    private static final String BARN2_FORNAVN = "Per";
    private static final String BARN2_ETTERNAVN = "Barnet";
    private static final Long SOKNAD_ID = 21L;

    @InjectMocks
    private BarnBolk barnBolk;

    @Mock
    private PersonService personMock;

    private List<Barn> barn;

    @Before
    public void setup() {
        barn = new ArrayList<>();
        when(personMock.hentBarn(anyString())).thenReturn(barn);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void skalMappeBarnTilFaktum() {
        barn.add(lagBarn(BARN_IDENT, BARN_FORNAVN, BARN_ETTERNAVN));
        barn.add(lagBarn(BARN2_IDENT, BARN2_FORNAVN, BARN2_ETTERNAVN));
        List<Faktum> faktums = barnBolk.genererSystemFakta(BARN_IDENT, SOKNAD_ID);
        assertThat(faktums.size(), equalTo(2));
        assertThat(faktums.get(0).getProperties().get("fnr"), equalTo(BARN_IDENT));
        assertThat(faktums.get(1).getProperties().get("fnr"), equalTo(BARN2_IDENT));
    }

    private Barn lagBarn(String ident, String fornavn, String etternavn) {
        return new Barn(SOKNAD_ID, null, "", ident, fornavn, "", etternavn);
    }
}
