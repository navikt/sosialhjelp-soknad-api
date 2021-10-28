package no.nav.sosialhjelp.soknad.web.rest.mappers;

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg;
import static org.assertj.core.api.Assertions.assertThat;

class VedleggMapperTest {

    private static final VedleggType BOSTOTTE = new VedleggType("bostotte|annetboutgift");
    private static final VedleggType SKATTEMELDING = new VedleggType("skatt|melding");
    private static final VedleggType ANNET = new VedleggType("annet|annet");
    private static final String EIER = "12345678910";

    @Test
    void skalReturnereAlleVedleggSomSortertListeAvEttersendteVedleggHvisSoknadBleSendtForMindreEnn30DagerSiden() {
        var innsendingstidspunkt = LocalDateTime.now();
        var opplastedeVedlegg = createOpplastetVedleggList();
        var originaleVedlegg = createOriginaleVedlegg();

        var result = mapVedleggToSortedListOfEttersendteVedlegg(innsendingstidspunkt, opplastedeVedlegg, originaleVedlegg);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).type).isEqualTo(BOSTOTTE.getSammensattType());
        assertThat(result.get(0).vedleggStatus).isEqualTo(Vedleggstatus.LastetOpp.toString());
        assertThat(result.get(1).type).isEqualTo(SKATTEMELDING.getSammensattType());
        assertThat(result.get(1).vedleggStatus).isEqualTo(Vedleggstatus.VedleggKreves.toString());
        assertThat(result.get(2).type).isEqualTo(ANNET.getSammensattType());
        assertThat(result.get(2).vedleggStatus).isEqualTo(Vedleggstatus.VedleggKreves.toString());
    }

    @Test
    void skalKunReturnereAnnetOgLastetOppHvisSoknadBleSendtForMerEnn30DagerSiden() {
        var innsendingstidspunkt = LocalDateTime.now().minusDays(31);
        var opplastedeVedlegg = createOpplastetVedleggList();
        var originaleVedlegg = createOriginaleVedlegg();

        var result = mapVedleggToSortedListOfEttersendteVedlegg(innsendingstidspunkt, opplastedeVedlegg, originaleVedlegg);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).type).isEqualTo(BOSTOTTE.getSammensattType());
        assertThat(result.get(0).vedleggStatus).isEqualTo(Vedleggstatus.LastetOpp.toString());
        assertThat(result.get(1).type).isEqualTo(ANNET.getSammensattType());
        assertThat(result.get(1).vedleggStatus).isEqualTo(Vedleggstatus.VedleggKreves.toString());
    }

    private List<JsonVedlegg> createOriginaleVedlegg() {
        return asList(
                new JsonVedlegg()
                        .withType(BOSTOTTE.getType())
                        .withTilleggsinfo(BOSTOTTE.getTilleggsinfo())
                        .withStatus(Vedleggstatus.LastetOpp.toString()),
                new JsonVedlegg()
                        .withType(SKATTEMELDING.getType())
                        .withTilleggsinfo(SKATTEMELDING.getTilleggsinfo())
                        .withStatus(Vedleggstatus.VedleggKreves.toString()),
                new JsonVedlegg()
                        .withType(ANNET.getType())
                        .withTilleggsinfo(ANNET.getTilleggsinfo())
                        .withStatus(Vedleggstatus.VedleggKreves.toString()));
    }

    private List<OpplastetVedlegg> createOpplastetVedleggList() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(createOpplastetVedlegg(BOSTOTTE));
        opplastedeVedlegg.add(createOpplastetVedlegg(ANNET));
        return opplastedeVedlegg;
    }

    private OpplastetVedlegg createOpplastetVedlegg(VedleggType type) {
        return new OpplastetVedlegg()
                .withVedleggType(type)
                .withEier(EIER);
    }

}
