//package no.nav.sosialhjelp.soknad.business.service.dittnav;
//
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
//import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
//import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
//import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class DittNavMetadataServiceTest {
//
//    @Mock
//    private SoknadMetadataRepository soknadMetadataRepository;
//
//    @InjectMocks
//    private DittNavMetadataService dittNavMetadataService;
//
//    @Test
//    void skalHenteAktivePabegynteSoknaderForBruker() {
//        var soknadMetadata = createSoknadMetadata(false);
//
//        when(soknadMetadataRepository.hentPabegynteSoknaderForBruker("12345", false))
//                .thenReturn(Collections.singletonList(soknadMetadata));
//
//        var dtos = dittNavMetadataService.hentAktivePabegynteSoknader("12345");
//
//        assertThat(dtos).hasSize(1);
//        assertThat(dtos.get(0).getEventId()).isEqualTo(soknadMetadata.behandlingsId + "_aktiv");
//        assertThat(dtos.get(0).getGrupperingsId()).isEqualTo(soknadMetadata.behandlingsId);
//        assertThat(dtos.get(0).isAktiv()).isTrue();
//    }
//
//    @Test
//    void skalHenteInaktivePabegynteSoknaderForBruker() {
//        var soknadMetadata = createSoknadMetadata(true);
//
//        when(soknadMetadataRepository.hentPabegynteSoknaderForBruker("12345", true))
//                .thenReturn(Collections.singletonList(soknadMetadata));
//
//        var dtos = dittNavMetadataService.hentInaktivePabegynteSoknader("12345");
//
//        assertThat(dtos).hasSize(1);
//        assertThat(dtos.get(0).getEventId()).isEqualTo(soknadMetadata.behandlingsId + "_inaktiv");
//        assertThat(dtos.get(0).getGrupperingsId()).isEqualTo(soknadMetadata.behandlingsId);
//        assertThat(dtos.get(0).isAktiv()).isFalse();
//    }
//
//    @Test
//    void markerPabegyntSoknadSomLest_skalGiFalse_hvisRepositoryReturnererNull() {
//        when(soknadMetadataRepository.hent(anyString()))
//                .thenReturn(null);
//
//        var markert = dittNavMetadataService.oppdaterLestDittNavForPabegyntSoknad("behandlingsId", "12345");
//
//        assertThat(markert).isFalse();
//    }
//
//    @Test
//    void markerPabegyntSoknadSomLest_skalGiFalse_hvisNoeFeiler() {
//        var soknadMetadata = createSoknadMetadata(false);
//
//        when(soknadMetadataRepository.hent(anyString()))
//                .thenReturn(soknadMetadata);
//        doThrow(new RuntimeException("Noe feilet")).when(soknadMetadataRepository).oppdaterLestDittNav(any(SoknadMetadata.class), anyString());
//
//        var markert = dittNavMetadataService.oppdaterLestDittNavForPabegyntSoknad("behandlingsId", "12345");
//
//        assertThat(markert).isFalse();
//    }
//
//    private SoknadMetadata createSoknadMetadata(boolean lestDittNav) {
//        var soknadMetadata = new SoknadMetadata();
//        soknadMetadata.fnr = "12345";
//        soknadMetadata.behandlingsId = "beh123";
//        soknadMetadata.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID;
//        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
//        soknadMetadata.opprettetDato = LocalDateTime.now().minusDays(10);
//        soknadMetadata.innsendtDato = LocalDateTime.now().minusDays(2);
//        soknadMetadata.sistEndretDato = LocalDateTime.now().minusDays(2);
//        soknadMetadata.lestDittNav = lestDittNav;
//        return soknadMetadata;
//    }
//}