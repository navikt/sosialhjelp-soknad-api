package no.nav.sbl.dialogarena.dokumentinnsending.mocks;

import no.nav.sbl.dialogarena.dokumentinnsending.config.ConsumerConfigTest;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.repository.SoknadRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTERNT_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.NAV_VEDLEGG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConsumerConfigTest.class)
public class SoknadRepositoryTest {

    @Inject
    private SoknadRepository soknadRepository;

    @Before
    public void oppsett() {
        soknadRepository.lagSoknader();
    }

    @Test
    public void skalLageKravOmDagpengerSoknad() {
        DokumentSoknad soknad1 = soknadRepository.hentSoknadMedHovedskjema("Krav om dagpenger");
        Dokument hovedSkjema = soknad1.hovedskjema;
        Dokument navVedlegg = soknad1.finnVedleggAvType(NAV_VEDLEGG).get(0);

        assertThat(hovedSkjema.getNavn(), is("Krav om dagpenger"));
        assertThat(navVedlegg.getNavn(), is("Dokumentasjon av sluttårsak"));

        Dokument eksterntVedlegg1 = soknad1.finnVedleggAvType(EKSTERNT_VEDLEGG).get(0);
        Dokument eksterntVedlegg2 = soknad1.finnVedleggAvType(EKSTERNT_VEDLEGG).get(1);

        assertThat(eksterntVedlegg1.getNavn(), is("Kopi av arbeidsavtale/sluttårsak"));
        assertThat(eksterntVedlegg2.getNavn(), is("Permitteringsvarsel"));
    }

    @Test
    public void skalLageKravOmBarnetrygdSoknad() {
        DokumentSoknad soknad1 = soknadRepository.hentSoknadMedHovedskjema("Sjøfartsbok/hyreavregning");
        Dokument hovedSkjema1 = soknad1.hovedskjema;
        Dokument navVedlegg = soknad1.finnVedleggAvType(NAV_VEDLEGG).get(0);

        assertThat(hovedSkjema1.getNavn(), is("Sjøfartsbok/hyreavregning"));
        assertThat(navVedlegg.getNavn(), is("Tilleggsblankett til krav om utbetaling av barnetrygd og/eller kontantstøtte på grunnlag av regler om eksport etter EØS-avtalen"));

        Dokument eksterntVedlegg1 = soknad1.finnVedleggAvType(EKSTERNT_VEDLEGG).get(0);
        Dokument eksterntVedlegg2 = soknad1.finnVedleggAvType(EKSTERNT_VEDLEGG).get(1);
        Dokument eksterntVedlegg3 = soknad1.finnVedleggAvType(EKSTERNT_VEDLEGG).get(2);

        assertThat(eksterntVedlegg1.getNavn(), is("Avtale om delt bosted"));
        assertThat(eksterntVedlegg2.getNavn(), is("Bekreftelse på oppholdstillatelse"));
        assertThat(eksterntVedlegg3.getNavn(), is("Kopi av arbeidsavtale og skattekort"));
    }

    @Test
    public void lagreSoknadLagrerSoknadenOgReturnererSoknadsId() {
        String ident = "1234567890";
        DokumentSoknad soknad = new DokumentSoknad();
        soknad.ident = ident;

        String soknadsId = soknadRepository.lagreSoknad(soknad);
        DokumentSoknad hentetSoknad = soknadRepository.hentSoknadFraSoknadsId(soknadsId);

        assertThat(hentetSoknad.ident, is(ident));
    }

    @Test
    public void knyttSoknadTilBrukerKnytterGittIdentMedSoknad() {
        String ident = "1234567890";
        DokumentSoknad soknad = new DokumentSoknad();

        String soknadsId = soknadRepository.lagreSoknad(soknad);
        soknadRepository.knyttSoknadTilBruker(soknadsId, ident);
        DokumentSoknad hentetSoknad = soknadRepository.hentSoknadFraSoknadsId(soknadsId);

        assertThat(hentetSoknad.ident, is(ident));
    }

    @Test
    public void getSoknadListeReturnererListeMedSoknader() {
        List<DokumentSoknad> soknadListe = soknadRepository.getSoknadListe();

        assertThat(soknadListe.size(), greaterThan(0));
    }
}