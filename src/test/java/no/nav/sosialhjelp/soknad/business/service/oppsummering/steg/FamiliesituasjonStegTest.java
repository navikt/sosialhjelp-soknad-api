package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class FamiliesituasjonStegTest {

    private final FamiliesituasjonSteg steg = new FamiliesituasjonSteg();

    @Test
    void ikkeUtfyltSivilstatus() {
        var soknad = createSoknad(null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(sivilstatusSporsmal.getErUtfylt()).isFalse();
    }

    @Test
    void brukerUtfyltSivilstatus_ulikGift() {
        var ugift = new JsonSivilstatus()
                .withKilde(JsonKilde.BRUKER)
                .withStatus(JsonSivilstatus.Status.UGIFT);
        var soknad = createSoknad(ugift, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(sivilstatusSporsmal.getErUtfylt()).isTrue();
        assertThat(sivilstatusSporsmal.getFelt()).hasSize(1);
        assertThat(sivilstatusSporsmal.getFelt().get(0).getSvar()).isEqualTo("familie.sivilstatus.ugift");
        assertThat(sivilstatusSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
    }

    @Test
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
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(sivilstatusSporsmal.getErUtfylt()).isFalse(); // fnr og pnr mangler
        assertThat(sivilstatusSporsmal.getFelt()).hasSize(1);
        assertThat(sivilstatusSporsmal.getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(sivilstatusSporsmal.getFelt().get(0).getLabelSvarMap())
                .hasSize(4)
                .containsEntry("familie.sivilstatus.gift.ektefelle.navn.label", "Gul Knapp")
                .containsEntry("familie.sivilstatus.gift.ektefelle.fnr.label", null)
                .containsEntry("familie.sivilstatus.gift.ektefelle.pnr.label", null)
                .containsEntry("familie.sivilstatus.gift.ektefelle.borsammen.sporsmal", "familie.sivilstatus.gift.ektefelle.borsammen.true");
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
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(sivilstatusSporsmal.getErUtfylt()).isTrue();
        assertThat(sivilstatusSporsmal.getFelt()).hasSize(1);
        assertThat(sivilstatusSporsmal.getFelt().get(0).getSvar()).isEqualTo("system.familie.sivilstatus.ikkeTilgang.label");
        assertThat(sivilstatusSporsmal.getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA);
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
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);
        var sivilstatusSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(sivilstatusSporsmal.getErUtfylt()).isTrue();
        assertThat(sivilstatusSporsmal.getFelt()).hasSize(1);
        assertThat(sivilstatusSporsmal.getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA_MAP);

        assertThat(sivilstatusSporsmal.getFelt().get(0).getLabelSvarMap())
                .hasSize(4)
                .containsKey("system.familie.sivilstatus.label")
                .containsEntry("system.familie.sivilstatus.gift.ektefelle.navn", "Gul Knapp")
                .containsEntry("system.familie.sivilstatus.gift.ektefelle.fodselsdato", "1999-12-31")
                .containsEntry("system.familie.sivilstatus.gift.ektefelle.folkereg", "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true");
    }

    @Test
    void ingenSystemBarn() {
        var forsorgerplikt = new JsonForsorgerplikt();

        var soknad = createSoknad(null, forsorgerplikt);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(1).getSporsmal()).hasSize(1);
        var forsorgerpliktSporsmal = res.getAvsnitt().get(1).getSporsmal().get(0);
        assertThat(forsorgerpliktSporsmal.getErUtfylt()).isTrue();
        assertThat(forsorgerpliktSporsmal.getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA);
        assertThat(forsorgerpliktSporsmal.getFelt().get(0).getSvar()).isEqualTo("familierelasjon.ingen_registrerte_barn_tekst");
    }

    @Test
    void harSystemBarn_ikkeUtfyltDeltBosted_ikkeUtfyltBarnebidrag() {
        var forsorgerplikt = new JsonForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(Boolean.TRUE))
                .withAnsvar(singletonList(new JsonAnsvar()
                        .withBarn(new JsonBarn()
                                .withKilde(JsonKilde.SYSTEM)
                                .withNavn(new JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                .withFodselsdato("2020-02-02")
                                .withPersonIdentifikator("11111111111"))
                        .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen().withVerdi(Boolean.TRUE))
                        .withHarDeltBosted(null)))
                .withBarnebidrag(null);

        var soknad = createSoknad(null, forsorgerplikt);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        var forsorgerpliktSporsmal = res.getAvsnitt().get(1).getSporsmal();
        assertThat(forsorgerpliktSporsmal).hasSize(3);
        var systemBarnSporsmal = forsorgerpliktSporsmal.get(0);
        assertThat(systemBarnSporsmal.getErUtfylt()).isTrue();
        assertThat(systemBarnSporsmal.getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(systemBarnSporsmal.getFelt().get(0).getLabelSvarMap())
                .hasSize(3)
                .containsEntry("familie.barn.true.barn.navn.label", "Grønn Jakke")
                .containsEntry("familierelasjon.fodselsdato", "2020-02-02")
                .containsEntry("familierelasjon.samme_folkeregistrerte_adresse", "system.familie.barn.true.barn.folkeregistrertsammen.true");

        var deltBostedSporsmal = forsorgerpliktSporsmal.get(1);
        assertThat(deltBostedSporsmal.getErUtfylt()).isFalse();
        assertThat(deltBostedSporsmal.getFelt()).isNull();

        var barnebidragSporsmal = forsorgerpliktSporsmal.get(2);
        assertThat(barnebidragSporsmal.getErUtfylt()).isFalse();
        assertThat(barnebidragSporsmal.getFelt()).isNull();
    }

    @Test
    void harSystemBarn_utfyltDeltBosted_utfyltBarnebidrag() {
        var forsorgerplikt = new JsonForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(Boolean.TRUE))
                .withAnsvar(singletonList(new JsonAnsvar()
                        .withBarn(new JsonBarn()
                                .withKilde(JsonKilde.SYSTEM)
                                .withNavn(new JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                .withFodselsdato("2020-02-02")
                                .withPersonIdentifikator("11111111111"))
                        .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen()
                                .withVerdi(Boolean.TRUE))
                        .withHarDeltBosted(new JsonHarDeltBosted()
                                .withVerdi(Boolean.TRUE))))
                .withBarnebidrag(new JsonBarnebidrag()
                        .withVerdi(JsonBarnebidrag.Verdi.BETALER)
                );

        var soknad = createSoknad(null, forsorgerplikt);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        var forsorgerpliktSporsmal = res.getAvsnitt().get(1).getSporsmal();
        assertThat(forsorgerpliktSporsmal).hasSize(3);
        var systemBarnSporsmal = forsorgerpliktSporsmal.get(0);
        assertThat(systemBarnSporsmal.getErUtfylt()).isTrue();

        var deltBostedSporsmal = forsorgerpliktSporsmal.get(1);
        assertThat(deltBostedSporsmal.getErUtfylt()).isTrue();
        assertThat(deltBostedSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(deltBostedSporsmal.getFelt().get(0).getSvar()).isEqualTo("system.familie.barn.true.barn.deltbosted.true");

        var barnebidragSporsmal = forsorgerpliktSporsmal.get(2);
        assertThat(barnebidragSporsmal.getErUtfylt()).isTrue();
        assertThat(barnebidragSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(barnebidragSporsmal.getFelt().get(0).getSvar()).isEqualTo("familie.barn.true.barnebidrag.betaler");
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