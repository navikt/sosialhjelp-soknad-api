package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.junit.Test;

import java.time.LocalDate;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TransformersTest {
    @Test
    public void skalMappeDatoType() {
        Faktum faktum = new Faktum()
                .medProperty("datotil", "2013-01-01")
                .medProperty("redusertfra", "2013-01-02")
                .medProperty("konkursdato", "2013-01-03")
                .medProperty("permiteringsperiodedatofra", "2013-01-04")
                .medProperty("type", KONTRAKT_UTGAATT);


        assertThat(Transformers.DATO_TIL.apply(faktum), is(equalTo(LocalDate.of(2013, 1, 1))));
        assertThat(Transformers.DATO_TIL.apply(faktum.medProperty("type", AVSKJEDIGET)), is(equalTo(LocalDate.of(2013, 1, 1))));
        assertThat(Transformers.DATO_TIL.apply(faktum.medProperty("type", REDUSERT_ARBEIDSTID)), is(equalTo(LocalDate.of(2013, 1, 2))));
        assertThat(Transformers.DATO_TIL.apply(faktum.medProperty("type", ARBEIDSGIVER_ERKONKURS)), is(equalTo(LocalDate.of(2013, 1, 3))));
        assertThat(Transformers.DATO_TIL.apply(faktum.medProperty("type", SAGTOPP_AV_ARBEIDSGIVER)), is(equalTo(LocalDate.of(2013, 1, 1))));
        assertThat(Transformers.DATO_TIL.apply(faktum.medProperty("type", SAGTOPP_SELV)), is(equalTo(LocalDate.of(2013, 1, 1))));
        assertThat(Transformers.DATO_TIL.apply(faktum.medProperty("type", "tullball")), is(nullValue()));
    }

}
