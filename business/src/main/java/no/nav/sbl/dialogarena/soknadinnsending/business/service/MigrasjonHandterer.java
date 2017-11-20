package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.FakeMigrasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.Migrasjon;

import java.util.*;

public class MigrasjonHandterer{
    Map<String,List<Migrasjon>> migreringsMap = new HashMap<>();

    public MigrasjonHandterer(){
        migreringsMap.put("NAV XO.XO-XO", Arrays.asList(new FakeMigrasjon()));
    }

    public WebSoknad handterMigrasjon(WebSoknad soknad){
        List<Migrasjon> migrasjoner = migreringsMap.get(soknad.getskjemaNummer());
        WebSoknad migrertSoknad = soknad;

        if(migrasjoner == null) return soknad;

        for(Migrasjon migrasjon : migrasjoner) {
            //Versjon som er hardkodet, må byttes ut med en soknad.getSkjemaVersjon for eksempel når det er på plass
            if (migrasjon.skalMigrere(migrertSoknad.getskjemaNummer(),1)){
                migrertSoknad = migrasjon.migreer(migrertSoknad,1);

                Event metrikk = MetricsFactory.createEvent("sendsoknad.skjemamigrasjon");
                metrikk.addTagToReport("skjemanummer",migrertSoknad.getskjemaNummer());
                metrikk.report();
            }
        }
        return migrertSoknad;
    }
}