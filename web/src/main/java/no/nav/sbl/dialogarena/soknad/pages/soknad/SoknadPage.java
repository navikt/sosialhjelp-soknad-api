package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.convert.xml.XmlSoknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.Checkboks;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.TekstFelt;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

public class SoknadPage extends BasePage {

    public SoknadPage(final XmlSoknad soknad) {
        super();

        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<SoknadViewModel>() {
            @Override
            protected SoknadViewModel load() {
                return new SoknadViewModel("Søknad", soknad);
            }
        }));

        IModel soknadModel = Model.of(soknad);


        add(new TekstFelt("navn", Model.of("Navn"), Model.of(""), soknadModel));
        add(new TekstFelt("adresse", Model.of("Adresse"), Model.of(""), soknadModel));
        add(new TekstFelt("telefon", Model.of("Telefon"), Model.of(""), soknadModel));
        add(new TekstFelt("epost", Model.of("E-Post"), Model.of(""), soknadModel));
        add(new TekstFelt("inntekt", Model.of("Inntekt"), Model.of(""), soknadModel));
        add(new TekstFelt("arbeidsgiver", Model.of("Arbeidsgiver"), Model.of(""), soknadModel));
        add(new Checkboks("penger", Model.of("Vil du ha penger?"), soknadModel));
    }
}
