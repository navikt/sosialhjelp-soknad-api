package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static java.util.Collections.singletonList;

public class BosituasjonSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var bosituasjon = jsonInternalSoknad.getSoknad().getData().getBosituasjon();

        return new Steg.Builder()
                .withStegNr(5)
                .withTittel("bosituasjonbolk.tittel")
                .withAvsnitt(
                        singletonList(
                                new Avsnitt.Builder()
                                        .withTittel("bosituasjonbolk.tittel") // skal være "din bosituasjon"
                                        .withSporsmal(bosituasjonSporsmal(bosituasjon))
                                        .build()
                        )
                )
                .build();
    }

    private List<Sporsmal> bosituasjonSporsmal(JsonBosituasjon bosituasjon) {
        var harUtfyltHvorBorDu = bosituasjon != null && bosituasjon.getBotype() != null;
        var harUtfyltHvorMangeBorSammen = bosituasjon != null && bosituasjon.getAntallPersoner() != null;

        var hvordanBorDuSporsmal = new Sporsmal.Builder()
                .withTittel("bosituasjon.sporsmal") // hvordan bor du
                .withErUtfylt(harUtfyltHvorBorDu)
                .withFelt(harUtfyltHvorBorDu ?
                        singletonList(
                                new Felt.Builder()
                                        .withType(Type.CHECKBOX)
                                        .withSvar(botypeToTekstKey(bosituasjon.getBotype()))
                                        .build()
                        ) :
                        null
                )
                .build();

        var hvorMangeBorSammenSporsmal = new Sporsmal.Builder()
                .withTittel("bosituasjon.sporsmal") // hvordan bor du
                .withErUtfylt(harUtfyltHvorMangeBorSammen)
                .withFelt(harUtfyltHvorMangeBorSammen ?
                        singletonList(
                                new Felt.Builder()
                                        .withType(Type.TEKST)
                                        .withSvar(bosituasjon.getAntallPersoner().toString())
                                        .build()
                        ) :
                        null
                )
                .build();

        return List.of(
                hvordanBorDuSporsmal,
                hvorMangeBorSammenSporsmal
        );
    }

    private String botypeToTekstKey(JsonBosituasjon.Botype botype) {
        String key;
        switch (botype) {
            case EIER: key = "bosituasjon.eier"; break;
            case LEIER: key =  "bosituasjon.leier"; break;
            case KOMMUNAL: key =  "bosituasjon.kommunal"; break;
            case INGEN: key =  "bosituasjon.ingen"; break;
            case FORELDRE: key =  "bosituasjon.annet.botype.foreldre"; break;
            case FAMILIE: key =  "bosituasjon.annet.botype.familie"; break;
            case VENNER: key =  "bosituasjon.annet.botype.venner"; break;
            case INSTITUSJON: key =  "bosituasjon.annet.botype.institusjon"; break;
            case FENGSEL: key =  "bosituasjon.annet.botype.fengsel"; break;
            case KRISESENTER: key =  "bosituasjon.annet.botype.krisesenter"; break;
            case ANNET: default:
                key =  "bosituasjon.annet"; break;
        }
        return key;
    }
}
