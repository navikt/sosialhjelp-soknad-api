package no.nav.sbl.dialogarena.konto;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class UtenlandskKontoTest {

    private final UtenlandskKonto konto = new UtenlandskKonto();

    @Test
    public void swiftBlirUpperCase() {
        konto.setSwift("Shedno22");
        assertThat(konto.getSwift(), is("SHEDNO22"));
    }

    @Test
    public void bankadresseBlirUppercase() {
        konto.setBankadresse1("ürkel allé 54");
        konto.setBankadresse2("54022 ärçœ");
        konto.setBankadresse3("úsbèkìstáñ");

        assertThat(konto.getBankadresse1(), is("ÜRKEL ALLÉ 54"));
        assertThat(konto.getBankadresse2(), is("54022 ÄRÇŒ"));
        assertThat(konto.getBankadresse3(), is("ÚSBÈKÌSTÁÑ"));
    }

    @Test
    public void banknavnBlirUppercase() {
        konto.setBanknavn("ürkelbank1");
        assertThat(konto.getBanknavn(), is("ÜRKELBANK1"));
    }

    @Test
    public void bankkodeBlirUppercase() {
        konto.setBankkode("aeiouy");
        assertThat(konto.getBankkode(), is("AEIOUY"));
    }

    @Test
    public void kontonummerBlirUppercase() {
        konto.setBankkontonummer("12-aIEx-489762");
        assertThat(konto.getBankkontonummer(), is("12-AIEX-489762"));
    }

}
