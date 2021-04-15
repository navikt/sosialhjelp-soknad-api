package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.oppslag.KontonummerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KontonummerSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String KONTONUMMER_SYSTEM = "12345678903";
    private static final String KONTONUMMER_BRUKER = "11223344556";

    @Mock
    private KontonummerService kontonummerService;

    @InjectMocks
    private KontonummerSystemdata kontonummerSystemdata;

    @Test
    public void skalOppdatereKontonummer() {

        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_SYSTEM));
    }

    @Test
    public void skalOppdatereKontonummerOgFjerneUlovligeSymboler() {

        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM + " !#¤%&/()=?`-<>|§,.-* ");

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_SYSTEM));
    }

    @Test
    public void skalIkkeOppdatereKontonummerDersomKildeErBruker() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUserDefinedKontonummer());

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_BRUKER));
    }

    @Test
    public void skalSetteNullDersomKontonummerErTomStreng() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn("");

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), nullValue());
    }

    @Test
    public void skalSetteNullDersomKontonummerErNull() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn(null);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), nullValue());
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUserDefinedKontonummer() {
        var jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getPersonalia()
                .setKontonummer(new JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi(KONTONUMMER_BRUKER));
        return jsonInternalSoknad;
    }
}
