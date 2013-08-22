package no.nav.sbl.dialogarena.dokumentinnsending.domain;


import org.junit.Test;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType.DOKUMENT_BEHANDLING;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType.DOKUMENT_ETTERSENDING;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SoknadTest {

    @Test
    public void skalFinneVedleggAvType() {
        DokumentSoknad soknad = new DokumentSoknad("ident", "id");
        soknad.brukerBehandlingType = DOKUMENT_BEHANDLING;
        Skjema hovedskjema = new Skjema(Dokument.Type.HOVEDSKJEMA, "id");
        hovedskjema.setNavn("navn");
        soknad.hovedskjema = hovedskjema;
        soknad.leggTilVedlegg(new Dokument(Dokument.Type.NAV_VEDLEGG));
        soknad.leggTilVedlegg(new Dokument(Dokument.Type.EKSTERNT_VEDLEGG));
        soknad.leggTilVedlegg(new Dokument(Dokument.Type.EKSTERNT_VEDLEGG));
        assertThat(soknad.finnVedleggAvType(Dokument.Type.NAV_VEDLEGG).size(), is(1));
        assertThat(soknad.finnVedleggAvType(Dokument.Type.EKSTERNT_VEDLEGG).size(), is(2));
        assertThat(soknad.finnVedleggAvType(Dokument.Type.IKKE_SPESIFISERT).size(), is(0));
        assertThat(soknad.getDokumenter().size(), is(4));
    }

    @Test
    public void skalTesteTittel() {
        DokumentSoknad soknad = new DokumentSoknad("ident", "id");
        Skjema hovedskjema = new Skjema(Dokument.Type.HOVEDSKJEMA, "id");
        hovedskjema.setNavn("navn");
        soknad.hovedskjema = hovedskjema;
        assertThat(soknad.getSkjemaNavn(), is(hovedskjema.getNavn()));
        DokumentSoknad tomSoknad = new DokumentSoknad("ident", "id");
        assertThat(tomSoknad.getSkjemaNavn(), is("Ukjent skjema"));
    }

    @Test
    public void skalTesteEr() {
        DokumentSoknad soknad = new DokumentSoknad("ident", "id");
        soknad.brukerBehandlingType = DOKUMENT_BEHANDLING;
        assertThat(soknad.er(DOKUMENT_BEHANDLING), is(true));
        assertThat(soknad.er(DOKUMENT_ETTERSENDING), is(false));
    }
}