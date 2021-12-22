//package no.nav.sosialhjelp.soknad.domain.model.oidc;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//
//import static no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler.getSubjectHandlerService;
//import static no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler.setSubjectHandlerService;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//
//class SubjectHandlerTest {
//
//    @AfterEach
//    public void tearDown() {
//        System.clearProperty("environment.name");
//        SubjectHandlerUtils.resetSubjectHandlerImpl();
//    }
//
//    @Test
//    void setSubjectHandlerService_iProdMiljo_skalGiException() {
//        System.setProperty("environment.name", "p");
//        assertThatExceptionOfType(RuntimeException.class)
//                .isThrownBy(() -> setSubjectHandlerService(new StaticSubjectHandlerService()));
//    }
//
//    @Test
//    void setSubjectHandlerService_iUkjentMiljo_skalGiException() {
//        System.setProperty("environment.name", "ukjent");
//        assertThatExceptionOfType(RuntimeException.class)
//                .isThrownBy(() -> setSubjectHandlerService(new StaticSubjectHandlerService()));
//    }
//
//    @Test
//    void setSubjectHandlerService_lokalt_skalSetteSubjectHandler() {
//        System.setProperty("environment.name", "local");
//        setSubjectHandlerService(new StaticSubjectHandlerService());
//        assertThat(getSubjectHandlerService()).isExactlyInstanceOf(StaticSubjectHandlerService.class);
//    }
//}