package no.nav.sbl.dialogarena.service.helpers;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class HentTekstForGDPRInfoHelperTest {

    private String rawtekst;
    private String regex;
    private String replacement;
    private String langTekst;

    private HentTekstForGDPRInfoHelper hentTekstForGDPRInfoHelper;

    @Before
    public void setup() {
        hentTekstForGDPRInfoHelper = new HentTekstForGDPRInfoHelper();
        rawtekst = "asdfasdf {navkontor} \n asdfasdf  {navkontor}";
        regex = "{navkontor}";
        replacement = "NAV Horten";
        langTekst = "<h3>Informasjon</h3>\n" +
                "    <p>Når du søker om økonomisk sosialhjelp digitalt, må du gi opplysninger om deg selv slik at NAV-kontoret ditt kan behandle søknaden. Eksempler på opplysninger er din adresse, familieforhold, inntekter og utgifter.</p>\n" +
                "    <p>NAV vil også hente opplysninger fra andre registre på vegne av kommunen din som skal behandle søknaden:</p>\n" +
                "    <p>Personopplysninger fra Folkeregisteret, skatte- og inntektsopplysninger fra Skatteetaten, arbeidsgiver- og arbeidstakerregisteret og informasjon om ytelser fra NAV.</p>\n" +
                "    <p>Du kan være trygg på at personopplysningene dine blir behandlet på en sikker og riktig måte:</p>\n" +
                "    <ul>\n" +
                "        <li>Vi skal ikke innhente flere opplysninger enn det som er nødvendig.</li>\n" +
                "        <li>NAV har taushetsplikt om alle opplysninger som vi behandler. Hvis offentlige virksomheter eller andre ønsker å få utlevert opplysninger om deg, må de ha hjemmel i lov eller du må gi samtykke til det.</li>\n" +
                "    </ul>\n" +
                "\n" +
                "    <h4>Behandlingsansvarlig</h4>\n" +
                "    <p>Det er {navkontor:oppholdskommunen} som er ansvarlig for å behandle søknaden og personopplysningene dine.</p>\n" +
                "    <p>Henvend deg til kommunen hvis du har spørsmål om personopplysninger. Kommunen har også et personvernombud som du kan kontakte.</p>\n" +
                "    <p>Arbeids- og velferdsdirektoratet har ansvaret for nav.no og behandler den digitale søknaden som en databehandler på vegne av kommunen.</p>\n" +
                "\n" +
                "    <h4>Formålet med å samle inn og bruke personopplysninger</h4>\n" +
                "    <p>Formålet med søknaden er å samle inn tilstrekkelig opplysninger til at kommunen din kan behandle søknaden om økonomisk sosialhjelp. Opplysninger du gir i den digitale søknaden og opplysninger som blir hentet inn, sendes digitalt fra nav.no til NAV-kontoret ditt. Det blir enklere for deg å søke, og NAV-kontoret ditt mottar søknaden ferdig utfylt med nødvendige vedlegg.</p>\n" +
                "    <p>Opplysningene i søknaden vil bli brukt til å vurdere om du fyller vilkårene for økonomisk sosialhjelp, og skal ikke lagres lenger enn det som er nødvendig ut fra formålet. Hvis ikke opplysningene skal oppbevares etter arkivloven eller andre lover, skal de slettes etter bruk.</p>\n" +
                "\n" +
                "    <h4>Lovgrunnlaget</h4>\n" +
                "    <p>Lovgrunnlaget for å samle inn informasjon i forbindelse med søknaden din er lov om sosiale tjenester i Arbeids- og velferdsforvaltningen.</p>\n" +
                "\n" +
                "    <h4>Innhenting av personopplysningene dine</h4>\n" +
                "    <p>Du gir selv flere opplysninger når du søker om økonomisk sosialhjelp. I tillegg henter vi opplysninger som NAV har i sine registre, som for eksempel opplysninger om andre ytelser du har fra NAV. Vi henter også opplysninger fra andre offentlige registre som vi har lov til å hente informasjon fra, for eksempel skatte- og inntektsopplysninger fra Skatteetaten.</p>\n" +
                "\n" +
                "    <h4>Lagring av personopplysningene dine</h4>\n" +
                "\n" +
                "    <h5>På nav.no</h5>\n" +
                "    <p>Søknader som er påbegynt, men ikke fullført, blir lagret hos Arbeids- og velferdsdirektoratet i to uker. Deretter slettes de.</p>\n" +
                "\n" +
                "    <h5>I kommunen din</h5>\n" +
                "    <p>Arkivloven bestemmer hvor lenge opplysninger skal lagres. Ta kontakt med kommunen din hvis du har spørsmål om lagringstid.</p>\n" +
                "\n" +
                "    <h4>Rettigheter som registrert</h4>\n" +
                "    <p>Alle har rett på informasjon om og innsyn i egne personopplysninger etter personopplysningsloven.</p>\n" +
                "    <p>Hvis opplysninger om deg er feil, ufullstendige eller unødvendige, kan du kreve at opplysningene blir rettet eller supplert etter personopplysningsloven. Du kan også i særlige tilfeller be om å få dem slettet, hvis ikke kommunen har en lovpålagt plikt til å lagre opplysningene som dokumentasjon. Slike krav skal besvares kostnadsfritt og senest innen 30 dager.</p>\n" +
                "    <p>Du har også flere personvernrettigheter, blant annet såkalt <strong>rett til begrensning</strong>: Du kan i visse tilfeller ha rett til å få en begrenset behandling av personopplysningene dine. Hvis du har en slik rett, vil opplysningene bli lagret, men ikke brukt.</p>\n" +
                "    <p>Du har også <strong>rett til å protestere</strong> mot behandling av personopplysninger: Det vil si at du i enkelte tilfeller kan ha rett til å protestere mot kommunens ellers lovlige behandling av personopplysninger. Behandlingen må da stanses, og hvis du får medhold vil opplysningene eventuelt bli slettet.</p>\n" +
                "\n" +
                "    <p>Du finner en samlet oversikt over dine rettigheter i Datatilsynets veileder <a href=\"https://www.datatilsynet.no/regelverk-og-skjema/veiledere/de-registrertes-rettigheter-etter-nytt-regelverk/\" class=\"lenke\" target=\"_blank\" rel=\"noreferrer noopener\">De registrertes rettigheter etter nytt regelverk</a>. Kommunen din vil også ha informasjon om behandling av personopplysninger på sine nettsider.</p>\n" +
                "    <p>Alle spørsmål du har om behandling av personopplysningene dine må du rette til {navkontor:NAV-kontoret ditt}.</p>\n" +
                "\n" +
                "    <h4>Klagerett til Datatilsynet</h4>\n" +
                "    <p>Du har rett til å klage til Datatilsynet hvis du ikke er fornøyd med hvordan vi behandler personopplysninger om deg, eller hvis du mener behandlingen er i strid med personvernreglene. Informasjon om hvordan du går frem finner du på nettsidene til <a href=\"https://www.datatilsynet.no/\" class=\"lenke\" target=\"_blank\" rel=\"noreferrer noopener\">Datatilsynet</a>.</p>\n" +
                "    <p><a href=\"https://www.nav.no/no/NAV+og+samfunn/Om+NAV/personvern-i-arbeids-og-velferdsetaten/Personvern+og+sikkerhet+p%C3%A5+nav.no\" class=\"lenke\" target=\"_blank\" rel=\"noreferrer noopener\">Personvern og sikkerhet på nav.no</a></p>\n" +
                "<strong>Søknaden din blir sendt til {navkontor}.</strong><br/>Dette kontoret har ansvar for å behandle søknaden din, og {navkontor} lagrer opplysningene fra søknaden i fagsystemet sitt.\n";
    }

    @Test
    public void testTekstReplacement() {

        rawtekst = rawtekst.replace(regex, replacement);
        System.out.println(rawtekst);

        assertEquals("asdfasdf NAV Horten \n" +
                " asdfasdf  NAV Horten", rawtekst);
    }

    @Test
    public void testErstattTekst2() {

        String resultat = null;
        resultat = hentTekstForGDPRInfoHelper.erstattTekst(new String[]{"{navkontor}", "{navkontor:NAV-kontoret ditt}", "{navkontor:oppholdskommunen}"}, langTekst, "NAV Horten");

        String expected = "<h3>Informasjon</h3>\n" +
                "    <p>Når du søker om økonomisk sosialhjelp digitalt, må du gi opplysninger om deg selv slik at NAV-kontoret ditt kan behandle søknaden. Eksempler på opplysninger er din adresse, familieforhold, inntekter og utgifter.</p>\n" +
                "    <p>NAV vil også hente opplysninger fra andre registre på vegne av kommunen din som skal behandle søknaden:</p>\n" +
                "    <p>Personopplysninger fra Folkeregisteret, skatte- og inntektsopplysninger fra Skatteetaten, arbeidsgiver- og arbeidstakerregisteret og informasjon om ytelser fra NAV.</p>\n" +
                "    <p>Du kan være trygg på at personopplysningene dine blir behandlet på en sikker og riktig måte:</p>\n" +
                "    <ul>\n" +
                "        <li>Vi skal ikke innhente flere opplysninger enn det som er nødvendig.</li>\n" +
                "        <li>NAV har taushetsplikt om alle opplysninger som vi behandler. Hvis offentlige virksomheter eller andre ønsker å få utlevert opplysninger om deg, må de ha hjemmel i lov eller du må gi samtykke til det.</li>\n" +
                "    </ul>\n" +
                "\n" +
                "    <h4>Behandlingsansvarlig</h4>\n" +
                "    <p>Det er NAV Horten som er ansvarlig for å behandle søknaden og personopplysningene dine.</p>\n" +
                "    <p>Henvend deg til kommunen hvis du har spørsmål om personopplysninger. Kommunen har også et personvernombud som du kan kontakte.</p>\n" +
                "    <p>Arbeids- og velferdsdirektoratet har ansvaret for nav.no og behandler den digitale søknaden som en databehandler på vegne av kommunen.</p>\n" +
                "\n" +
                "    <h4>Formålet med å samle inn og bruke personopplysninger</h4>\n" +
                "    <p>Formålet med søknaden er å samle inn tilstrekkelig opplysninger til at kommunen din kan behandle søknaden om økonomisk sosialhjelp. Opplysninger du gir i den digitale søknaden og opplysninger som blir hentet inn, sendes digitalt fra nav.no til NAV-kontoret ditt. Det blir enklere for deg å søke, og NAV-kontoret ditt mottar søknaden ferdig utfylt med nødvendige vedlegg.</p>\n" +
                "    <p>Opplysningene i søknaden vil bli brukt til å vurdere om du fyller vilkårene for økonomisk sosialhjelp, og skal ikke lagres lenger enn det som er nødvendig ut fra formålet. Hvis ikke opplysningene skal oppbevares etter arkivloven eller andre lover, skal de slettes etter bruk.</p>\n" +
                "\n" +
                "    <h4>Lovgrunnlaget</h4>\n" +
                "    <p>Lovgrunnlaget for å samle inn informasjon i forbindelse med søknaden din er lov om sosiale tjenester i Arbeids- og velferdsforvaltningen.</p>\n" +
                "\n" +
                "    <h4>Innhenting av personopplysningene dine</h4>\n" +
                "    <p>Du gir selv flere opplysninger når du søker om økonomisk sosialhjelp. I tillegg henter vi opplysninger som NAV har i sine registre, som for eksempel opplysninger om andre ytelser du har fra NAV. Vi henter også opplysninger fra andre offentlige registre som vi har lov til å hente informasjon fra, for eksempel skatte- og inntektsopplysninger fra Skatteetaten.</p>\n" +
                "\n" +
                "    <h4>Lagring av personopplysningene dine</h4>\n" +
                "\n" +
                "    <h5>På nav.no</h5>\n" +
                "    <p>Søknader som er påbegynt, men ikke fullført, blir lagret hos Arbeids- og velferdsdirektoratet i to uker. Deretter slettes de.</p>\n" +
                "\n" +
                "    <h5>I kommunen din</h5>\n" +
                "    <p>Arkivloven bestemmer hvor lenge opplysninger skal lagres. Ta kontakt med kommunen din hvis du har spørsmål om lagringstid.</p>\n" +
                "\n" +
                "    <h4>Rettigheter som registrert</h4>\n" +
                "    <p>Alle har rett på informasjon om og innsyn i egne personopplysninger etter personopplysningsloven.</p>\n" +
                "    <p>Hvis opplysninger om deg er feil, ufullstendige eller unødvendige, kan du kreve at opplysningene blir rettet eller supplert etter personopplysningsloven. Du kan også i særlige tilfeller be om å få dem slettet, hvis ikke kommunen har en lovpålagt plikt til å lagre opplysningene som dokumentasjon. Slike krav skal besvares kostnadsfritt og senest innen 30 dager.</p>\n" +
                "    <p>Du har også flere personvernrettigheter, blant annet såkalt <strong>rett til begrensning</strong>: Du kan i visse tilfeller ha rett til å få en begrenset behandling av personopplysningene dine. Hvis du har en slik rett, vil opplysningene bli lagret, men ikke brukt.</p>\n" +
                "    <p>Du har også <strong>rett til å protestere</strong> mot behandling av personopplysninger: Det vil si at du i enkelte tilfeller kan ha rett til å protestere mot kommunens ellers lovlige behandling av personopplysninger. Behandlingen må da stanses, og hvis du får medhold vil opplysningene eventuelt bli slettet.</p>\n" +
                "\n" +
                "    <p>Du finner en samlet oversikt over dine rettigheter i Datatilsynets veileder <a href=\"https://www.datatilsynet.no/regelverk-og-skjema/veiledere/de-registrertes-rettigheter-etter-nytt-regelverk/\" class=\"lenke\" target=\"_blank\" rel=\"noreferrer noopener\">De registrertes rettigheter etter nytt regelverk</a>. Kommunen din vil også ha informasjon om behandling av personopplysninger på sine nettsider.</p>\n" +
                "    <p>Alle spørsmål du har om behandling av personopplysningene dine må du rette til NAV Horten.</p>\n" +
                "\n" +
                "    <h4>Klagerett til Datatilsynet</h4>\n" +
                "    <p>Du har rett til å klage til Datatilsynet hvis du ikke er fornøyd med hvordan vi behandler personopplysninger om deg, eller hvis du mener behandlingen er i strid med personvernreglene. Informasjon om hvordan du går frem finner du på nettsidene til <a href=\"https://www.datatilsynet.no/\" class=\"lenke\" target=\"_blank\" rel=\"noreferrer noopener\">Datatilsynet</a>.</p>\n" +
                "    <p><a href=\"https://www.nav.no/no/NAV+og+samfunn/Om+NAV/personvern-i-arbeids-og-velferdsetaten/Personvern+og+sikkerhet+p%C3%A5+nav.no\" class=\"lenke\" target=\"_blank\" rel=\"noreferrer noopener\">Personvern og sikkerhet på nav.no</a></p>\n" +
                "<strong>Søknaden din blir sendt til NAV Horten.</strong><br/>Dette kontoret har ansvar for å behandle søknaden din, og NAV Horten lagrer opplysningene fra søknaden i fagsystemet sitt.";
        assertEquals(expected, resultat);

    }


}
