package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.finn.unleash.Unleash;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sosialhjelp.soknad.consumer.personv3.PersonServiceV3;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.Kontonummer;
import no.nav.sosialhjelp.soknad.oppslag.KontonummerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KontonummerSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String KONTONUMMER_SYSTEM = "12345678903";
    private static final String KONTONUMMER_BRUKER = "11223344556";

    @Mock
    private PersonServiceV3 personService;
    @Mock
    private KontonummerService kontonummerService;
    @Mock
    private Unleash unleash;

    @InjectMocks
    private KontonummerSystemdata kontonummerSystemdata;

    @Before
    public void setUp() throws Exception {
        when(unleash.isEnabled(anyString(), anyBoolean())).thenReturn(false);
    }

    @Test
    public void skalOppdatereKontonummer() {
        var kontonummer = new Kontonummer()
                .withKontonummer(KONTONUMMER_SYSTEM);
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentKontonummer(anyString())).thenReturn(kontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_SYSTEM));
        verifyNoInteractions(kontonummerService);
    }

    @Test
    public void skalOppdatereKontonummerOgFjerneUlovligeSymboler() {
        var kontonummer = new Kontonummer()
                .withKontonummer(KONTONUMMER_SYSTEM + " !#¤%&/()=?`-<>|§,.-* ");
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentKontonummer(anyString())).thenReturn(kontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_SYSTEM));
        verifyNoInteractions(kontonummerService);
    }

    @Test
    public void skalIkkeOppdatereKontonummerDersomKildeErBruker() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUserDefinedKontonummer());

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_BRUKER));
        verifyNoInteractions(kontonummerService);
    }

    @Test
    public void skalSetteNullDersomKontonummerErTomStreng() {
        var kontonummer = new Kontonummer()
                .withKontonummer("");
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentKontonummer(anyString())).thenReturn(kontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), nullValue());
        verifyNoInteractions(kontonummerService);
    }

    @Test
    public void skalSetteNullDersomKontonummerErNull() {
        var kontonummer = new Kontonummer()
                .withKontonummer(null);
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentKontonummer(anyString())).thenReturn(kontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), nullValue());
        verifyNoInteractions(kontonummerService);
    }

    @Test
    public void skalOppdatereKontonummer_fraKontonummerService() {
        when(unleash.isEnabled(anyString(), anyBoolean())).thenReturn(true);

        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_SYSTEM));
        verifyNoInteractions(personService);
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUserDefinedKontonummer() {
        var jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getPersonalia()
                .setKontonummer(new JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi(KONTONUMMER_BRUKER));
        return jsonInternalSoknad;
    }
}
