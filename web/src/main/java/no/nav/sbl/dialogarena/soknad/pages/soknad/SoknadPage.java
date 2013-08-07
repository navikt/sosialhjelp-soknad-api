package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.Radiogruppe;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter.Checkboks;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter.TekstFelt;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;
import java.util.List;

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

        form.add(new TekstFelt("fornavn", pageModel));
        form.add(new TekstFelt("etternavn", pageModel));

        form.add(new TekstFelt("fnr", pageModel));

        form.add(new TekstFelt("adresse", pageModel));
        form.add(new TekstFelt("postnr", pageModel));
        form.add(new TekstFelt("poststed", pageModel));

        form.add(new TekstFelt("telefon", pageModel));
        form.add(new TekstFelt("bokommune", pageModel));

        final TekstFelt nasjonalitet = new TekstFelt("nasjonalitet", pageModel);
        nasjonalitet.setOutputMarkupPlaceholderTag(true);

        Radiogruppe statsborger = new Radiogruppe("statsborger", pageModel, new PropertyModel<List<FaktumViewModel>>(pageModel, "statsborgerListe")) {
            @Override
            public void onSelectionChanged(AjaxRequestTarget target) {
                target.add(nasjonalitet);
            }
        };
        nasjonalitet.add(visibleIf(statsborger.isSelected(UTENLANDSK)));

        form.add(statsborger, nasjonalitet);

        final TekstFelt sum = new TekstFelt("sum", pageModel);
        sum.setOutputMarkupPlaceholderTag(true);

        Checkboks penger = new Checkboks("penger", pageModel) {
            @Override
            public void onToggle(AjaxRequestTarget target) {
                target.add(sum);
            }
        };
        sum.add(visibleIf(penger.isChecked()));

        form.add(sum, penger);
    }
}
