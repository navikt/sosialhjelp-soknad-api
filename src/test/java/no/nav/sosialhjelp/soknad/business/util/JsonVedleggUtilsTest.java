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
import static org.junit.Assert.*;

public class JsonVedleggUtilsTest {

    @Test
    public void doNot_addHendelseTypeAndHendelseReferanse_ifUnleashToggleIsDeactivated() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, false);

        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseReferanse());
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_forSoknad_ifUnleashToggleIsActivated() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, true);

        assertEquals(JsonVedlegg.HendelseType.SOKNAD, jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNotNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());
        assertEquals(JsonVedlegg.HendelseType.SOKNAD, jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType());
        assertNotNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse());
        // annet|annet -> hendelseType:bruker uten hendelseReferanse
        assertEquals(JsonVedlegg.HendelseType.BRUKER, jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseReferanse());
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_shouldAddUniqueReferanse() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, true);

        assertNotEquals(
                jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse(),
                jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse()
        );
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_forEttersendelse_shouldOnlyAddHendelseTypeBrukerForAnnetAnnet() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, false, true);

        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse());
        assertEquals(JsonVedlegg.HendelseType.BRUKER, jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(2).getHendelseReferanse());
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_forEttersendelse_shouldNotEditHendelseReferanse() {
        String hendelseReferanse = "1234";
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        jsonVedleggSpesifikasjon.getVedlegg().get(0).setHendelseType(JsonVedlegg.HendelseType.SOKNAD);
        jsonVedleggSpesifikasjon.getVedlegg().get(0).setHendelseReferanse(hendelseReferanse);
        jsonVedleggSpesifikasjon.getVedlegg().get(1).setHendelseType(JsonVedlegg.HendelseType.BRUKER);

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, false, true);

        assertEquals(JsonVedlegg.HendelseType.SOKNAD, jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertEquals(hendelseReferanse, jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());
        assertEquals(JsonVedlegg.HendelseType.BRUKER, jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse());
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