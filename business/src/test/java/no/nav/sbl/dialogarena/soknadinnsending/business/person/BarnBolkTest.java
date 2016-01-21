package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(value = MockitoJUnitRunner.class)
public class BarnBolkTest {
    private static final String BARN_IDENT = "01010091736";
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";

    private static final String BARN2_IDENT = "01010081336";
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
