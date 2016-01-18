package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

public class DefaultVedleggServiceIntegrationTest {
    /*
    @Test
    public void skalGenerereEttVedleggOmFlereTillattErFalse() {
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1").medValue("true").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedlegg2").medValue("true").medFaktumId(2L);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
        assertThat(vedlegg).extracting("faktumId").containsNull();
        assertThat(vedlegg).extracting("skjemaNummer").contains("v4");
    }


    @Test
    public void skalGenerereVedleggNaarVerdiStemmer() {
        Faktum faktum = new Faktum().medKey("faktumMedToOnValue").medValue("riktigVerdi1").medFaktumId(1L);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
        assertThat(vedlegg).extracting("faktumId").containsNull();
        assertThat(vedlegg).extracting("skjemaNummer").contains("v3");

        faktum.medValue("riktigVerdi2");
        vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
        assertThat(vedlegg).extracting("faktumId").containsNull();
        assertThat(vedlegg).extracting("skjemaNummer").contains("v3");
    }

    @Test
    public void skalSetteVedleggTilIkkeVedleggOmIngenFaktumMatcher(){
        Faktum faktum = new Faktum().medKey("faktumMedVedleggOnTrue").medValue("false").medFaktumId(1L);
        Vedlegg vedleggForFaktum = new Vedlegg().medSkjemaNummer("v6").medInnsendingsvalg(VedleggKreves);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum)
                        .medVedlegg(vedleggForFaktum));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(0);
        assertThat(vedleggForFaktum.getInnsendingsvalg()).isEqualTo(IkkeVedlegg);
        verify(vedleggRepository).opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(eq(vedleggForFaktum));
    }

    @Test
    public void skalSjekkeParentSinParentFaktaVedUthenting() {
        Faktum parentSinParent = new Faktum().medFaktumId(1L).medKey("parentSinParent").medValue("true");
        Faktum parent = new Faktum().medFaktumId(2L).medParrentFaktumId(1L).medKey("parent").medValue("true");
        Faktum vedlegg1 = new Faktum().medFaktumId(3L).medParrentFaktumId(2L).medKey("parent.faktumMedParentPaaTrue").medValue("true");

        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true))).thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                .medFaktum(vedlegg1).medFaktum(parent).medFaktum(parentSinParent));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).extracting("skjemaNummer").contains("v1");
        assertThat(vedlegg).hasSize(1);

        parentSinParent.setValue("false");
        vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(0);
    }

    @Test
    public void skalIkkeGenerereNyttVedleggOmEtAlleredeFinnesMenOppdatereDetEksistende(){
        Faktum faktum = new Faktum().medKey("faktumMedVedleggOnTrue").medValue("true").medFaktumId(1L);
        Vedlegg vedleggForFaktum = new Vedlegg().medSkjemaNummer("v6").medInnsendingsvalg(IkkeVedlegg);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum)
                        .medVedlegg(vedleggForFaktum));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
        assertThat(vedlegg).contains(vedleggForFaktum);
        assertThat(vedleggForFaktum.getInnsendingsvalg()).isEqualTo(VedleggKreves);
        verify(vedleggRepository).opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(eq(vedleggForFaktum));
    }

    @Test
    public void skalKjoreNyLogikkVedUthentingAvVedleggForEtFaktum(){
        System.setProperty(FunksjonalitetBryter.GammelVedleggsLogikk.nokkel, "false");
        Faktum vedlegg1 = new Faktum().medFaktumId(3L).medKey("toFaktumMedSammeVedlegg1Unik").medValue("true");
        Faktum vedlegg2 = new Faktum().medFaktumId(4L).medKey("toFaktumMedSammeVedlegg2Unik").medValue("true");
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true))).thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                .medFaktum(vedlegg1).medFaktum(vedlegg2));
        when(faktaService.hentBehandlingsId(3L)).thenReturn("123");
        List<Vedlegg> vedleggs = vedleggService.hentPaakrevdeVedlegg(3L);
        assertThat(vedleggs).hasSize(1);
        assertThat(vedleggs.get(0).getFaktumId()).isEqualTo(vedlegg1.getFaktumId());
    }


    @Test
    public void skalGenerereToVedleggOmSkjemanummerTilleggErSatt(){
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1").medValue("true").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedleggTillegg").medValue("true").medFaktumId(2L);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(2);
        assertThat(vedlegg).extracting("faktumId").containsNull();
        assertThat(vedlegg).extracting("skjemaNummer").contains("v4", "v4");
        assertThat(vedlegg).extracting("skjemanummerTillegg").contains(null, "tillegg");
    }

    @Test
    public void skalGenerereToVedleggOmFlereTillattErTrueOgEtVedlegg(){
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1Unik").medValue("true").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedlegg2Unik").medValue("true").medFaktumId(2L);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(2);
        assertThat(vedlegg).extracting("faktumId").doesNotContainNull();
        assertThat(vedlegg).extracting("skjemaNummer").contains("v5", "v5");
    }



    @Test
    public void skalGenerereEttVedleggVedFaktumMedRiktigParentfaktumVerdi() {
        Faktum parentFaktum1 = new Faktum().medKey("parentfaktum").medValue("true").medFaktumId(1L);
        Faktum parentFaktum2 = new Faktum().medKey("parentfaktum").medValue("false").medFaktumId(2L);

        Faktum barnefaktum1 = new Faktum().medKey("barnefaktum")
                .medFaktumId(3L)
                .medValue("true")
                .medParrentFaktumId(1L);

        Faktum barnefaktum2 = new Faktum().medKey("barnefaktum")
                .medFaktumId(4L)
                .medValue("true")
                .medParrentFaktumId(2L);

        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(parentFaktum1)
                        .medFaktum(parentFaktum2)
                        .medFaktum(barnefaktum1)
                        .medFaktum(barnefaktum2));

        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
    }

    @Test
    public void skalGenerereVedleggOmEtErTrue() {
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1").medValue("false").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedlegg2").medValue("true").medFaktumId(2L);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
        assertThat(vedlegg).extracting("faktumId").containsNull();
        assertThat(vedlegg).extracting("skjemaNummer").contains("v4");
    }

    @Test
    public void skalSjekkeParentFaktaVedUthenting() {
        Faktum parentSinParent = new Faktum().medFaktumId(1L).medKey("parentSinParent").medValue("true");
        Faktum parent = new Faktum().medFaktumId(2L).medParrentFaktumId(1L).medKey("parent").medValue("true").medProperty("parentProp", "true");
        Faktum vedlegg1 = new Faktum().medFaktumId(3L).medParrentFaktumId(2L).medKey("parent.faktumMedParentPaaTrue").medValue("true");
        Faktum vedlegg2 = new Faktum().medFaktumId(4L).medParrentFaktumId(2L).medKey("parent.faktumMedParentPropPaaTrue").medValue("true");

        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true))).thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                .medFaktum(vedlegg1).medFaktum(vedlegg2).medFaktum(parent).medFaktum(parentSinParent));

        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).extracting("skjemaNummer").contains("v1", "v2");
        assertThat(vedlegg).extracting("faktumId").containsNull();
        assertThat(vedlegg).hasSize(2);

        parent.setValue("false");
        parent.getProperties().put("parentProp", "false");
        vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(0);
    }
    */
}
