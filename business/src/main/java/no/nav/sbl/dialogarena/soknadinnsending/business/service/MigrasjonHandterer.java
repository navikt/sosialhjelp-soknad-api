package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
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

        if(migrasjoner == null) return soknad;

        for(Migrasjon migrasjon : migrasjoner) {
            //Versjon som er hardkodet, må byttes ut med en soknad.getSkjemaVersjon for eksempel når det er på plass
            if (migrasjon.skalMigrere(1, migrertSoknad.getskjemaNummer())){
                migrertSoknad = migrasjon.migrer(migrertSoknad,1);

                Event metrikk = MetricsFactory.createEvent("sendsoknad.skjemamigrasjon");
                String soknadTypePrefix = new KravdialogInformasjonHolder().hentKonfigurasjon(migrertSoknad.getskjemaNummer()).getSoknadTypePrefix();
                metrikk.addTagToReport("soknadstype", soknadTypePrefix);
                metrikk.report();
                return migrertSoknad;
            }
        }
        return migrertSoknad;
    }

    public static List<Migrasjon> migrasjoner() {
        List<Migrasjon> migrasjonsListe = new ArrayList<>();

        migrasjonsListe.add(new FakeMigrasjon());

        return migrasjonsListe;
    }
}