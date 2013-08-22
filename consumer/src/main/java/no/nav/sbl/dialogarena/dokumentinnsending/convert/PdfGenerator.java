package no.nav.sbl.dialogarena.dokumentinnsending.convert;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.either;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.harValg;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.LASTET_OPP;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.SENDES_IKKE;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.SEND_SENERE;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.pdfbox.pdmodel.PDPage.PAGE_SIZE_A4;

public class PdfGenerator {
    public static final PDFont FONT_HEADER = PDType1Font.HELVETICA_BOLD;
    public static final PDFont FONT_DOKUMENT = PDType1Font.HELVETICA;
    private static final int FONT_EKSTRA_STOR = 16;
    private static final int FONT_STOR = 14;
    private static final int FONT_VANLIG = 12;

    public byte[] lagKvitteringsSide(DokumentSoknad soknad) {
        String tittel;
        if (soknad.er(BrukerBehandlingType.DOKUMENT_ETTERSENDING)) {
            tittel = String.format("Kvittering 'Ettersending %s'", soknad.getSkjemaNavn());
        } else {
            tittel = String.format("Kvittering '%s'", soknad.getSkjemaNavn());
        }
        try {
            return new PdfBuilder()
                    .startSide()
                    .leggTilNavLogo()
                    .startTekst()
                    .flyttTilTopp()
                    .leggTilHeader(tittel, FONT_EKSTRA_STOR)
                    .flyttNedMed(60)
                    .leggTilDokumenter(on(soknad.getDokumenter()).filter(harValg(LASTET_OPP)).collect(), "Følgende dokumenter er innsendt: ")
                    .leggTilDokumenter(on(soknad.getDokumenter()).filter(either(harValg(SEND_SENERE)).or(harValg(SENDES_IKKE))).collect(), "Følgende dokumenter sendes ikke nå: ")
                    .avsluttTekst()
                    .avsluttSide()
                    .generer();
        } catch (IOException | COSVisitorException e) {
            throw new ApplicationException("Kunne ikke generere kvitteringside", e);
        }
    }

    public byte[] lagForsideEttersending(DokumentSoknad soknad) {
        try {
            return new PdfBuilder()
                    .startSide()
                    .leggTilNavLogo()
                    .startTekst()
                    .flyttTilTopp()
                    .leggTilHeader("Ettersending for:", FONT_EKSTRA_STOR)
                    .flyttNedMed(60)
                    .leggTilHeader(soknad.getSkjemaNavn(), FONT_STOR)
                    .avsluttTekst()
                    .avsluttSide()
                    .generer();
        } catch (IOException | COSVisitorException e) {
            throw new ApplicationException("Kunne ikke generere kvitteringside", e);
        }

    }

    private static final class PdfBuilder {
        private PDDocument pdDocument;


        private PdfBuilder() {
            try {
                this.pdDocument = new PDDocument();
            } catch (IOException e) {
                throw new ApplicationException("Skal ikke skje. ", e);
            }
        }

        public static PdfBuilder start() {
            return new PdfBuilder();
        }

        public PageBuilder startSide() {
            return new PageBuilder();
        }

        public byte[] generer() throws IOException, COSVisitorException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pdDocument.save(baos);
            pdDocument.close();
            return baos.toByteArray();
        }

        private class PageBuilder {
            private PDPage page;
            private PDJpeg logo;
            private PDPageContentStream contentStream;

            public PageBuilder() {
                page = new PDPage(PAGE_SIZE_A4);
                pdDocument.addPage(page);
                try {
                    logo = new PDJpeg(pdDocument, PdfGenerator.class.getResourceAsStream("./navlogo.jpg"));
                    contentStream = new PDPageContentStream(pdDocument, page, true, true);
                } catch (IOException e) {
                    throw new ApplicationException("navlogo.jpg er fjernet fra prosjektet, eller feilet under åpning av contentStream. ", e);
                }
            }

            public PageBuilder leggTilNavLogo() throws IOException {
                contentStream.drawImage(logo, 480, 750);
                return this;
            }

            public TextBuilder startTekst() throws IOException {
                return new TextBuilder();
            }

            public PdfBuilder avsluttSide() throws IOException {
                contentStream.close();
                return PdfBuilder.this;
            }

            private class TextBuilder {
                TextBuilder() throws IOException {
                    contentStream.beginText();
                }

                public TextBuilder leggTilHeader(String tekst, int storrelse) throws IOException {
                    contentStream.setFont(FONT_HEADER, storrelse);
                    contentStream.drawString(tekst);
                    return this;
                }

                public TextBuilder leggTilTekst(String tekst, int storrelse) throws IOException {
                    contentStream.setFont(FONT_DOKUMENT, storrelse);
                    contentStream.drawString(tekst);
                    return this;
                }

                public TextBuilder flyttTilTopp() throws IOException {
                    contentStream.moveTextPositionByAmount(50, 700);
                    return this;
                }

                public TextBuilder flyttNedMed(int piksler) throws IOException {
                    contentStream.moveTextPositionByAmount(0, -1 * piksler);
                    return this;
                }

                private TextBuilder leggTilDokumenter(List<Dokument> dokumenter, String overskrift) throws IOException {
                    TextBuilder textBuilder = this;
                    if (isNotEmpty(dokumenter)) {
                        textBuilder = textBuilder.flyttNedMed(50)
                                .leggTilHeader(overskrift, FONT_STOR);

                        for (Dokument dokument : dokumenter) {
                            textBuilder = textBuilder.flyttNedMed(20)
                                    .leggTilTekst(dokument.getKodeverkId(), FONT_VANLIG)
                                    .flyttHoyre(100)
                                    .leggTilTekst(dokument.getNavn(), FONT_VANLIG)
                                    .flyttVenstre(100);
                        }
                    }
                    return textBuilder;
                }

                public PageBuilder avsluttTekst() throws IOException {
                    contentStream.endText();
                    return PageBuilder.this;
                }

                public TextBuilder flyttHoyre(int piksler) throws IOException {
                    contentStream.moveTextPositionByAmount(piksler, 0);
                    return this;
                }

                public TextBuilder flyttVenstre(int piksler) throws IOException {
                    contentStream.moveTextPositionByAmount(-1 * piksler, 0);
                    return this;
                }
            }
        }
    }
}