package no.nav.sbl.dialogarena.dokumentinnsending.convert;

import no.nav.sbl.dialogarena.detect.IsPdf;
import no.nav.sbl.dialogarena.dokumentinnsending.builder.SkjemaBuilder;
import no.nav.sbl.dialogarena.dokumentinnsending.builder.SoknadBuilder;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import org.hamcrest.number.OrderingComparison;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PdfGeneratorTest {

    @Test
    public void skalReturnereDokument() throws IOException {
        PdfGenerator generator = new PdfGenerator();
        DokumentSoknad soknad = lagSoknad(BrukerBehandlingType.DOKUMENT_BEHANDLING);
        byte[] bytes = generator.lagKvitteringsSide(soknad);
        assertThat(bytes.length, OrderingComparison.greaterThan(0));
        assertTrue(new IsPdf().evaluate(bytes));
        Files.write(FileSystems.getDefault().getPath("test.pdf"), bytes);
    }

    @Test
    public void skalReturnereDokumentForEttersending() throws IOException {
        PdfGenerator generator = new PdfGenerator();
        DokumentSoknad soknad = lagSoknad(BrukerBehandlingType.DOKUMENT_ETTERSENDING);
        byte[] bytes = generator.lagKvitteringsSide(soknad);
        assertThat(bytes.length, OrderingComparison.greaterThan(0));
        assertTrue(new IsPdf().evaluate(bytes));
        Files.write(FileSystems.getDefault().getPath("test.pdf"), bytes);
    }

    @Test
    public void skalReturnereForside() throws IOException {
        PdfGenerator generator = new PdfGenerator();
        DokumentSoknad soknad = lagSoknad(BrukerBehandlingType.DOKUMENT_BEHANDLING);
        byte[] bytes = generator.lagForsideEttersending(soknad);
        assertThat(bytes.length, OrderingComparison.greaterThan(0));
        assertTrue(new IsPdf().evaluate(bytes));
        Files.write(FileSystems.getDefault().getPath("test.pdf"), bytes);
    }


    private DokumentSoknad lagSoknad(BrukerBehandlingType type) {
        return SoknadBuilder.with()
                .ident("itent")
                .soknadsId("soknad1")
                .type(type)
                .skjema(
                        SkjemaBuilder
                                .forType(Dokument.Type.HOVEDSKJEMA)
                                .tittel("Hovedskjema")
                                .kodeverkId("100kkkk")
                                .skjemaId("HT1")
                                .gosysId("gosys1")
                                .skjemanummer("nr1")
                                .innsendingsValg(InnsendingsValg.LASTET_OPP))
                .navVedlegg(
                        SkjemaBuilder
                                .forType(Dokument.Type.NAV_VEDLEGG)
                                .tittel("Nav vedlegg sendes senere")
                                .kodeverkId("kodeverk_1")
                                .skjemaId("sk1")
                                .gosysId("gs2")
                                .link("http://link.com")
                                .beskrivelse("beskrivelse")
                                .innsendingsValg(InnsendingsValg.SENDES_IKKE))
                .navVedlegg(
                        SkjemaBuilder
                                .forType(Dokument.Type.NAV_VEDLEGG)
                                .tittel("Nav vedlegg Lastet opp")
                                .kodeverkId("kodeverk_2")
                                .skjemaId("sk1")
                                .gosysId("gs2")
                                .link("http://link.com")
                                .beskrivelse("beskrivelse")
                                .innsendingsValg(InnsendingsValg.LASTET_OPP)).build();
    }
}
