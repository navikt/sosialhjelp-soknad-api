package no.nav.sosialhjelp.soknad.business.util;

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse;
import static org.junit.Assert.*;

public class JsonVedleggUtilsTest {

    @Test
    public void doNot_addHendelseTypeAndHendelseReferanse_ifUnleashToggleIsDeactivated() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, false);

        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse());
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_ifUnleashToggleIsActivated() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true);

        assertNotNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseType());
        assertNotNull(jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse());
        assertNotNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseType());
        assertNotNull(jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse());
    }

    @Test
    public void addHendelseTypeAndHendelseReferanse_shouldAddUniqueReferanse() {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon();
        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true);

        assertNotEquals(
                jsonVedleggSpesifikasjon.getVedlegg().get(0).getHendelseReferanse(),
                jsonVedleggSpesifikasjon.getVedlegg().get(1).getHendelseReferanse()
        );
    }

    private JsonVedleggSpesifikasjon createJsonVedleggSpesifikasjon() {
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.VedleggKreves.name())
                .withType("type1")
                .withTilleggsinfo("tilleggsinfo1"));
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType("type1")
                .withTilleggsinfo("tilleggsinfo2")
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