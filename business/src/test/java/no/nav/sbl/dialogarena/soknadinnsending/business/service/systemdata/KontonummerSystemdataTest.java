package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.AdresserOgKontonummer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.BrukerprofilService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KontonummerSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String KONTONUMMER_SYSTEM = "12345678903";
    private static final String KONTONUMMER_BRUKER = "11223344556";

    @Mock
    private BrukerprofilService brukerprofilService;

    @InjectMocks
    private KontonummerSystemdata kontonummerSystemdata;

    @Test
    public void skalOppdatereKontonummer() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer()
                .withKontonummer(KONTONUMMER_SYSTEM)
                .withUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(brukerprofilService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_SYSTEM));
    }

    @Test
    public void skalOppdatereKontonummerOgFjerneUlovligeSymboler() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer()
                .withKontonummer(KONTONUMMER_SYSTEM + " !#¤%&/()=?`-<>|§,.-* ")
                .withUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(brukerprofilService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_SYSTEM));
    }

    @Test
    public void skalIkkeOppdatereKontonummerDersomKildeErBruker() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer()
                .withKontonummer(KONTONUMMER_SYSTEM)
                .withUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUserDefinedKontonummer());
        when(brukerprofilService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), is(KONTONUMMER_BRUKER));
    }

    @Test
    public void skalSetteNullDersomUtenlandskKontonummer() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer()
                .withKontonummer(KONTONUMMER_SYSTEM)
                .withUtenlandskBankkonto(true);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(brukerprofilService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), nullValue());
    }

    @Test
    public void skalSetteNullDersomKontonummerErTomStreng() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer()
                .withKontonummer("")
                .withUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(brukerprofilService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), nullValue());
    }

    @Test
    public void skalSetteNullOgKildeBrukerDersomKontonummerErNull() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer()
                .withKontonummer(null)
                .withUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(brukerprofilService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getKontonummer().getVerdi(), nullValue());
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUserDefinedKontonummer() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getPersonalia()
                .setKontonummer(new JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi(KONTONUMMER_BRUKER));
        return jsonInternalSoknad;
    }
}
