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
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
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
        validateFeltMedSvar(sivilstatusSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "familie.sivilstatus.ugift");
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
        var labelSvarMap = sivilstatusSporsmal.getFelt().get(0).getLabelSvarMap();
        assertThat(labelSvarMap).hasSize(4);
        assertThat(labelSvarMap.get("familie.sivilstatus.gift.ektefelle.navn.label").getValue()).isEqualTo("Gul Knapp");
        assertThat(labelSvarMap.get("familie.sivilstatus.gift.ektefelle.fnr.label").getValue()).isNull();
        assertThat(labelSvarMap.get("familie.sivilstatus.gift.ektefelle.pnr.label").getValue()).isNull();
        assertThat(labelSvarMap.get("familie.sivilstatus.gift.ektefelle.borsammen.sporsmal").getValue()).isEqualTo("familie.sivilstatus.gift.ektefelle.borsammen.true");
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
        validateFeltMedSvar(sivilstatusSporsmal.getFelt().get(0), Type.SYSTEMDATA, SvarType.LOCALE_TEKST, "system.familie.sivilstatus.ikkeTilgang.label");
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
        var labelSvarMap = sivilstatusSporsmal.getFelt().get(0).getLabelSvarMap();
        assertThat(labelSvarMap).hasSize(3);
        assertThat(labelSvarMap.get("system.familie.sivilstatus.gift.ektefelle.navn").getValue()).isEqualTo("Gul Knapp");
        assertThat(labelSvarMap.get("system.familie.sivilstatus.gift.ektefelle.fodselsdato").getValue()).isEqualTo("1999-12-31");
        assertThat(labelSvarMap.get("system.familie.sivilstatus.gift.ektefelle.folkereg").getValue()).isEqualTo("system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true");
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
        validateFeltMedSvar(forsorgerpliktSporsmal.getFelt().get(0), Type.SYSTEMDATA, SvarType.LOCALE_TEKST, "familierelasjon.ingen_registrerte_barn_tekst");
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
        var labelSvarMap = systemBarnSporsmal.getFelt().get(0).getLabelSvarMap();
        assertThat(labelSvarMap).hasSize(3);
        assertThat(labelSvarMap.get("familie.barn.true.barn.navn.label").getValue()).isEqualTo("Grønn Jakke");
        assertThat(labelSvarMap.get("familierelasjon.fodselsdato").getValue()).isEqualTo("2020-02-02");
        assertThat(labelSvarMap.get("familierelasjon.samme_folkeregistrerte_adresse").getValue()).isEqualTo("system.familie.barn.true.barn.folkeregistrertsammen.true");

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
        assertThat(deltBostedSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(deltBostedSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "system.familie.barn.true.barn.deltbosted.true");

        var barnebidragSporsmal = forsorgerpliktSporsmal.get(2);
        assertThat(barnebidragSporsmal.getErUtfylt()).isTrue();
        assertThat(barnebidragSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(barnebidragSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "familie.barn.true.barnebidrag.betaler");
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