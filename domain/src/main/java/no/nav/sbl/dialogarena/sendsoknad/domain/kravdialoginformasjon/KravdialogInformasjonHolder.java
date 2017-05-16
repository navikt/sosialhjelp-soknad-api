package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.modig.core.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Component
public class KravdialogInformasjonHolder {


    //TODO: Generelt for alle søknadsdialoger gjelder det at SoknadPrefix brukes både for å hente navnet på hbs-fil, og for å finne prefixet på CMSnøkler. Dette bør
    //være to ulike parametere, da cms-tekster ikke nødvendigvis trenger å være knyttet til en søknadstype, noe det legges opp til i dag.
    // Endringer er gjort i AAPGjenopptakInformasjon og AAPOrdinaerInformasjon som følge av dette.


    private List<KravdialogInformasjon> soknadsKonfigurasjoner = new ArrayList<>();

    public KravdialogInformasjonHolder() {
        soknadsKonfigurasjoner.addAll(asList(
                new AAPOrdinaerInformasjon(),
                new AAPGjenopptakInformasjon(),
                new DagpengerGjenopptakInformasjon(),
                new DagpengerOrdinaerInformasjon(),
                new ForeldrepengerInformasjon(),
                new BilstonadInformasjon(),
                new SoknadTilleggsstonader(),
                new TiltakspengerInformasjon(),
                new SoknadRefusjonDagligreise()
                //new ForeldrepengerOverforingInformasjon()
        ));
    }

    public KravdialogInformasjon hentKonfigurasjon(String skjemanummer) {
        for (KravdialogInformasjon soknadKonfigurasjon : soknadsKonfigurasjoner) {
            if (soknadKonfigurasjon.getSkjemanummer().contains(skjemanummer)) {
                return soknadKonfigurasjon;
            }
        }
        throw new ApplicationException("Fant ikke config for skjemanummer: " + skjemanummer);
    }

    public List<String> hentAlleSkjemanumre() {
        List<String> skjemanumre = new ArrayList<>();
        for (KravdialogInformasjon soknadKonfigurasjon : soknadsKonfigurasjoner) {
            skjemanumre.addAll(soknadKonfigurasjon.getSkjemanummer());
        }
        return skjemanumre;
    }

    public List<KravdialogInformasjon> getSoknadsKonfigurasjoner() {
        return soknadsKonfigurasjoner;
    }
}
