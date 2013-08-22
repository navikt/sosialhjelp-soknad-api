package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import no.nav.sbl.dialogarena.pdf.ImageToPdf;
import no.nav.sbl.dialogarena.pdf.PdfMerger;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.pdf.PdfFunctions.PDF_SIDEANTALL;

/**
 * Denne klassen representere innholdet til et dokument, og settes i det en bruker laster opp en fil.
 */
public class DokumentInnhold implements Serializable {
    private String navn;
    private byte[] innhold;
    private long id;
    private DateTime opplastetDato;
    private transient Integer sider;

    public DateTime getOpplastetDato() {
        return opplastetDato;
    }

    public void setOpplastetDato(DateTime opplastetDato) {
        this.opplastetDato = opplastetDato;
    }

    public long getId() {
        return id;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getNavn() {
        return navn;
    }

    public void setInnhold(byte[] innhold) {
        this.innhold = (innhold != null ? innhold.clone() : null);
    }

    public void setOpplastetDato(Date opplastetDato) {
        if (opplastetDato == null) {
            this.opplastetDato = null;
        } else {
            this.opplastetDato = new DateTime(opplastetDato);
        }
    }

    public byte[] hentInnholdSomBytes() {
        return innhold != null ? innhold.clone() : null;
    }

    public void settOgTransformerInnhold(Iterable<byte[]> dokument) {
        Iterable<byte[]> enkeltPdfer = on(dokument).map(new ImageToPdf());
        this.innhold = new PdfMerger().transform(enkeltPdfer);
    }

    public int antallSider() {
        if (sider == null) {
            this.sider = PDF_SIDEANTALL.transform(innhold);
        }
        return sider;
    }

    public void setId(long id) {
        this.id = id;
    }
}