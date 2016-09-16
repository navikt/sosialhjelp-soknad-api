package no.nav.sbl.dialogarena.service.helpers;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public class SoknadTestHelper{

    public static WebSoknad soknadMedInnsendtVedlegg(){
        return lagSoknadMedVedleggStatus(Vedlegg.Status.LastetOpp);
    }

    public static WebSoknad soknadMedIkkeInnsendtVedlegg(){
        return lagSoknadMedVedleggStatus(Vedlegg.Status.VedleggKreves);
    }

    private static WebSoknad lagSoknadMedVedleggStatus(Vedlegg.Status status) {
        WebSoknad webSoknad = new WebSoknad();

        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setInnsendingsvalg(status);
        webSoknad.medVedlegg(vedlegg);

        return webSoknad;
    }

}
