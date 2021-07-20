package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelefonnummerSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String TELEFONNUMMER_SYSTEM = "98765432";
    private static final String TELEFONNUMMER_BRUKER = "+4723456789";

    @Mock
    private DkifService dkifService;

    @InjectMocks
    private TelefonnummerSystemdata telefonnummerSystemdata;

    @Test
    void skalOppdatereTelefonnummerUtenLandkode() {
        when(dkifService.hentMobiltelefonnummer(anyString())).thenReturn(TELEFONNUMMER_SYSTEM);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getTelefonnummer().getVerdi()).isEqualTo("+47" + TELEFONNUMMER_SYSTEM);
    }

    @Test
    void skalOppdatereTelefonnummerMedLandkode() {
        when(dkifService.hentMobiltelefonnummer(anyString())).thenReturn("+47" + TELEFONNUMMER_SYSTEM);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getTelefonnummer().getVerdi()).isEqualTo("+47" + TELEFONNUMMER_SYSTEM);
    }

    @Test
    void skalIkkeOppdatereTelefonnummerDersomKildeErBruker() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUserDefinedTelefonnummer());

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer().getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(jsonPersonalia.getTelefonnummer().getVerdi()).isEqualTo(TELEFONNUMMER_BRUKER);
    }

    @Test
    void skalSetteNullDersomTelefonnummerErTomStreng() {
        when(dkifService.hentMobiltelefonnummer(anyString())).thenReturn("");
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer()).isNull();
    }

    @Test
    void skalSetteNullDersomTelefonnummerErNull() {
        when(dkifService.hentMobiltelefonnummer(anyString())).thenReturn(null);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonPersonalia jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getTelefonnummer()).isNull();
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUserDefinedTelefonnummer() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getPersonalia()
                .setTelefonnummer(new JsonTelefonnummer().withKilde(JsonKilde.BRUKER).withVerdi(TELEFONNUMMER_BRUKER));
        return jsonInternalSoknad;
    }
}
