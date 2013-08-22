package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;


import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.security.SecurityHandler;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import org.apache.wicket.markup.html.WebMarkupContainer;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

public class InformasjonOmUtenlandsAdresse extends ObjectPerRowFixture<InformasjonOmUtenlandsAdresse.AdresseInformasjon> {

    private final FluentWicketTester<WicketApplication> wicketTester;
    private final DokumentServiceMock dokumentService;

    static class AdresseInformasjon {

        @NoCompare
        public String ident;

        @NoCompare
        public boolean bosattINorge;

        public boolean felteneVises;
        @NoCompare

        public String valgtAlternativ;

        public boolean sendeInfoTilHenvendelse;

        @NoCompare
        public String kommentar;

        @Override
        public String toString() {
            return "Ident: " + ident + ", bosatt i Norge: " + bosattINorge + ", valgt alternativ: " + valgtAlternativ + ", feltene vises: " + felteneVises + ", valgtAlternativ: " + valgtAlternativ;
        }
    }


    public InformasjonOmUtenlandsAdresse(FluentWicketTester<WicketApplication> wicketTester, DokumentServiceMock dokumentService) {
        this.dokumentService = dokumentService;
        this.wicketTester = wicketTester;

    }


    @Override
    protected void perRow(Row<AdresseInformasjon> rad) throws Exception {
        //Set up user
        SecurityHandler.setSecurityContext(rad.expected.ident);
        wicketTester.goTo(BekreftelsesPage.class);

        //RadioGroup radioGroup = wicketTester.get().component(withId("adresseGruppe"));
        WebMarkupContainer utenlandskAdresse = wicketTester.get().component(withId("utenlandskAdresse")); 

        boolean erAdresseValgSynlig = utenlandskAdresse.isVisibleInHierarchy();

        wicketTester.get().component(withId("samtykket")).setDefaultModelObject(true);

        wicketTester.printComponentsTree();
        wicketTester.inForm("bekreftelsesForm").check("samtykket", true)
        	.select("utenlandskAdresse:adresseGruppe",  getIndex(rad.expected.valgtAlternativ))
        	.submit();
//        	.should().beOn(InnsendingKvitteringPage.class);

//        wicketTester.tester.assertRenderedPage(InnsendingKvitteringPage.class);

        final AdresseInformasjon resultat = lagResultat(erAdresseValgSynlig);

        rad.isActually(resultat);

        dokumentService.toemInnsendtListe();
    }

    private AdresseInformasjon lagResultat(boolean erAdresseValgSynlig) {
        final AdresseInformasjon resultat = new AdresseInformasjon();
        resultat.felteneVises = erAdresseValgSynlig;
        resultat.sendeInfoTilHenvendelse = dokumentService.skalSendesTilNavInternasjonalEnhet("", "");
        return resultat;
    }


	private int getIndex(String valgtAlternativ) {
		switch (valgtAlternativ) {
		case "valg1":
			return 0;
		case "valg2":
			return 1;
		case "valg3":
			return 2;

		default:
			return 0;
		}
	}


}
