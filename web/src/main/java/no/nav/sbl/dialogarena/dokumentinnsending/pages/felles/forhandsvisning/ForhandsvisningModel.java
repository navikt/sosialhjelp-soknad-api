package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning;

import no.nav.sbl.dialogarena.detect.IsPdf;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import org.apache.commons.collections15.Transformer;

import java.io.Serializable;

import static no.nav.sbl.dialogarena.pdf.PdfFunctions.PDF_SIDEANTALL;

/**
 * Model for forhåndsvisning
 */
public class ForhandsvisningModel implements Serializable {
    public String id;
    public String filename;
    public byte[] bilde;
    private int antallSider;

    public int getAntallSider() {
        if (antallSider == 0) {
            byte[] bildeIBytes = bilde;
            antallSider = new IsPdf().evaluate(bildeIBytes) ? PDF_SIDEANTALL.transform(bildeIBytes) : 1;
        }
        return antallSider;
    }

    public int size() {
        return bilde.length;
    }

    public static final Transformer<DokumentInnhold, ForhandsvisningModel> DOKUMENT_TIL_FORHANDSVISNING_MODEL = new Transformer<DokumentInnhold, ForhandsvisningModel>() {
        @Override
        public ForhandsvisningModel transform(DokumentInnhold dokumentInnhold) {
            ForhandsvisningModel iModel = new ForhandsvisningModel();
            if (dokumentInnhold != null) {
                iModel.id = "" + dokumentInnhold.getId();
                iModel.bilde = dokumentInnhold.hentInnholdSomBytes();
                iModel.filename = dokumentInnhold.getNavn();
            }
            return iModel;
        }
    };
}