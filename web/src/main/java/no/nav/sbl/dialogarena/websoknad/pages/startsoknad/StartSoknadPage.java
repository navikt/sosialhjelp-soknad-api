package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage.MainBasePage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.SendSoknadServicePage;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StartSoknadPage extends MainBasePage {

    @Inject
    private WebSoknadService soknadService;

    Logger log = LoggerFactory.getLogger(SendSoknadServicePage.class);
    String soknadType;

    public StartSoknadPage(PageParameters parameters) {
        super(parameters);
        soknadType = parameters.get("soknadType").toString();
//        Long soknadId = soknadService.startSoknad(soknadType);

        add(new WebComponent("include") {
            @Override
            public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                InputStream content = null;
                if(soknadType.equalsIgnoreCase("dagpenger")){
                    content = StartSoknadPage.class.getResourceAsStream("/html/Dagpenger.html");
                } else if (soknadType.equalsIgnoreCase("barnebidrag")){
                    content = StartSoknadPage.class.getResourceAsStream("/html/Barnebidrag.html");
                }
                try {
                    replaceComponentTagBody(markupStream, openTag, StreamUtils.copyToString(content, Charset.forName("UTF-8")));
                } catch (IOException e) {
                    throw new ApplicationException("feilet under lasting av markup", e);
                }
            }
        });
    
    }

}
