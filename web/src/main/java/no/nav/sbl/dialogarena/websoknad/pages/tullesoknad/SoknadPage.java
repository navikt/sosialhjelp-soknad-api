package no.nav.sbl.dialogarena.websoknad.pages.tullesoknad;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.felles.input.inputkomponenter.Checkboks;
import no.nav.sbl.dialogarena.websoknad.pages.felles.input.inputkomponenter.TekstFelt;
import no.nav.sbl.dialogarena.websoknad.pages.felles.input.radiogruppe.Radiogruppe;
import no.nav.sbl.dialogarena.websoknad.pages.kvittering.KvitteringPage;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import javax.inject.Inject;

import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;

public class SoknadPage extends BasePage {

    @Inject
    private WebSoknadService soknadService;

    private static final String UTENLANDSK = "Utenlandsk";

    public SoknadPage(final WebSoknad soknad) {
        super();

        CompoundPropertyModel<SoknadViewModel> pageModel = new CompoundPropertyModel<>(new LoadableDetachableModel<SoknadViewModel>() {
            @Override
            protected SoknadViewModel load() {
                return new SoknadViewModel("Søknad", soknad);
            }
        });
        setDefaultModel(pageModel);

        Form form = new Form("form") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                Long soknadId = ((SoknadViewModel) getPage().getDefaultModelObject()).getSoknadId();
                soknadService.sendSoknad(soknadId);
                setResponsePage(new KvitteringPage(soknad));
            }
        };
        add(form);

        form.add(new TekstFelt("telefon"));
        form.add(new TekstFelt("bokommune"));

        final TekstFelt nasjonalitet = new TekstFelt("nasjonalitet");
        nasjonalitet.setOutputMarkupPlaceholderTag(true);
        Radiogruppe statsborger = new Radiogruppe("statsborger") {
            @Override
            public void onSelectionChanged(AjaxRequestTarget target) {
                target.add(nasjonalitet);
            }
        };
        nasjonalitet.add(visibleIf(statsborger.isSelected(UTENLANDSK)));

        form.add(nasjonalitet, statsborger);

        final TekstFelt sum = new TekstFelt("sum");
        sum.setOutputMarkupPlaceholderTag(true);

        Checkboks penger = new Checkboks("penger") {
            @Override
            public void onToggle(AjaxRequestTarget target) {
                target.add(sum);
            }
        };
        sum.add(visibleIf(penger.isChecked()));

        form.add(sum, penger);

        Link avbryt = new Link("avbryt") {
            @Override
            public void onClick() {
                Long soknadId = ((SoknadViewModel) getPage().getDefaultModelObject()).getSoknadId();
                soknadService.avbrytSoknad(soknadId);
            }
        };

        avbryt.setBody(Model.of("Avbryt"));

        form.add(avbryt);
    }
}
