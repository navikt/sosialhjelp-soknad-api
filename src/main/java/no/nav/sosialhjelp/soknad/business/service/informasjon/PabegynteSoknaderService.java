//package no.nav.sosialhjelp.soknad.business.service.informasjon;
//
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
//import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon.dto.PabegyntSoknad;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class PabegynteSoknaderService {
//
//    public SoknadMetadataRepository soknadMetadataRepository;
//
//    public PabegynteSoknaderService(SoknadMetadataRepository soknadMetadataRepository) {
//        this.soknadMetadataRepository = soknadMetadataRepository;
//    }
//
//    public List<PabegyntSoknad> hentPabegynteSoknaderForBruker(String fnr) {
//        List<SoknadMetadata> soknader = soknadMetadataRepository.hentPabegynteSoknaderForBruker(fnr);
//
//        return soknader.stream()
//                .map(soknadMetadata -> new PabegyntSoknad(soknadMetadata.sistEndretDato, soknadMetadata.behandlingsId))
//                .collect(Collectors.toList());
//    }
//}
