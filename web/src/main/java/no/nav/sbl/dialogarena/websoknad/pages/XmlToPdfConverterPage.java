package no.nav.sbl.dialogarena.websoknad.pages;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.print.HtmlGenerator;
import no.nav.sbl.dialogarena.print.PDFFabrikk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * En side som tar inn en xml struktur og konverterer til en pdf.
 */
public class XmlToPdfConverterPage extends BasePage {

    @Inject
    private VedleggService vedleggService;
    @Inject
    private HtmlGenerator pdfTemplate;
    public XmlToPdfConverterPage(PageParameters parameters) {
        super(parameters);
        final FileUploadField fil = new FileUploadField("fileUpload");
        Form form = new Form("fileUploadForm") {
            @Override
            protected void onSubmit() {
                FileUpload opplastet = fil.getFileUpload();
                WebSoknad soknad = JAXB.unmarshal(new ByteArrayInputStream(opplastet.getBytes()), WebSoknad.class);
                final String oppsummeringMarkup;
                try {
                    vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());
                    oppsummeringMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger");
                } catch (IOException e) {
                    throw new ApplicationException("Kunne ikke lage markup av søknad", e);
                }
                getRequestCycle().scheduleRequestHandlerAfterCurrent(
                        new ResourceStreamRequestHandler(
                                new AbstractResourceStreamWriter() {
                                    @Override
                                    public void write(OutputStream output) throws IOException {
                                        output.write( new PDFFabrikk().lagPdfFil(oppsummeringMarkup));
                                    }
                                }, opplastet.getClientFileName() + ".pdf"));
            }
        };
        form.setMultiPart(true);
        add(form);
        form.add(fil);
    }
}
