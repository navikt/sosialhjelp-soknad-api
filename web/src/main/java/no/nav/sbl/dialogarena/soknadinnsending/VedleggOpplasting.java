package no.nav.sbl.dialogarena.soknadinnsending;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Klasse for å returnere et resultat av en opplasting
 */
@XmlRootElement
public class VedleggOpplasting {
    private List<Vedlegg> files;

    public VedleggOpplasting(List<Vedlegg> vedlegg) {
        files = vedlegg;
    }

    public List<Vedlegg> getFiles() {
        return files;
    }

    public void setFiles(List<Vedlegg> files) {
        this.files = files;
    }
}
