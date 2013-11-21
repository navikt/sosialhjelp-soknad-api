package no.nav.sbl.dialogarena.soknadinnsending;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse for å returnere et resultat av en opplasting
 */
@XmlRootElement
public class VedleggOpplasting {
    private List<VedleggOpplastingResultat> files;

    public VedleggOpplasting(List<Vedlegg> vedlegg) {
        files = new ArrayList<>();
        for (Vedlegg vedlegget : vedlegg) {
            files.add(new VedleggOpplastingResultat(vedlegget));
        }
    }

    public List<VedleggOpplastingResultat> getFiles() {
        return files;
    }

    public void setFiles(List<VedleggOpplastingResultat> files) {
        this.files = files;
    }
}
