package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import java.util.List;
import java.util.Map;

public interface DigisosApi {
    void ping();

    KommuneStatus kommuneInfo(String kommunenr, Map<String, KommuneInfo> stringKommuneInfoMap1);

    // @Cacheable("kommuneinfoCache")
    // todo: får ikke cache til å virke, legger inn manuelt enn så lenge
    Map<String, KommuneInfo> hentKommuneInfo();

    String krypterOgLastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token);
}
