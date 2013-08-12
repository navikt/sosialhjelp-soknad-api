package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BaseViewModel;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.radiogruppe.RadiogruppeViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SoknadViewModel extends BaseViewModel {

    private static final Logger logger = LoggerFactory.getLogger(SoknadViewModel.class);

    private static final List<String> STATSBORGERSKAP_VALG = Arrays.asList("Norsk", "Flyktning", "Utenlandsk");
    private static final String FORNAVN_KEY = "fornavn";
    private static final String ETTERNAVN_KEY = "etternavn";
    private static final String FNR_KEY = "fnr";
    private static final String ADRESSE_KEY = "adresse";
    private static final String POSTNR_KEY = "postnr";
    private static final String POSTSTED_KEY = "poststed";
    private static final String TELEFON_KEY = "telefon";
    private static final String BOKOMMUNE_KEY = "bokommune";
    private static final String NASJONALITET_KEY = "nasjonalitet";
    private static final String PENGER_KEY = "penger";
    private static final String SUM_KEY = "sum";
    private static final String STATSBORGER_KEY = "statsborger";

    public SoknadViewModel(String tabTittel, Soknad soknad) {
        super(tabTittel, soknad);
    }

    public final FaktumViewModel getFornavn() {
        return getFakumViewModel(FORNAVN_KEY);
    }

    public final FaktumViewModel getEtternavn() {
        return getFakumViewModel(ETTERNAVN_KEY);
    }

    public final FaktumViewModel getFnr() {
        return getFakumViewModel(FNR_KEY);
    }

    public final FaktumViewModel getAdresse() {
        return getFakumViewModel(ADRESSE_KEY);
    }

    public final FaktumViewModel getPostnr() {
        return getFakumViewModel(POSTNR_KEY);
    }

    public final FaktumViewModel getPoststed() {
        return getFakumViewModel(POSTSTED_KEY);
    }

    public final FaktumViewModel getTelefon() {
        return getFakumViewModel(TELEFON_KEY);
    }

    public final FaktumViewModel getBokommune() {
        return getFakumViewModel(BOKOMMUNE_KEY);
    }

    public final FaktumViewModel getNasjonalitet() {
        return getFakumViewModel(NASJONALITET_KEY);
    }

    public final FaktumViewModel getPenger() {
        return getFakumViewModel(PENGER_KEY);
    }

    public final FaktumViewModel getSum() {
        return getFakumViewModel(SUM_KEY);
    }

    public final RadiogruppeViewModel getStatsborger() {
        return getRadiogruppeViewModel(STATSBORGER_KEY, STATSBORGERSKAP_VALG);
    }

    private FaktumViewModel getFakumViewModel(String key) {
        Faktum faktum = getFaktum(key);
        String label = getApplicationProperty("tullesoknad." + key);
        return new FaktumViewModel(faktum, label);
    }

    private RadiogruppeViewModel getRadiogruppeViewModel(String key, List<String> valgliste) {
        Faktum faktum = getFaktum(key);
        return new RadiogruppeViewModel(faktum, valgliste);
    }

    private Faktum getFaktum(String key) {
        if (getSoknad().getFakta().containsKey(key)) {
            return getSoknad().getFakta().get(key);
        }
        logger.error("Fant ikke nøkkel {} i søknadsstrukturen", key);
        throw new ApplicationException("Fant ikke nøkkelen i søknadsstrukturen");
    }
}