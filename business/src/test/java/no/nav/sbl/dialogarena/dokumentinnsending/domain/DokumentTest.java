package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTERNT_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTRA_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.HOVEDSKJEMA;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.avType;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.harValg;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.IKKE_VALGT;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.INNSENDT;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.SEND_I_POST;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DokumentTest {

    @Test
    public void shouldReturnTrueIfComparingObjectUsingEqualsToItself() {
        Dokument dokument = new Dokument(EKSTERNT_VEDLEGG);
        assertThat(dokument.equals(dokument), is(true));
    }

    @Test
    public void shouldReturnTrueIfComparingObjectUsingEqualsToDokumentWithSameKodeverksId() {
        Dokument dokument1 = new Dokument(EKSTERNT_VEDLEGG);
        dokument1.setKodeverkId("1");

        Dokument dokument2 = new Dokument(EKSTERNT_VEDLEGG);
        dokument2.setKodeverkId("1");

        Boolean isSame = dokument1.equals(dokument2);

        assertThat(isSame, is(true));
    }

    @Test
    public void shouldReturnFalseIfComparingObjectUsingEqualsToNull() {
        Dokument dokument = new Dokument(EKSTERNT_VEDLEGG);

        Boolean isSame = dokument.equals(null); //NOPMD

        assertThat(isSame, is(false));
    }

    @Test
    public void shouldReturnFalseIfComparingObjectUsingEqualsToObjectOfAnotherClass() {
        Dokument dokument = new Dokument(EKSTERNT_VEDLEGG);

        Boolean isSame = dokument.equals(new DokumentSoknad());

        assertThat(isSame, is(false));
    }

    @Test
    public void skalTesteErErIkke() {
        Dokument dokument = new Dokument(EKSTRA_VEDLEGG);
        dokument.setInnsendingsvalg(INNSENDT);
        assertThat(dokument.er(EKSTRA_VEDLEGG), is(true));
        assertThat(dokument.er(HOVEDSKJEMA), is(false));
        assertThat(dokument.erIkke(EKSTRA_VEDLEGG), is(false));
        assertThat(dokument.erIkke(HOVEDSKJEMA), is(true));
        assertThat(dokument.er(INNSENDT), is(true));
        assertThat(dokument.er(SEND_I_POST), is(false));
        assertThat(dokument.erIkke(INNSENDT), is(false));
        assertThat(dokument.erIkke(SEND_I_POST), is(true));
    }

    @Test
    public void skalTestePredikater() {
        Dokument dokument = new Dokument(EKSTRA_VEDLEGG);
        dokument.setInnsendingsvalg(INNSENDT);

        assertThat(avType(EKSTRA_VEDLEGG).evaluate(dokument), is(true));
        assertThat(avType(HOVEDSKJEMA).evaluate(dokument), is(false));
        assertThat(harValg(INNSENDT).evaluate(dokument), is(true));
        assertThat(harValg(IKKE_VALGT).evaluate(dokument), is(false));
        assertThat(Dokument.harIkkeValg(INNSENDT).evaluate(dokument), is(false));
        assertThat(Dokument.harIkkeValg(IKKE_VALGT).evaluate(dokument), is(true));
    }

    @Test
    public void skalKloneDatoer() {
        Dokument dokument = new Dokument(EKSTRA_VEDLEGG);
        dokument.setDokumentInnhold(new DokumentInnhold());
        assertThat(dokument.getOpplastetDato(), is(nullValue()));
        dokument.setOpplastetDato(null);
        assertThat(dokument.getOpplastetDato(), is(nullValue()));
        DateTime d = new DateTime();
        dokument.setOpplastetDato(d.toDate());
        assertThat(dokument.getDokumentInnhold().getOpplastetDato(), is(equalTo(d)));
        assertThat(dokument.getDokumentInnhold().getOpplastetDato(), is(not(sameInstance(d))));
    }

    @Test
    public void skalTesteInnholdMetoder() throws IOException {
        Dokument dokument = new Dokument(HOVEDSKJEMA);
        assertThat(dokument.harInnhold(), is(false));
        dokument.setDokumentId(1L);
        assertThat(dokument.harInnhold(), is(true));
        InputStream pdf = DokumentTest.class.getResourceAsStream("/testFiles/testpdf.pdf");
        byte[] bytes = IOUtils.toByteArray(pdf);
        dokument.setDokumentInnhold(new DokumentInnhold());
        dokument.getDokumentInnhold().setInnhold(bytes);
        assertThat(dokument.harInnhold(), is(true));
        List<byte[]> liste = new ArrayList<>();
        liste.add(bytes);
        dokument.settOgTransformerInnhold(liste);
        assertThat(dokument.harInnhold(), is(true));
        dokument.slettInnhold();
        dokument.setDokumentId(null);
        assertThat(dokument.harInnhold(), is(false));
    }

    @Test
    public void skalFaaOpplastetDato() {
        Dokument dokument = new Dokument(EKSTRA_VEDLEGG);
        dokument.setDokumentInnhold(new DokumentInnhold());
        assertThat(dokument.getOpplastetDato(), is(nullValue()));
        dokument.setOpplastetDato(null);
        assertThat(dokument.getOpplastetDato(), is(nullValue()));
        DateTime d = new DateTime();
        dokument.setOpplastetDato(d.toDate());
        assertThat(dokument.getOpplastetDato(), is(equalTo(d.toDate())));
    }

    @Test
    public void skalKunneSjekkeOmEtDokumentErOpplastetVedASjekkeDokumentId() {
        Dokument dokument = new Dokument(EKSTRA_VEDLEGG);
        assertThat(dokument.erOpplastet(), is(false));
        dokument.setDokumentId(1L);
        assertThat(dokument.erOpplastet(), is(true));
    }

    @Test
    public void skalKunneSjekkeOmTittelErUnderEllerOver30Tegn() {
        Dokument dokument = new Dokument(EKSTRA_VEDLEGG);
        assertThat(dokument.erTittelOver30Tegn(), is(false));
        dokument.setNavn("kort");
        assertThat(dokument.erTittelOver30Tegn(), is(false));
        dokument.setNavn("1234567890123456789012345678901");
        assertThat(dokument.erTittelOver30Tegn(), is(true));
    }

    @Test
    public void skalKunneSjekkeOmTittelErUnderEllerOver18Tegn() {
        Dokument dokument = new Dokument(EKSTRA_VEDLEGG);
        assertThat(dokument.erTittelOver18Tegn(), is(false));
        dokument.setNavn("kort");
        assertThat(dokument.erTittelOver18Tegn(), is(false));
        dokument.setNavn("1234567890123456789");
        assertThat(dokument.erTittelOver18Tegn(), is(true));
    }
}