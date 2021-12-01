package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService;
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
class KontonummerSystemdataTest {

    private static final String EIER = "12345678901";
    private static final String KONTONUMMER_SYSTEM = "12345678903";
    private static final String KONTONUMMER_BRUKER = "11223344556";

    @Mock
    private KontonummerService kontonummerService;

    @InjectMocks
    private KontonummerSystemdata kontonummerSystemdata;

    @Test
    void skalOppdatereKontonummer() {

        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getKontonummer().getVerdi()).isEqualTo(KONTONUMMER_SYSTEM);
    }

    @Test
    void skalOppdatereKontonummerOgFjerneUlovligeSymboler() {

        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM + " !#¤%&/()=?`-<>|§,.-* ");

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonPersonalia.getKontonummer().getVerdi()).isEqualTo(KONTONUMMER_SYSTEM);
    }

    @Test
    void skalIkkeOppdatereKontonummerDersomKildeErBruker() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUserDefinedKontonummer());

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(jsonPersonalia.getKontonummer().getVerdi()).isEqualTo(KONTONUMMER_BRUKER);
    }

    @Test
    void skalSetteNullDersomKontonummerErTomStreng() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn("");

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(jsonPersonalia.getKontonummer().getVerdi()).isNull();
    }

    @Test
    void skalSetteNullDersomKontonummerErNull() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(kontonummerService.getKontonummer(anyString())).thenReturn(null);

        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var jsonPersonalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        assertThat(jsonPersonalia.getKontonummer().getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(jsonPersonalia.getKontonummer().getVerdi()).isNull();
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUserDefinedKontonummer() {
        var jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getPersonalia()
                .setKontonummer(new JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi(KONTONUMMER_BRUKER));
        return jsonInternalSoknad;
    }
}
