package no.nav.sbl.dialogarena.soknadinnsending;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Klasse for å returnere et resultat av en opplasting
 */
@XmlRootElement
public class VedleggOpplasting {
    private List<VedleggOpplastingResultat> files;

    public List<VedleggOpplastingResultat> getFiles() {
        return files;
    }

    public void setFiles(List<VedleggOpplastingResultat> files) {
        this.files = files;
    }
}
