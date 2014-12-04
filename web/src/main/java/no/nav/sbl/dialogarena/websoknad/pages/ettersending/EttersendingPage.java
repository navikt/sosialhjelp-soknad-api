package no.nav.sbl.dialogarena.websoknad.pages.ettersending;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class EttersendingPage extends EttersendingBasePage {

    public EttersendingPage(PageParameters parameters) {
        super(parameters);

//        WebSoknad soknad = ettersendingService.hentEttersendingForBehandlingskjedeId(brukerbehandlingId.toString());
//        if (soknad != null) {
//            new CookieUtils().remove("XSRF-TOKEN");
//            new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknad.getBehandlingskjedeId()));
//        } else {
//            setResponsePage(StartEttersendingPage.class, parameters);
//        }
    }
}
