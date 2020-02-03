package no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;

@Component
public class KommuneInfoService {
    private static final Logger log = LoggerFactory.getLogger(KommuneInfoService.class);

    @Inject
    private DigisosApi digisosApi;

    public boolean kanMottaSoknader(String kommunenummer) {
        boolean kanMottaSoknader = digisosApi.hentKommuneInfo()
                .getOrDefault(kommunenummer, new KommuneInfo())
                .getKanMottaSoknader();
        if (ServiceUtils.isRunningInProd() && kanMottaSoknader) {
            log.info("Kommune {} har aktivert Digisos i Fiks konfigurasjonen. (Dette er ingen feil, men kanskje litt overraskende?)", kommunenummer);
        }
        return kanMottaSoknader;
    }

    public boolean harMidlertidigDeaktivertMottak(String kommunenummer) {
        return digisosApi.hentKommuneInfo()
                .getOrDefault(kommunenummer, new KommuneInfo())
                .getHarMidlertidigDeaktivertMottak();
    }

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    public KommuneStatus kommuneInfo(String kommunenummer) {
        KommuneInfo kommuneInfo = digisosApi.hentKommuneInfo().getOrDefault(kommunenummer, null);

        if (kommuneInfo == null) {
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
