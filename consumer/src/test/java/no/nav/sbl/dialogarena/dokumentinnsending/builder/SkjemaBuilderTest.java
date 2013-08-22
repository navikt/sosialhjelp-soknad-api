package no.nav.sbl.dialogarena.dokumentinnsending.builder;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Skjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import org.joda.time.DateTime;
import org.junit.Test;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.HOVEDSKJEMA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SkjemaBuilderTest {

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjema() {
        assertTrue(SkjemaBuilder.forType(HOVEDSKJEMA).build().er(HOVEDSKJEMA));
        assertTrue(SkjemaBuilder.forType(Type.NAV_VEDLEGG).build().er(Type.NAV_VEDLEGG));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedBrukerBehandlingsId() {
        String id = "1";
        Dokument dokument = SkjemaBuilder.forType(HOVEDSKJEMA)
                .brukerBehandlingId(id)
                .build();

        assertThat(dokument.getBehandlingsId(), is(id));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedDokumentForventningsId() {
        Long id = 1L;
        Dokument dokument = SkjemaBuilder.forType(HOVEDSKJEMA)
                .dokumentForventningId(id)
                .build();

        assertThat(dokument.getDokumentForventningsId(), is(id));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedInformasjonFraDokument() throws Exception {
        DateTime expectedDate = new DateTime();
        WSDokument dokument = new WSDokument();
        dokument.setOpplastetDato(expectedDate);
        Dokument vedlegg = SkjemaBuilder.forType(HOVEDSKJEMA).opplastetDatoFra(dokument).build();

        assertThat(vedlegg.getOpplastetDato().getTime(), is(expectedDate.getMillis()));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaUtenInformasjonFraDokumentDersomGittDokumentErNull() {
        assertThat(SkjemaBuilder.forType(HOVEDSKJEMA).opplastetDatoFra(null).build().getOpplastetDato(), nullValue());
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaUtenOpplastetDatoDeromGittDokumentIkkeHarSattDato() {
        WSDokument dokument = new WSDokument();
        Dokument vedlegg = SkjemaBuilder.forType(HOVEDSKJEMA)
                .opplastetDatoFra(dokument)
                .build();

        assertTrue(vedlegg != null);
        assertTrue(vedlegg.getOpplastetDato() == null);
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedTittel() {
        String tittel = "tittel";
        Dokument dokument = SkjemaBuilder.forType(HOVEDSKJEMA)
                .tittel(tittel)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getNavn(), is(tittel));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedBeskrivelse() {
        String beskrivelse = "beskrivelse";
        Dokument dokument = SkjemaBuilder.forType(HOVEDSKJEMA)
                .beskrivelse(beskrivelse)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getBeskrivelse(), is(beskrivelse));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedKodeverksId() {
        String kodeverksId = "id";
        Dokument dokument = SkjemaBuilder.forType(HOVEDSKJEMA)
                .kodeverkId(kodeverksId)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getKodeverkId(), is(kodeverksId));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedInnsendingsValg() {
        InnsendingsValg valg = InnsendingsValg.IKKE_VALGT;
        Dokument dokument = SkjemaBuilder.forType(HOVEDSKJEMA)
                .innsendingsValg(valg)
                .build();

        assertTrue(dokument != null);
        assertThat(dokument.getValg(), is(valg));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedVedleggsId() {
        String skjemaId = "id";
        Skjema vedlegg = SkjemaBuilder.forType(HOVEDSKJEMA)
                .skjemaId(skjemaId)
                .build();

        assertTrue(vedlegg != null);
        assertThat(vedlegg.getSkjemaId(), is(skjemaId));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedLink() {
        String link = "link";
        Skjema vedlegg = SkjemaBuilder.forType(HOVEDSKJEMA)
                .link(link)
                .build();

        assertTrue(vedlegg != null);
        assertThat(vedlegg.getLink(), is(link));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedSkjemanummer() {
        String skjemanummer = "nummer";
        Skjema vedlegg = SkjemaBuilder.forType(HOVEDSKJEMA)
                .skjemanummer(skjemanummer)
                .build();

        assertTrue(vedlegg != null);
        assertThat(vedlegg.getSkjemanummer(), is(skjemanummer));
    }

    @Test
    public void hovedHovedSkjemaBuilderReturnererSkjemaMedGosysId() {
        String gosysId = "id";
        Skjema vedlegg = SkjemaBuilder.forType(HOVEDSKJEMA)
                .gosysId(gosysId)
                .build();

        assertTrue(vedlegg != null);
        assertThat(vedlegg.getGosysId(), is(gosysId));
    }
}