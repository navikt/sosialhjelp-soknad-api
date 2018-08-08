package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

@Component
public class MigrasjonHandterer{

    @Inject
    private HendelseRepository hendelseRepository;

    List<Migrasjon> migrasjoner = migrasjoner();

    public WebSoknad handterMigrasjon(WebSoknad soknad){
        WebSoknad migrertSoknad = soknad;

        if(migrasjoner == null || migrasjoner.size() <= 0) return soknad;

        Optional<Migrasjon> migrasjon = hentMigrasjonForVersjon(migrertSoknad.getVersjon());

        if(migrasjon.isPresent()){
            migrertSoknad = migrasjon.get().migrer(migrertSoknad.getVersjon(), migrertSoknad);

            hendelseRepository.registrerMigrertHendelse(migrertSoknad);

            Event metrikk = MetricsFactory.createEvent("soknadsosialhjelp.skjemamigrasjon");
            metrikk.addTagToReport("skjemaversjon", String.valueOf(migrasjon.get().getTilVersjon()));

            metrikk.report();
        }

        return migrertSoknad;
    }

    public static List<Migrasjon> migrasjoner() {
        List<Migrasjon> migrasjonsListe = new ArrayList<>();
        return migrasjonsListe;
    }

    private Optional<Migrasjon> hentMigrasjonForVersjon(Integer versjon) {
        return migrasjoner.stream()
                .filter(migrasjon -> migrasjon.skalMigrere(versjon))
                .findFirst();
    }
}