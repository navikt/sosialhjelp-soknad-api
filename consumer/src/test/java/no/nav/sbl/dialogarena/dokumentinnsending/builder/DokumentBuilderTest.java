package no.nav.sbl.dialogarena.dokumentinnsending.builder;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DokumentBuilderTest {

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedlegg() {
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).build();

        assertTrue(dokument != null);
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggMedBrukerBehandlingsId() {
        String id = "1";
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .brukerBehandlingId(id)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getBehandlingsId(), is(id));
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggMedDokumentForventningsId() {
        Long id = 1L;
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .dokumentForventningId(id)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getDokumentForventningsId(), is(id));
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggMedInformasjonFraDokument() {
        DateTime expectedDate = new DateTime();
        WSDokument dokument = new WSDokument();
        dokument.setOpplastetDato(expectedDate);
        Dokument vedlegg = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .opplastetDatoFra(dokument)
                .build();

        assertTrue(vedlegg != null);
        assertThat(vedlegg.getOpplastetDato().getTime(), is(expectedDate.getMillis()));
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggUtenInformasjonFraDokumentDersomGittDokumentErNull() {
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .opplastetDatoFra(null)
                .build();

        assertTrue(dokument != null);
        assertTrue(dokument.getOpplastetDato() == null);
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggUtenOpplastetDatoDeromGittDokumentIkkeHarSattDato() {
        WSDokument dokument = new WSDokument();
        Dokument vedlegg = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .opplastetDatoFra(dokument)
                .build();

        assertTrue(vedlegg != null);
        assertTrue(vedlegg.getOpplastetDato() == null);
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggMedNavn() {
        String tittel = "tittel";
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .tittel(tittel)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getNavn(), is(tittel));
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggMedBeskrivelse() {
        String beskrivelse = "beskrivelse";
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .beskrivelse(beskrivelse)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getBeskrivelse(), is(beskrivelse));
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggMedKodeverksId() {
        String kodeverksId = "id";
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .kodeverkId(kodeverksId)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getKodeverkId(), is(kodeverksId));
    }

    @Test
    public void eksterntVedleggBuilderReturnererEksterntVedleggMedInnsendingsValg() {
        InnsendingsValg valg = InnsendingsValg.IKKE_VALGT;
        Dokument dokument = VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                .innsendingsValg(valg)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getValg(), is(valg));
    }
}