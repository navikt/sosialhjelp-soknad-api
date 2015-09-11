package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.joda.time.LocalDate;
import org.junit.Test;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.SENDES_IKKE;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.SEND_SENERE;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.VEDLEGG_ALLEREDE_SENDT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class TransformersTest {
    @Test
    public void skalMappeDatoType() {
        Faktum faktum = new Faktum()
                .medProperty("datotil", "2013-01-01")
                .medProperty("redusertfra", "2013-01-02")
                .medProperty("konkursdato", "2013-01-03")
                .medProperty("permiteringsperiodedatofra", "2013-01-04")
                .medProperty("type", "Kontrakt utgått");


        assertThat(Transformers.DATO_TIL.transform(faktum), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", "Avskjediget")), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", "Redusert arbeidstid")), is(equalTo(new LocalDate("2013-01-02"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", "Arbeidsgiver er konkurs")), is(equalTo(new LocalDate("2013-01-03"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", "Sagt opp av arbeidsgiver")), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", "Sagt opp selv")), is(equalTo(new LocalDate("2013-01-01"))));
        assertThat(Transformers.DATO_TIL.transform(faktum.medProperty("type", "tullball")), is(nullValue()));
        assertThat(Transformers.DATO_TIL_PERMITTERING.transform(faktum.medProperty("type", "Permittert")), is(equalTo(new LocalDate("2013-01-04"))));
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
