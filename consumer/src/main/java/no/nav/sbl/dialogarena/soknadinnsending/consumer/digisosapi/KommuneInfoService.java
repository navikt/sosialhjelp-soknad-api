package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.KommuneStatus.*;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;

@Component
public class KommuneInfoService {

    @Inject
    private DigisosApi digisosApi;

    public boolean kanMottaSoknader(String kommunenummer) {
        return digisosApi.hentKommuneInfo()
                .getOrDefault(kommunenummer, new KommuneInfo())
                .getKanMottaSoknader();
    }

    public boolean harMidlertidigDeaktivertMottak(String kommunenummer) {
        return digisosApi.hentKommuneInfo()
                .getOrDefault(kommunenummer, new KommuneInfo())
                .getHarMidlertidigDeaktivertMottak();
    }

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    public KommuneStatus kommuneInfo(String kommunenummer) {
        KommuneInfo kommuneInfo = digisosApi.hentKommuneInfo().getOrDefault(kommunenummer, new KommuneInfo());

        if (kommuneInfo.getKanMottaSoknader() == null) {
            return MANGLER_KONFIGURASJON;
        }
        if (!kommuneInfo.getKanMottaSoknader()) {
            return HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT;
        }
        if (kommuneInfo.getHarMidlertidigDeaktivertMottak()) {
            return SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER;
        }

        return SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;
    }

}
