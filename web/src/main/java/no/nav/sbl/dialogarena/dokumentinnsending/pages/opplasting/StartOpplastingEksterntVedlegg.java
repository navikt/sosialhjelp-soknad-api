package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting;

import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter.UploadFormPanel;

public class StartOpplastingEksterntVedlegg extends StartOpplasting {

    public StartOpplastingEksterntVedlegg(String id) {
        super(id);

        add(new OpplastingSteg("steg1", 1, "opplasting.vedlegg.steg1"));

        OpplastingSteg steg2 = new OpplastingSteg("steg2", 2, "opplasting.vedlegg.steg2");
        UploadFormPanel form = getUploadForm(steg2.getButtonId(), "knapp-liten");
        steg2.setButton(form, "startOpplastingContainer");
        add(steg2);
    }
}
