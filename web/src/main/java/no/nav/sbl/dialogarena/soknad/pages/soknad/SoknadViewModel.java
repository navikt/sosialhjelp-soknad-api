package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BaseViewModel;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoknadViewModel extends BaseViewModel {

    private static final List<String> STATSBORGERSKAP_VALG = Arrays.asList(new String[]{"Norsk", "Flyktning", "Utenlandsk"});

    private static final String FORNAVN_KEY = "fornavn";
    private static final String FORNAVN_LABEL = "Fornavn";

    private static final String ETTERNAVN_KEY = "etternavn";
    private static final String ETTERNAVN_LABEL = "Etternavn";

    private static final String FNR_KEY = "fnr";
    private static final String FNR_LABEL = "Fødselsnummer";

    private static final String ADRESSE_KEY = "adresse";
    private static final String ADRESSE_LABEL = "Adresse";

    private static final String POSTNR_KEY = "postnr";
    private static final String POSTNR_LABEL = "Postnummer";

    private static final String POSTSTED_KEY = "poststed";
    private static final String POSTSTED_LABEL = "Poststed";

    private static final String TELEFON_KEY = "telefon";
    private static final String TELEFON_LABEL = "Telefon";

    private static final String BOKOMMUNE_KEY = "bokommune";
    private static final String BOKOMMUNE_LABEL = "Bokommune";

    private static final String NASJONALITET_KEY = "nasjonalitet";
    private static final String NASJONALITET_LABEL = "Nasjonalitet";

    private static final String PENGER_KEY = "penger";
    private static final String PENGER_LABEL = "Vil du ha penger?";

    private static final String SUM_KEY = "sum";
    private static final String SUM_LABEL = "Hvor mye penger?";

    private static final String STATSBORGER_KEY = "statsborger";

    public SoknadViewModel(String tabTittel, Soknad soknad) {
        super(tabTittel, soknad);
    }

    public FaktumViewModel getFornavn() {
        return getFakumViewModel(FORNAVN_KEY, FORNAVN_LABEL);
    }

    public FaktumViewModel getEtternavn() {
        return getFakumViewModel(ETTERNAVN_KEY, ETTERNAVN_LABEL);
    }

    public FaktumViewModel getFnr() {
        return getFakumViewModel(FNR_KEY, FNR_LABEL);
    }

    public FaktumViewModel getAdresse() {
        return getFakumViewModel(ADRESSE_KEY, ADRESSE_LABEL);
    }

    public FaktumViewModel getPostnr() {
        return getFakumViewModel(POSTNR_KEY, POSTNR_LABEL);
    }

    public FaktumViewModel getPoststed() {
        return getFakumViewModel(POSTSTED_KEY, POSTSTED_LABEL);
    }

    public FaktumViewModel getTelefon() {
        return getFakumViewModel(TELEFON_KEY, TELEFON_LABEL);
    }

    public FaktumViewModel getBokommune() {
        return getFakumViewModel(BOKOMMUNE_KEY, BOKOMMUNE_LABEL);
    }

    public FaktumViewModel getNasjonalitet() {
        return getFakumViewModel(NASJONALITET_KEY, NASJONALITET_LABEL);
    }

    public FaktumViewModel getPenger() {
        return getFakumViewModel(PENGER_KEY, PENGER_LABEL);
    }

    public FaktumViewModel getSum() {
        return getFakumViewModel(SUM_KEY, SUM_LABEL);
    }

    public FaktumViewModel getStatsborger() {
        return getFakumViewModel(STATSBORGER_KEY, "");
    }

    public List<FaktumViewModel> getStatsborgerListe() {
        List<FaktumViewModel> valgListe = new ArrayList<>();

        for (String valg : STATSBORGERSKAP_VALG) {
            Faktum faktum = new Faktum();
            FaktumViewModel fvm = new FaktumViewModel(faktum, valg);
            valgListe.add(fvm);
        }
        return valgListe;
    }

    private FaktumViewModel getFakumViewModel(String key, String label) {
        Faktum faktum = getFaktum(key);
        return new FaktumViewModel(faktum, label);
    }

    private Faktum getFaktum(String key) {
        if (soknad.fakta.containsKey(key)) {
            return soknad.fakta.get(key);
        }
        Faktum faktum = new Faktum();
        faktum.setKey(key);
        faktum.setSoknadId(soknad.soknadId);
        return faktum;
    }
}