package no.nav.sosialhjelp.soknad.consumer.fiks;

import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilOpplasting;

import java.util.List;

public interface DigisosApi {
    void ping();

    String krypterOgLastOppFiler(String soknadJson, String tilleggsinformasjonJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token);
}
