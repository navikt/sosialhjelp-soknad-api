package no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import javax.inject.Inject;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static org.apache.commons.lang3.StringUtils.join;

public class InnsendteDokumenterBekreftelse extends GenericPanel<List<Dokument>> {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public InnsendteDokumenterBekreftelse(String id, IModel<List<Dokument>> model) {
        super(id, model);
        IModel<String> mottatteDokumenter = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                List<Dokument> innsendteDokumenter = InnsendteDokumenterBekreftelse.this.getModelObject();
                List<String> dokumentNavn = on(innsendteDokumenter).map(Dokument.NAVN).collect();
                String og = String.format(" %s", cmsContentRetriever.hentTekst("og"));
                String mottatteDokumenter = join(dokumentNavn, ", ");

                int lastComma = mottatteDokumenter.lastIndexOf(',');

                if (lastComma > 0) {
                    String substring = mottatteDokumenter.substring(lastComma);
                    mottatteDokumenter = mottatteDokumenter.replace(substring, substring.replace(",", og));
                }

                mottatteDokumenter += " " + cmsContentRetriever.hentTekst("kvittering.innsendt.mottatt");
                return mottatteDokumenter;
            }
        };
        add(new Label("mottatteDokumenter", mottatteDokumenter));
    }

    private static String getApplicationProperty(String key) {
        return new StringResourceModel(key, null).getString();
    }

}
