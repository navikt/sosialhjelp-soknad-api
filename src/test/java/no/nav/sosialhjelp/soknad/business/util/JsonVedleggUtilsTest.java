package no.nav.sosialhjelp.soknad.business.util;

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.ANNET;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonVedleggUtilsTest {

    @Test
    public void doNot_addHendelseTypeAndHendelseReferanse_ifUnleashToggleIsDeactivated() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse()).isNull();

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, false);

        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseReferanse()).isNull();
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_forSoknad_ifUnleashToggleIsActivated() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse()).isNull();

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, true);

        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType()).isEqualTo(JsonVedlegg.HendelseType.SOKNAD);
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse()).isNotNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType()).isEqualTo(JsonVedlegg.HendelseType.SOKNAD);
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse()).isNotNull();
        // annet|annet -> hendelseType:bruker uten hendelseReferanse
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseType()).isEqualTo(JsonVedlegg.HendelseType.BRUKER);
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseReferanse()).isNull();
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_shouldAddUniqueReferanse() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, true);

        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse())
                .isNotEqualTo(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse());
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_forEttersendelse_shouldOnlyAddHendelseTypeBrukerForAnnetAnnet() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse()).isNull();

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, false, true);

        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse()).isNull();
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseType()).isEqualTo(JsonVedlegg.HendelseType.BRUKER);
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseReferanse()).isNull();
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_forEttersendelse_shouldNotEditHendelseReferanse() {
        String hendelseReferanse = "1234";
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        jsonVedleggSpesifikasjon.getVedlegg().get(0).setHendelseType(JsonVedlegg.HendelseType.SOKNAD);
        jsonVedleggSpesifikasjon.getVedlegg().get(0).setHendelseReferanse(hendelseReferanse);
        jsonVedleggSpesifikasjon.getVedlegg().get(1).setHendelseType(JsonVedlegg.HendelseType.BRUKER);

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, false, true);

        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType()).isEqualTo(JsonVedlegg.HendelseType.SOKNAD);
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse()).isEqualTo(hendelseReferanse);
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType()).isEqualTo(JsonVedlegg.HendelseType.BRUKER);
        assertThat(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse()).isNull();
    }

    private JsonVedleggSpesifikasjon createJsonVedleggSpesifikasjon() {
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.VedleggKreves.name())
                .withType("annet")
                .withTilleggsinfo("tilleggsinfo1"));
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType("type1")
                .withTilleggsinfo("annet")
                .withFiler(lagJsonFiler()));
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType(ANNET)
                .withTilleggsinfo(ANNET)
                .withFiler(lagJsonFiler()));
        return new JsonVedleggSpesifikasjon()
                .withVedlegg(jsonVedlegg);
    }

    private List<JsonFiler> lagJsonFiler() {
        List<JsonFiler> filer = new ArrayList<>();
        filer.add(new JsonFiler()
                .withFilnavn("filnavn")
                .withSha512("sha1"));
        return filer;
    }
}