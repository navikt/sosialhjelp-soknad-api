package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter;


import no.nav.sbl.dialogarena.detect.IsImage;
import no.nav.sbl.dialogarena.detect.IsPdf;
import org.apache.commons.collections15.Predicate;

public class IsValidFileTypeForUpload implements Predicate<byte[]> {
    @Override
    public boolean evaluate(byte[] bytes) {
        return new IsImage().evaluate(bytes) || new IsPdf().evaluate(bytes);
    }
}
