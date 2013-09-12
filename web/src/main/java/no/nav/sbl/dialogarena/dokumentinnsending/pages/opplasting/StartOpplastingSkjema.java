package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.Spraak;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter.UploadFormPanel;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class StartOpplastingSkjema extends StartOpplasting {
    public StartOpplastingSkjema(String id, final IModel<Dokument> dokument) {
        super(id);
        IModel<String> link = new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return dokument.getObject().getKodeverk().getUrl(Spraak.NB);
            }
        };

        OpplastingSteg steg1 = new OpplastingSteg("steg1", 1, "opplasting.skjema.steg1");
        ExternalLink externalLink = new ExternalLink(steg1.getButtonId(), link,  new ResourceModel(("opplasting.skjema.steg1.knapp")));
       
        steg1.setButton(externalLink, "knapp-liten");
        add(steg1);

        OpplastingSteg steg2 = new OpplastingSteg("steg2", 2, "opplasting.skjema.steg2");
        UploadFormPanel form = getUploadForm(steg2.getButtonId(), "knapp-liten");
        steg2.setButton(form, "startOpplastingContainer");
        add(steg2);
    }
}
