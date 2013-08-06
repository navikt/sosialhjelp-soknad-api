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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.inject.Inject;
import java.util.List;

import static no.nav.modig.lang.collections.PredicateUtils.equalToIgnoreCase;
import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;
import static no.nav.modig.wicket.model.ModelUtils.when;

public class SoknadPage extends BasePage {

    @Inject
    private SoknadService soknadService;

    private static final String UTENLANDSK = "Utenlandsk";

    private IModel<String> valgtStatsborgerskap = Model.of("");
    private Boolean vilHaPenger = false;

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


        form.add(new TekstFelt("fornavn", new PropertyModel(pageModel, "fornavn")));
        form.add(new TekstFelt("etternavn", new PropertyModel(pageModel, "etternavn")));

        form.add(new TekstFelt("fnr", new PropertyModel(pageModel, "fnr")));

        form.add(new TekstFelt("adresse", new PropertyModel(pageModel, "adresse")));
        form.add(new TekstFelt("postnr", new PropertyModel(pageModel, "postnr")));
        form.add(new TekstFelt("poststed", new PropertyModel(pageModel, "poststed")));

        form.add(new TekstFelt("telefon", new PropertyModel(pageModel, "telefon")));
        form.add(new TekstFelt("bokommune", new PropertyModel(pageModel, "bokommune")));

        final TekstFelt nasjonalitet = new TekstFelt("nasjonalitet", new PropertyModel(pageModel, "nasjonalitet"));
        nasjonalitet.setOutputMarkupPlaceholderTag(true);
        nasjonalitet.add(visibleIf(when(valgtStatsborgerskap, equalToIgnoreCase(UTENLANDSK))));
        form.add(nasjonalitet);

        Radiogruppe statsborger = new Radiogruppe("statsborger", new PropertyModel<FaktumViewModel>(pageModel, "statsborger"), new PropertyModel<List<FaktumViewModel>>(pageModel, "statsborgerListe")) {
            @Override
            public void onSelectionChanged(AjaxRequestTarget target) {
                String value = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("value").toString();
                valgtStatsborgerskap.setObject(value);
                target.add(nasjonalitet);
            }
        };

        form.add(statsborger);

        final TekstFelt sum = new TekstFelt("sum", new PropertyModel(pageModel, "sum"));
        sum.setOutputMarkupPlaceholderTag(true);
        sum.add(visibleIf(new PropertyModel<Boolean>(this, "vilHaPenger")));


        Checkboks penger = new Checkboks("penger", new PropertyModel(pageModel, "penger")) {
            @Override
            public void onToggle(AjaxRequestTarget target) {
                vilHaPenger = !vilHaPenger;
                target.add(sum);
            }
        };
        form.add(sum, penger);
    }
}
