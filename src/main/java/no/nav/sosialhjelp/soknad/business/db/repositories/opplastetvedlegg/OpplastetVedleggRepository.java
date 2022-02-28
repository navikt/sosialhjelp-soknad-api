//package no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg;
//
//import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface OpplastetVedleggRepository {
//
//    Optional<OpplastetVedlegg> hentVedlegg(String uuid, String eier);
//    List<OpplastetVedlegg> hentVedleggForSoknad(Long soknadId, String eier);
//    String opprettVedlegg(OpplastetVedlegg opplastetVedlegg, String eier);
//    void slettVedlegg(String uuid, String eier);
//    void slettAlleVedleggForSoknad(Long soknadId, String eier);
//    Integer hentSamletVedleggStorrelse(Long soknadId, String eier);
//}
