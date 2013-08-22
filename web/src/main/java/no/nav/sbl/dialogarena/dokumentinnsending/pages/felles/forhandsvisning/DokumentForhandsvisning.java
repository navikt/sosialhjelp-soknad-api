package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.wicket.model.ModelUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;

import static no.nav.modig.lang.collections.PredicateUtils.greaterThan;
import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;
import static no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverter.SMALL;

public class DokumentForhandsvisning extends GenericPanel<ForhandsvisningModel> {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;
    private Integer side = 1;

    public DokumentForhandsvisning(String id, final IModel<ForhandsvisningModel> dokument) {
        super(id, new CompoundPropertyModel<>(dokument));

        setOutputMarkupId(true);
        add(new Label("side", cmsContentRetriever.hentTekst("side")));
        final Label sideNummer = new Label("sideNummer", new PropertyModel<Integer>(this, "side"));
        sideNummer.setOutputMarkupId(true);

        final LazyForhandsvisning forhandsvisning = new LazyForhandsvisning("forhandsvisning", dokument, sideForRendring(), SMALL);
        forhandsvisning.setOutputMarkupId(true);
        add(sideNummer, forhandsvisning);


        add(new Label("av", cmsContentRetriever.hentTekst("av")));

        WebMarkupContainer sidevelger = new WebMarkupContainer("sidevelger");
        sidevelger.add(visibleIf(ModelUtils.when(new PropertyModel<Integer>(dokument, "antallSider"), greaterThan(1))));

        AjaxLink forrige = new AjaxLink("forrige") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                MarkupContainer parent = DokumentForhandsvisning.this;

                side = (erSideTalletEnEllerLavere() ? getAntallSiderForDokumentet() : reduserSideTalletMedEn());
                parent.addOrReplace(new LazyForhandsvisning("forhandsvisning", dokument, sideForRendring(), SMALL));
                target.add(parent);
            }

            private int reduserSideTalletMedEn() {
                return side - 1;
            }

            private int getAntallSiderForDokumentet() {
                return dokument.getObject().getAntallSider();
            }

            private boolean erSideTalletEnEllerLavere() {
                return side <= 1;
            }
        };

        AjaxLink neste = new AjaxLink("neste") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                MarkupContainer parent = getParent().getParent();

                side = erSideTalletStoerreEnnAntallSiderForDokumentet() ? 1 : oekSideTalletMedEn();
                parent.addOrReplace(new LazyForhandsvisning("forhandsvisning", dokument, sideForRendring(), SMALL));
                target.add(parent);
            }

            private int oekSideTalletMedEn() {
                return side + 1;
            }

            private boolean erSideTalletStoerreEnnAntallSiderForDokumentet() {
                return side >= dokument.getObject().getAntallSider();
            }
        };
        sidevelger.add(forrige, neste);
        add(sidevelger);
        add(new Label("antallSider"));
        LoadableDetachableModel<String> label = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return String.format("%d %s", dokument.getObject().size() / 1000, cmsContentRetriever.hentTekst("kilobyte"));
            }
        };
        add(new Label("filesize", label));
    }

    private int sideForRendring() {
        return side - 1;
    }
}
