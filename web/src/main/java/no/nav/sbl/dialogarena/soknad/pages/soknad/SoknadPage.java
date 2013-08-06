package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.Radiogruppe;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter.TekstFelt;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import java.util.Arrays;
import java.util.List;

import static no.nav.modig.lang.collections.PredicateUtils.equalToIgnoreCase;
import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;
import static no.nav.modig.wicket.model.ModelUtils.when;

public class SoknadPage extends BasePage {

    private static final String UTENLANDSK = "Utenlandsk";

    private IModel<String> valgtStatsborgerskap = Model.of("");

    public SoknadPage(final Soknad soknad) {
        super();

        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<SoknadViewModel>() {
            @Override
            protected SoknadViewModel load() {
                return new SoknadViewModel("Søknad", soknad);
            }
        }));

        IModel soknadModel = Model.of(soknad);


        add(new TekstFelt("fornavn", Model.of("Fornavn"), Model.of(""), soknadModel));
        add(new TekstFelt("etternavn", Model.of("Etternavn"), Model.of(""), soknadModel));

        add(new TekstFelt("fnr", Model.of("Fødselsnummer"), Model.of(""), soknadModel));

        add(new TekstFelt("adresse", Model.of("Bolidadresse"), Model.of(""), soknadModel));
        add(new TekstFelt("postnr", Model.of("Postnummer"), Model.of(""), soknadModel));
        add(new TekstFelt("poststed", Model.of("Poststed"), Model.of(""), soknadModel));

        add(new TekstFelt("telefon", Model.of("Telefonnummer"), Model.of(""), soknadModel));
        add(new TekstFelt("bokommune", Model.of("Bokommune"), Model.of(""), soknadModel));

        final TekstFelt nasjonalitet = new TekstFelt("nasjonalitet", Model.of("Nasjonalitet"), Model.of(""), soknadModel);
        nasjonalitet.setOutputMarkupId(true);
        nasjonalitet.add(visibleIf(when(valgtStatsborgerskap, equalToIgnoreCase(UTENLANDSK))));
        add(nasjonalitet);

        final List<String> statsborgerskapValg = Arrays.asList(new String[] {"Norsk", "Flyktning", "Utenlandsk"});

        Radiogruppe statsborger = new Radiogruppe("statsborger", statsborgerskapValg, soknadModel) {
            @Override
            public void onSelectionChanged(AjaxRequestTarget target) {
                String value = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("value").toString();
                valgtStatsborgerskap.setObject(value);
                target.add(nasjonalitet);
            }
        };

        add(statsborger);

    }
}
