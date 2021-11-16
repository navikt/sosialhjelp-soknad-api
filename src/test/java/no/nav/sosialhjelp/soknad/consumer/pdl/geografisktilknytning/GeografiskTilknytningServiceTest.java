//package no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning;
//
//
//import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.dto.GeografiskTilknytningDto;
//import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.dto.GtType;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class GeografiskTilknytningServiceTest {
//
//    @Mock
//    private GeografiskTilknytningConsumer geografiskTilknytningConsumer;
//
//    @InjectMocks
//    private GeografiskTilknytningService geografiskTilknytningService;
//
//    private final String ident = "ident";
//    private final String gt = "gt";
//
//    @Test
//    void skalReturnereBydelsnummer() {
//        when(geografiskTilknytningConsumer.hentGeografiskTilknytning(ident))
//                .thenReturn(new GeografiskTilknytningDto(GtType.BYDEL, null, gt, null));
//
//        var geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident);
//        assertThat(geografiskTilknytning).isEqualTo(gt);
//    }
//
//    @Test
//    void skalReturnereKommunenummer() {
//        when(geografiskTilknytningConsumer.hentGeografiskTilknytning(ident))
//                .thenReturn(new GeografiskTilknytningDto(GtType.KOMMUNE, gt, null, null));
//
//        var geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident);
//        assertThat(geografiskTilknytning).isEqualTo(gt);
//    }
//
//    @Test
//    void skalReturnereNullHvisUtland() {
//        when(geografiskTilknytningConsumer.hentGeografiskTilknytning(ident))
//                .thenReturn(new GeografiskTilknytningDto(GtType.UTLAND, null, null, gt));
//
//        var geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident);
//        assertThat(geografiskTilknytning).isNull();
//    }
//
//    @Test
//    void skalReturnereNullHvisUdefinert() {
//        when(geografiskTilknytningConsumer.hentGeografiskTilknytning(ident))
//                .thenReturn(new GeografiskTilknytningDto(GtType.UDEFINERT, null, null, null));
//
//        var geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident);
//        assertThat(geografiskTilknytning).isNull();
//    }
//
//    @Test
//    void skalReturnereNullHvisConsumerGirNull() {
//        when(geografiskTilknytningConsumer.hentGeografiskTilknytning(ident))
//                .thenReturn(null);
//
//        var geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident);
//        assertThat(geografiskTilknytning).isNull();
//    }
//
//}