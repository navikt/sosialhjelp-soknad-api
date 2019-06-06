package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
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
public class TelefonnummerSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String TELEFONNUMMER_SYSTEM = "98765432";
    private static final String TELEFONNUMMER_BRUKER = "+4723456789";

    @Mock
    private PersonaliaFletter personaliaFletter;

    @InjectMocks
    private TelefonnummerSystemdata telefonnummerSystemdata;

    @Test
    public void skalOppdatereTelefonnummerUtenLandkode() {
        Personalia personalia = new Personalia();
        personalia.setMobiltelefonnummer(TELEFONNUMMER_SYSTEM);
        personalia.setErUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid);

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getTelefonnummer().getVerdi(), is("+47" + TELEFONNUMMER_SYSTEM));
    }

    @Test
    public void skalOppdatereTelefonnummerMedLandkode() {
        Personalia personalia = new Personalia();
        personalia.setMobiltelefonnummer("+47" + TELEFONNUMMER_SYSTEM);
        personalia.setErUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid);

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer().getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonPersonalia.getTelefonnummer().getVerdi(), is("+47" + TELEFONNUMMER_SYSTEM));
    }

    @Test
    public void skalIkkeOppdatereTelefonnummerDersomKildeErBruker() {
        Personalia personalia = new Personalia();
        personalia.setMobiltelefonnummer(TELEFONNUMMER_SYSTEM);
        personalia.setErUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUserDefinedTelefonnummer());
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid);

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer().getKilde(), is(JsonKilde.BRUKER));
        assertThat(jsonPersonalia.getTelefonnummer().getVerdi(), is(TELEFONNUMMER_BRUKER));
    }

    @Test
    public void skalSetteNullDersomTelefonnummerErTomStreng() {
        Personalia personalia = new Personalia();
        personalia.setMobiltelefonnummer("");
        personalia.setErUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid);

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer(), nullValue());
    }

    @Test
    public void skalSetteNullDersomTelefonnummerErNull() {
        Personalia personalia = new Personalia();
        personalia.setMobiltelefonnummer(null);
        personalia.setErUtenlandskBankkonto(false);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid);

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer(), nullValue());
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUserDefinedTelefonnummer() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getPersonalia()
                .setTelefonnummer(new JsonTelefonnummer().withKilde(JsonKilde.BRUKER).withVerdi(TELEFONNUMMER_BRUKER));
        return jsonInternalSoknad;
    }
}
