package no.nav.sbl.dialogarena.dokumentinnsending.builder;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import org.junit.Test;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTERNT_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.NAV_VEDLEGG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public class SoknadBuilderTest {

    @Test
    public void soknadBuilderReturnererSoknad() {
        DokumentSoknad soknad = SoknadBuilder.with().build();

        assertTrue(soknad != null);
    }

    @Test
    public void skalReturnereSoknadMedIdent() {
        String ident = "1";
        DokumentSoknad soknad = SoknadBuilder.with().ident(ident).build();

        assertThat(soknad.ident, is(ident));
    }

    @Test
    public void soknadBuilderReturnererSoknadMedSoknadsId() {
        String soknadsId = "id";
        DokumentSoknad soknad = SoknadBuilder.with()
                .soknadsId(soknadsId)
                .build();

        assertTrue(soknad != null);
        assertThat(soknad.soknadsId, is(soknadsId));
    }

    @Test
    public void soknadBuilderReturnererSoknadMedHovedSkjema() {
        DokumentSoknad soknad = SoknadBuilder.with()
                .skjema(SkjemaBuilder.forType(Type.HOVEDSKJEMA))
                .build();

        assertTrue(soknad != null);
        assertTrue(soknad.hovedskjema != null);
    }

    @Test
    public void soknadBuilderReturnererSoknadMedEksterntVedlegg() {
        DokumentSoknad soknad = SoknadBuilder.with()
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG))
                .build();

        assertTrue(soknad != null);
        assertTrue(soknad.finnVedleggAvType(EKSTERNT_VEDLEGG) != null);
        assertThat(soknad.finnVedleggAvType(EKSTERNT_VEDLEGG).size(), is(1));
    }

    @Test
    public void soknadBuilderReturnererSoknadMedEksterneVedlegg() {
        DokumentSoknad soknad = SoknadBuilder.with()
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG))
                .build();

        assertTrue(soknad != null);
        assertTrue(soknad.finnVedleggAvType(EKSTERNT_VEDLEGG) != null);
        assertThat(soknad.finnVedleggAvType(EKSTERNT_VEDLEGG).size(), is(2));
    }

    @Test
    public void soknadBuilderReturnererSoknadMedMedEksterneVedleggGittViaListe() {
        DokumentSoknad soknad = SoknadBuilder.with()
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG))
                .build();

        assertTrue(soknad != null);
        assertTrue(soknad.finnVedleggAvType(EKSTERNT_VEDLEGG) != null);
        assertThat(soknad.finnVedleggAvType(EKSTERNT_VEDLEGG).size(), is(4));
    }

    @Test
    public void soknadBuilderReturnererSoknadMedMedNAVVedleggGittViaListe() {
        DokumentSoknad soknad = SoknadBuilder.with()
                .navVedlegg(SkjemaBuilder.forType(Type.NAV_VEDLEGG))
                .navVedlegg(SkjemaBuilder.forType(Type.NAV_VEDLEGG))
                .navVedlegg(SkjemaBuilder.forType(Type.NAV_VEDLEGG))
                .navVedlegg(SkjemaBuilder.forType(Type.NAV_VEDLEGG))
                .build();

        assertTrue(soknad != null);
        assertTrue(soknad.finnVedleggAvType(NAV_VEDLEGG) != null);
        assertThat(soknad.finnVedleggAvType(NAV_VEDLEGG).size(), is(4));
    }
}
