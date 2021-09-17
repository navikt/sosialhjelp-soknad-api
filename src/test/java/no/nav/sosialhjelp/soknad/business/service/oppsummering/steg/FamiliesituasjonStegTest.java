package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FamiliesituasjonStegTest {

    private final FamiliesituasjonSteg steg = new FamiliesituasjonSteg();

    @Test
    void ikkeUtfyltSivilstatus() {
        var soknad = createSoknad(null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(sivilstatusSporsmal).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getErUtfylt()).isFalse();
    }

    @Test
    void brukerUtfyltSivilstatus_ulikGift() {
        var ugift = new JsonSivilstatus()
                .withKilde(JsonKilde.BRUKER)
                .withStatus(JsonSivilstatus.Status.UGIFT);
        var soknad = createSoknad(ugift, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(sivilstatusSporsmal).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getErUtfylt()).isTrue();
        assertThat(sivilstatusSporsmal.get(0).getFelt()).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getFelt().get(0).getSvar()).isEqualTo("familie.sivilstatus.ugift");
        assertThat(sivilstatusSporsmal.get(0).getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
    }

    @Test
    @Disabled("ikke implementert enda")
    void brukerUtfyltSivilstatus_ektefelle_manglerFelter() {
        var gift = new JsonSivilstatus()
                .withKilde(JsonKilde.BRUKER)
                .withStatus(JsonSivilstatus.Status.GIFT)
                .withEktefelle(new JsonEktefelle()
                        .withNavn(new JsonNavn().withFornavn("Gul").withEtternavn("Knapp"))
                        .withFodselsdato(null)
                        .withPersonIdentifikator(null)
                )
                .withBorSammenMed(true);
        var soknad = createSoknad(gift, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(sivilstatusSporsmal).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getErUtfylt()).isTrue();
        // mer assertions her
    }

    @Test
    void systemSivilstatus_ektefelleMedAdressebeskyttelse() {
        var giftMedAdressebeskyttelse = new JsonSivilstatus()
                .withKilde(JsonKilde.SYSTEM)
                .withStatus(JsonSivilstatus.Status.GIFT)
                .withEktefelleHarDiskresjonskode(true);
        var soknad = createSoknad(giftMedAdressebeskyttelse, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(sivilstatusSporsmal).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getErUtfylt()).isTrue();
        assertThat(sivilstatusSporsmal.get(0).getFelt()).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getFelt().get(0).getSvar()).isEqualTo("system.familie.sivilstatus.ikkeTilgang.label");
        assertThat(sivilstatusSporsmal.get(0).getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA);
    }

    @Test
    void systemSivilstatus_ektefelle() {
        var giftMedAdressebeskyttelse = new JsonSivilstatus()
                .withKilde(JsonKilde.SYSTEM)
                .withStatus(JsonSivilstatus.Status.GIFT)
                .withEktefelle(new JsonEktefelle()
                        .withNavn(new JsonNavn().withFornavn("Gul").withEtternavn("Knapp"))
                        .withFodselsdato("1999-12-31")
                        .withPersonIdentifikator("11111111111")
                )
                .withFolkeregistrertMedEktefelle(true);
        var soknad = createSoknad(giftMedAdressebeskyttelse, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(sivilstatusSporsmal).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getErUtfylt()).isTrue();
        assertThat(sivilstatusSporsmal.get(0).getFelt()).hasSize(1);
        assertThat(sivilstatusSporsmal.get(0).getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA_MAP);

        assertThat(sivilstatusSporsmal.get(0).getFelt().get(0).getLabelSvarMap())
                .hasSize(4)
                .containsKey("system.familie.sivilstatus.label")
                .containsEntry("system.familie.sivilstatus.gift.ektefelle.navn", "Gul Knapp")
                .containsEntry("system.familie.sivilstatus.gift.ektefelle.fodselsdato", "1999-12-31")
                .containsEntry("system.familie.sivilstatus.gift.ektefelle.folkereg", "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true");
    }

    private JsonInternalSoknad createSoknad(JsonSivilstatus sivilstatus, JsonForsorgerplikt forsorgerplikt) {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withData(new JsonData()
                                .withFamilie(new JsonFamilie()
                                        .withSivilstatus(sivilstatus)
                                        .withForsorgerplikt(forsorgerplikt)
                                )
                        )
                );
    }

}