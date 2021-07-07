package no.nav.sosialhjelp.soknad.domain.model.util;

import org.junit.After;
import org.junit.Test;

import static no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.feilmeldingUtenFnr;
import static no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.isNonProduction;
import static org.assertj.core.api.Assertions.assertThat;

public class ServiceUtilsTest {


    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void skalStrippeVekkFnutter() {
        String utenFnutter = ServiceUtils.stripVekkFnutter("\"123\"");
        assertThat(utenFnutter).isEqualTo("123");
    }

    @Test
    public void skalFjerne_alleFnr_fraFeilmelding() {
        String str = "12121212121 feilmelding som har flere fnr 12345678911 og 11111111111";

        String res = feilmeldingUtenFnr(str);

        assertThat(res).isEqualTo("[FNR] feilmelding som har flere fnr [FNR] og [FNR]");
    }

    @Test
    public void skalFjerne_fnr_fraUrl() {
        String str = "/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/11111111111/oppgave/inntekt?fnr=12121212121&a=b";

        String res = feilmeldingUtenFnr(str);

        assertThat(res).isEqualTo("/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/[FNR]/oppgave/inntekt?fnr=[FNR]&a=b");
    }

    @Test
    public void skalIkkeFjerne_12siffretTall_fraFeilmelding() {
        String str = "feilmelding som har for langt fnr 111112222233";

        String res = feilmeldingUtenFnr(str);

        assertThat(res).isEqualTo(str);
    }

    @Test
    public void skalIkkeFjerne_10siffretTall_fraFeilmelding() {
        String str = "feilmelding som har for kort fnr 1111122222";

        String res = feilmeldingUtenFnr(str);

        assertThat(res).isEqualTo(str);
    }

    @Test
    public void skalFjerne_11siffretTallWrappetMedHermetegn_fraFeilmelding() {
        String str = "feilmelding som har fnr \"12345612345\"";

        String res = feilmeldingUtenFnr(str);

        assertThat(res).isEqualTo("feilmelding som har fnr \"[FNR]\"");
    }

    @Test
    public void skalIkkeFeile_medNull_iFeilmelding() {
        String res = feilmeldingUtenFnr(null);

        assertThat(res).isNull();
    }

    @Test
    public void isNonProduction_skalGiTrue_forNonProd() {
        System.setProperty("environment.name", "q0");
        assertThat(isNonProduction()).isTrue();
        System.setProperty("environment.name", "q1");
        assertThat(isNonProduction()).isTrue();
        System.setProperty("environment.name", "labs-gcp");
        assertThat(isNonProduction()).isTrue();
        System.setProperty("environment.name", "dev-gcp");
        assertThat(isNonProduction()).isTrue();
        System.setProperty("environment.name", "local");
        assertThat(isNonProduction()).isTrue();
        System.setProperty("environment.name", "test");
        assertThat(isNonProduction()).isTrue();
    }

    @Test
    public void isNonProduction_skalGiFalse_forProd() {
        System.setProperty("environment.name", "p");
        assertThat(isNonProduction()).isFalse();
        System.setProperty("environment.name", "prod");
        assertThat(isNonProduction()).isFalse();
        System.setProperty("environment.name", "prod-sbs");
        assertThat(isNonProduction()).isFalse();
    }

    @Test
    public void isNonProduction_skalGiFalse_forUkjentMiljo() {
        System.clearProperty("environment.name");
        assertThat(isNonProduction()).isFalse();
        System.setProperty("environment.name", "");
        assertThat(isNonProduction()).isFalse();
        System.setProperty("environment.name", "ukjent");
        assertThat(isNonProduction()).isFalse();
        System.setProperty("environment.name", "mock");
        assertThat(isNonProduction()).isFalse();
    }
}