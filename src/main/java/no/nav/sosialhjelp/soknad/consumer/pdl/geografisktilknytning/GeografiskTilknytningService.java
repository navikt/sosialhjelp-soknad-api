//package no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning;
//
//import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.dto.GeografiskTilknytningDto;
//import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.dto.GtType;
//import org.slf4j.Logger;
//
//import static org.slf4j.LoggerFactory.getLogger;
//
//public class GeografiskTilknytningService {
//
//    private static final Logger log = getLogger(GeografiskTilknytningService.class);
//
//    private final GeografiskTilknytningConsumer geografiskTilknytningConsumer;
//
//    public GeografiskTilknytningService(GeografiskTilknytningConsumer geografiskTilknytningConsumer) {
//        this.geografiskTilknytningConsumer = geografiskTilknytningConsumer;
//    }
//
//    public String hentGeografiskTilknytning(String ident) {
//        var geografiskTilknytningDto = geografiskTilknytningConsumer.hentGeografiskTilknytning(ident);
//        return bydelsnummerEllerKommunenummer(geografiskTilknytningDto);
//    }
//
//    private String bydelsnummerEllerKommunenummer(GeografiskTilknytningDto dto) {
//        if (dto != null && GtType.BYDEL.equals(dto.getGtType())) {
//            return dto.getGtBydel();
//        }
//        if (dto != null && GtType.KOMMUNE.equals(dto.getGtType())) {
//            return dto.getGtKommune();
//        }
//        log.warn("GeografiskTilknytningDto er ikke av type Bydel eller Kommune -> returnerer null");
//        return null;
//    }
//}
