package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PdlPersonTest {

    @Test
    public void harAdressebeskyttelse() {
        PdlPerson p1 = personMedAdressebeskyttelse(null);
        PdlPerson p2 = personMedAdressebeskyttelse(Collections.emptyList());
        PdlPerson p3 = personMedAdressebeskyttelse(List.of(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)));
        PdlPerson p4 = personMedAdressebeskyttelse(List.of(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG)));
        PdlPerson p5 = personMedAdressebeskyttelse(List.of(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG_UTLAND)));
        PdlPerson p6 = personMedAdressebeskyttelse(List.of(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)));
        PdlPerson p7 = personMedAdressebeskyttelse(List.of(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG), new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)));

        assertFalse(p1.harAdressebeskyttelse());
        assertFalse(p2.harAdressebeskyttelse());
        assertFalse(p3.harAdressebeskyttelse());
        assertTrue(p4.harAdressebeskyttelse());
        assertTrue(p5.harAdressebeskyttelse());
        assertTrue(p6.harAdressebeskyttelse());
        assertTrue(p7.harAdressebeskyttelse());
    }

    private PdlPerson personMedAdressebeskyttelse(List<AdressebeskyttelseDto> adressebeskyttelse) {
        return new PdlPerson(adressebeskyttelse, null, null, null, null, null, null);
    }
}