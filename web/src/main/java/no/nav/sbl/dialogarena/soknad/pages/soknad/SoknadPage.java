package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter.Checkboks;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter.TekstFelt;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.radiogruppe.Radiogruppe;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.inject.Inject;

import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;

public class SoknadPage extends BasePage {

    @Inject
    private SoknadService soknadService;

    private static final String UTENLANDSK = "Utenlandsk";

    public SoknadPage(final Soknad soknad) {
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
            }
        };
        add(form);

        form.add(new TekstFelt("fornavn"));
        form.add(new TekstFelt("etternavn"));

        form.add(new TekstFelt("fnr"));

        form.add(new TekstFelt("adresse"));
        form.add(new TekstFelt("postnr"));
        form.add(new TekstFelt("poststed"));

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
    }
}
