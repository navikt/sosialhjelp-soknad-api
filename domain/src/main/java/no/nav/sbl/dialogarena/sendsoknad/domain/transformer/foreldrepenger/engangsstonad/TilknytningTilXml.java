package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Landkoder;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Periode;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.TilknytningNorge;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.TilknytningNorge.FremtidigOppholdUtenlands;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.TilknytningNorge.TidligereOppholdUtenlands;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Utenlandsopphold;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class TilknytningTilXml implements Function<WebSoknad, TilknytningNorge> {
    @Override
    public TilknytningNorge apply(WebSoknad webSoknad) {

        TilknytningNorge tilknytningNorge = new TilknytningNorge();

        Boolean ikkeTidligereUtenlandsopphold = Boolean.valueOf(webSoknad.getValueForFaktum("tilknytningnorge.tidligere"));
        tilknytningNorge.setTidligereOppholdNorge(ikkeTidligereUtenlandsopphold);

        if (!ikkeTidligereUtenlandsopphold) {
            tilknytningNorge.withTidligereOppholdUtenlands(tidligereOppholdUtenlands(webSoknad));
        }

        Boolean ikkeFremtidigUtenlandsOpphold = Boolean.valueOf(webSoknad.getValueForFaktum("tilknytningnorge.fremtidig"));
        tilknytningNorge.setFremtidigOppholdNorge(ikkeFremtidigUtenlandsOpphold);

        if(!ikkeFremtidigUtenlandsOpphold) {
            tilknytningNorge.withFremtidigOppholdUtenlands(fremtidigOppholdUtenlands(webSoknad));
        }

        return tilknytningNorge
                .withOppholdNorgeNaa(Boolean.valueOf(webSoknad.getValueForFaktum("tilknytningnorge.oppholder")));
    }

    private TidligereOppholdUtenlands tidligereOppholdUtenlands(WebSoknad webSoknad) {
        return new TidligereOppholdUtenlands()
                .withUtenlandsoppholds(utenlandsOpphold(webSoknad, "tilknytningnorge.tidligere.periode"));
    }

    private FremtidigOppholdUtenlands fremtidigOppholdUtenlands(WebSoknad webSoknad) {
        return new FremtidigOppholdUtenlands()
                .withUtenlandsoppholds(utenlandsOpphold(webSoknad, "tilknytningnorge.fremtidig.periode"));
    }


    private Collection<Utenlandsopphold> utenlandsOpphold(WebSoknad webSoknad, String faktumNokkel) {
        return webSoknad.getFaktaMedKey(faktumNokkel).stream()
                .map(TO_UTENLANDSOPPHOLD)
                .collect(toList());
    }

    private Function<Faktum, Utenlandsopphold> TO_UTENLANDSOPPHOLD = faktum -> {
        Map<String, String> properties = faktum.getProperties();
        return new Utenlandsopphold()
                .withLand(new Landkoder().withKode(properties.get("land")))
                .withPeriode(new Periode()
                        .withFom(LocalDate.parse(properties.get("fradato")))
                        .withTom(LocalDate.parse(properties.get("tildato")))
                );
    };
}
