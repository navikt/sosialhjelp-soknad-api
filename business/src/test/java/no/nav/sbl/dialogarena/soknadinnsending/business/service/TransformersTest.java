package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TransformersTest {
    @Test
    public void skalMappeDatoType() {
        Faktum faktum = new Faktum()
                .medProperty("datotil", "2013-01-01")
                .medProperty("redusertfra", "2013-01-02")
                .medProperty("konkursdato", "2013-01-03")
                .medProperty("permiteringsperiodedatofra", "2013-01-04")
                .medProperty("type", KONTRAKT_UTGAATT);


        assertThat(Transformers.DATO_TIL.transform(faktum), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", AVSKJEDIGET)), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", REDUSERT_ARBEIDSTID)), is(equalTo(new LocalDate("2013-01-02"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", ARBEIDSGIVER_ERKONKURS)), is(equalTo(new LocalDate("2013-01-03"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", SAGTOPP_AV_ARBEIDSGIVER)), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", SAGTOPP_SELV)), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", "tullball")), is(nullValue()));
        assertThat(Transformers.DATO_TIL_PERMITTERING.transform(faktum.medProperty("type", DagpengerUtils.PERMITTERT)), is(equalTo(new LocalDate("2013-01-04"))));
    }

    @Test
    public void skalKonvertereInnsendingsvalg(){
        assertThat(Transformers.toXmlInnsendingsvalg(Vedlegg.Status.LastetOpp), is(equalTo(LASTET_OPP.toString())));
        assertThat(Transformers.toXmlInnsendingsvalg(Vedlegg.Status.SendesIkke), is(equalTo(SENDES_IKKE.toString())));
        assertThat(Transformers.toXmlInnsendingsvalg(Vedlegg.Status.SendesSenere), is(equalTo(SEND_SENERE.toString())));
        assertThat(Transformers.toXmlInnsendingsvalg(Vedlegg.Status.IkkeVedlegg), is(equalTo(SENDES_IKKE.toString())));
        assertThat(Transformers.toXmlInnsendingsvalg(Vedlegg.Status.VedleggAlleredeSendt), is(equalTo(VEDLEGG_ALLEREDE_SENDT.toString())));
    }
}
