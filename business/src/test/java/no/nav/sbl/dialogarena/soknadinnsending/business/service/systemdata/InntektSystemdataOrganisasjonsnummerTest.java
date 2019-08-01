package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import org.junit.Test;

import static org.junit.Assert.*;

public class InntektSystemdataOrganisasjonsnummerTest {

    @Test
    public void skalReturnereTrueOmGyldigOrganisasjonsnummer() {
        String organisasjonsnummer = "089640782";
        boolean result = InntektSystemdata.isOrganisasjonsnummer(organisasjonsnummer);
        assertTrue(result);
    }

    @Test
    public void skalReturnereFalseOmOrganisasjonsnummerInneholderTekst() {
        String organisasjonsnummer = "o89640782";
        boolean result = InntektSystemdata.isOrganisasjonsnummer(organisasjonsnummer);
        assertFalse(result);
    }

    @Test
    public void skalReturnereFalseOmForKortOrganisasjonsnummer() {
        String nummer = "12345678";
        boolean result = InntektSystemdata.isOrganisasjonsnummer(nummer);
        assertFalse(result);
    }

    @Test
    public void skalReturnereFalseOmForLangtOrganisasjonsnummer() {
        String nummer = "12346";
        boolean result = InntektSystemdata.isOrganisasjonsnummer(nummer);
        assertFalse(result);

        String personnummer = "01010011111";
        result = InntektSystemdata.isOrganisasjonsnummer(personnummer);
        assertFalse(result);
    }
}