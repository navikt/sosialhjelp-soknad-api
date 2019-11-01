package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import java.util.List;

public interface DigisosApi {
    void ping();

    KommuneStatus kommuneInfo(String kommunenr);

    String krypterOgLastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token);
}
