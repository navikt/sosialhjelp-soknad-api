package no.nav.sosialhjelp.soknad.domain.model.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.isNonProduction;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceUtilsTest {

    @AfterEach
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    void isNonProduction_skalGiTrue_forNonProd() {
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
    void isNonProduction_skalGiFalse_forProd() {
        System.setProperty("environment.name", "p");
        assertThat(isNonProduction()).isFalse();
        System.setProperty("environment.name", "prod");
        assertThat(isNonProduction()).isFalse();
        System.setProperty("environment.name", "prod-sbs");
        assertThat(isNonProduction()).isFalse();
    }

    @Test
    void isNonProduction_skalGiFalse_forUkjentMiljo() {
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