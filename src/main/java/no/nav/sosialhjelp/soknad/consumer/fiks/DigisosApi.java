package no.nav.sosialhjelp.soknad.consumer.fiks;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.soknad.domain.model.digisosapi.FilOpplasting;

import java.util.List;
import java.util.Map;

public interface DigisosApi {
    void ping();

    Map<String, KommuneInfo> hentAlleKommuneInfo();

    String krypterOgLastOppFiler(String soknadJson, String tilleggsinformasjonJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token);
}
