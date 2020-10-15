package no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks;

import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.FilOpplasting;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;

import java.util.List;
import java.util.Map;

public interface DigisosApi {
    void ping();

    Map<String, KommuneInfo> hentKommuneInfo();

    String krypterOgLastOppFiler(String soknadJson, String tilleggsinformasjonJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token);
}
