package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.ANNET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SoknadMetricsServiceTest {

    @Mock
    SoknadMetricsService metricsService;



    @Test
    public void reportVedleggskrav_shouldReportCorrect() {
        doCallRealMethod().when(metricsService).countAndreportVedleggskrav(anyBoolean(), any());

        List<SoknadMetadata.VedleggMetadata> vedleggList = new ArrayList<>();
        vedleggList.add(createVedleggMetadata(Vedleggstatus.LastetOpp, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.LastetOpp, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.LastetOpp, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggAlleredeSendt, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggAlleredeSendt, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, ANNET, ANNET));

        metricsService.countAndreportVedleggskrav(true, vedleggList);
        verify(metricsService).reportVedleggskrav(eq(true), eq(9), eq(3), eq(2), eq(4));
    }

    @Test
    public void reportVedleggskrav_with3LastetOpp_shouldReport3() {
        doCallRealMethod().when(metricsService).countAndreportVedleggskrav(anyBoolean(), any());

        List<SoknadMetadata.VedleggMetadata> vedleggList = new ArrayList<>();
        vedleggList.add(createVedleggMetadata(Vedleggstatus.LastetOpp, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.LastetOpp, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.LastetOpp, "skjema", "tillegg"));

        metricsService.countAndreportVedleggskrav(true, vedleggList);
        verify(metricsService).reportVedleggskrav(eq(true), eq(3), eq(3), eq(0), eq(0));
    }

    @Test
    public void reportVedleggskrav_with3Kreves_shouldReport3() {
        doCallRealMethod().when(metricsService).countAndreportVedleggskrav(anyBoolean(), any());

        List<SoknadMetadata.VedleggMetadata> vedleggList = new ArrayList<>();
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, "skjema", "tillegg"));

        metricsService.countAndreportVedleggskrav(true, vedleggList);
        verify(metricsService).reportVedleggskrav(eq(true), eq(3), eq(0), eq(0), eq(3));
    }

    @Test
    public void reportVedleggskrav_with3LevertTidligere_shouldReport3() {
        doCallRealMethod().when(metricsService).countAndreportVedleggskrav(anyBoolean(), any());

        List<SoknadMetadata.VedleggMetadata> vedleggList = new ArrayList<>();
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggAlleredeSendt, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggAlleredeSendt, "skjema", "tillegg"));
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggAlleredeSendt, "skjema", "tillegg"));

        metricsService.countAndreportVedleggskrav(true, vedleggList);
        verify(metricsService).reportVedleggskrav(eq(true), eq(3), eq(0), eq(3), eq(0));
    }

    @Test
    public void reportVedleggskrav_withAnnetLastetOpp_shouldReportZero() {
        doCallRealMethod().when(metricsService).countAndreportVedleggskrav(anyBoolean(), any());

        List<SoknadMetadata.VedleggMetadata> vedleggList = new ArrayList<>();
        vedleggList.add(createVedleggMetadata(Vedleggstatus.LastetOpp, ANNET, ANNET));

        metricsService.countAndreportVedleggskrav(true, vedleggList);
        verify(metricsService).reportVedleggskrav(eq(true), eq(0), eq(0), eq(0), eq(0));
    }

    @Test
    public void reportVedleggskrav_withAnnetKreves_shouldReportZero() {
        doCallRealMethod().when(metricsService).countAndreportVedleggskrav(anyBoolean(), any());

        List<SoknadMetadata.VedleggMetadata> vedleggList = new ArrayList<>();
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggKreves, ANNET, ANNET));

        metricsService.countAndreportVedleggskrav(true, vedleggList);
        verify(metricsService).reportVedleggskrav(eq(true), eq(0), eq(0), eq(0), eq(0));
    }

    @Test
    public void reportVedleggskrav_withAnnetLevertTidligere_shouldReportZero() {
        doCallRealMethod().when(metricsService).countAndreportVedleggskrav(anyBoolean(), any());

        List<SoknadMetadata.VedleggMetadata> vedleggList = new ArrayList<>();
        vedleggList.add(createVedleggMetadata(Vedleggstatus.VedleggAlleredeSendt, ANNET, ANNET));

        metricsService.countAndreportVedleggskrav(true, vedleggList);
        verify(metricsService).reportVedleggskrav(eq(true), eq(0), eq(0), eq(0), eq(0));
    }


    private SoknadMetadata.VedleggMetadata createVedleggMetadata(Vedleggstatus status, String skjema, String tillegg){
        SoknadMetadata.VedleggMetadata vedleggMetadata = new SoknadMetadata.VedleggMetadata();
        vedleggMetadata.status = status;
        vedleggMetadata.skjema = skjema;
        vedleggMetadata.tillegg = tillegg;
        return vedleggMetadata;
    }
}