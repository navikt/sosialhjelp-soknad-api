package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.FakeMigrasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.Migrasjon;

import java.util.*;

public class MigrasjonHandterer{
    List<Migrasjon> migrasjoner = new ArrayList<>();

    public MigrasjonHandterer(){
        migrasjoner = migrasjoner();
    }

    public WebSoknad handterMigrasjon(WebSoknad soknad){
        WebSoknad migrertSoknad = soknad;

        if(migrasjoner == null || migrasjoner.size() <= 0) return soknad;

        Optional<Migrasjon> migrasjon = hentMigrasjonForSkjemanummerOgVersjon(migrertSoknad.getVersjon(), migrertSoknad.getskjemaNummer());

        if(migrasjon.isPresent()){
            migrertSoknad = migrasjon.get().migrer(migrertSoknad.getVersjon(), migrertSoknad);

            Event metrikk = MetricsFactory.createEvent("sendsoknad.skjemamigrasjon");
            String soknadTypePrefix;
            try {
                KravdialogInformasjon kravdialogInformasjon = new KravdialogInformasjonHolder()
                        .hentKonfigurasjon(migrertSoknad.getskjemaNummer());
                soknadTypePrefix = kravdialogInformasjon.getSoknadTypePrefix();
            }
            catch (Exception e) {
                soknadTypePrefix = "";
            }
            metrikk.addTagToReport("soknadstype", soknadTypePrefix);
            metrikk.addTagToReport("skjemaversjon", String.valueOf(migrasjon.get().getTilVersjon()));

            metrikk.report();
        }

        return migrertSoknad;
    }

    public static List<Migrasjon> migrasjoner() {
        List<Migrasjon> migrasjonsListe = new ArrayList<>();

        migrasjonsListe.add(new FakeMigrasjon());

        return migrasjonsListe;
    }

    private Optional<Migrasjon> hentMigrasjonForSkjemanummerOgVersjon(int versjon, String skjemanummer) {
        return migrasjoner.stream()
                .filter(migrasjon -> migrasjon.getMigrasjonSkjemanummer().equalsIgnoreCase(skjemanummer))
                .filter(migrasjon -> migrasjon.skalMigrere(versjon, skjemanummer))
                .findFirst();
    }
}