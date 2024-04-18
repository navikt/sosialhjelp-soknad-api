package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.soknad.pdf.Utils.addLinks

object JuridiskInformasjon {
    fun leggTilJuridiskInformasjon(
        pdf: PdfGenerator,
        soknad: JsonSoknad,
        utvidetSoknad: Boolean,
    ) {
        if (utvidetSoknad && soknad.mottaker != null && soknad.mottaker.navEnhetsnavn != null) {
            val navkontor = soknad.mottaker.navEnhetsnavn
            pdf.skrivH4Bold("Oppsummering")
            pdf.skrivTekst(
                "Søknaden din blir sendt til $navkontor. Dette kontoret har ansvar for å behandle søknaden din, og $navkontor lagrer opplysningene fra søknaden.",
            )
            pdf.addBlankLine()

            pdf.skrivTekstBold("Informasjon")
            pdf.skrivTekst(
                "Når du søker om økonomisk sosialhjelp digitalt, må du gi opplysninger om deg selv slik at NAV-kontoret ditt kan behandle søknaden. Eksempler på opplysninger er din adresse, familieforhold, inntekter og utgifter.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst("NAV vil også hente opplysninger fra andre registre på vegne av kommunen din som skal behandle søknaden:")
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Personopplysninger fra Folkeregisteret, opplysninger om arbeidsforhold fra Arbeidstakerregisteret, opplysninger om skattbare inntekter fra Skatteetaten, opplysninger om bostøtte fra Husbanken og informasjon om statlige ytelser fra NAV.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst("Du kan være trygg på at personopplysningene dine blir behandlet på en sikker og riktig måte:")
            pdf.skrivTekstMedInnrykk(
                "* Vi skal ikke innhente flere opplysninger enn det som er nødvendig.",
                PdfGenerator.INNRYKK_2,
            )
            pdf.skrivTekstMedInnrykk(
                "* NAV har taushetsplikt om alle opplysninger som vi behandler. Hvis offentlige virksomheter eller andre ønsker å få utlevert opplysninger om deg, må de ha hjemmel i lov eller du må gi samtykke til det.",
                PdfGenerator.INNRYKK_2,
            )
            pdf.addBlankLine()

            pdf.skrivTekstBold("Behandlingsansvarlig")
            pdf.skrivTekst("Det er $navkontor som er ansvarlig for å behandle søknaden og personopplysningene dine.")
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Henvend deg til kommunen hvis du har spørsmål om personopplysninger. Kommunen har også et personvernombud som du kan kontakte.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Arbeids- og velferdsdirektoratet har ansvaret for nav.no og behandler den digitale søknaden som en databehandler på vegne av kommunen.",
            )
            pdf.addBlankLine()

            pdf.skrivTekstBold("Formålet med å samle inn og bruke personopplysninger")
            pdf.skrivTekst(
                "Formålet med søknaden er å samle inn tilstrekkelig opplysninger til at kommunen din kan behandle søknaden om økonomisk sosialhjelp. Opplysninger du gir i den digitale søknaden og opplysninger som blir hentet inn, sendes digitalt fra nav.no til NAV-kontoret ditt. Det blir enklere for deg å søke, og NAV-kontoret ditt mottar søknaden ferdig utfylt med nødvendige vedlegg.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Opplysningene i søknaden vil bli brukt til å vurdere om du fyller vilkårene for økonomisk sosialhjelp, og skal ikke lagres lenger enn det som er nødvendig ut fra formålet. Hvis ikke opplysningene skal oppbevares etter arkivloven eller andre lover, skal de slettes etter bruk.",
            )
            pdf.addBlankLine()

            pdf.skrivTekstBold("Lovgrunnlaget")
            pdf.skrivTekst(
                "Lovgrunnlaget for å samle inn informasjon i forbindelse med søknaden din er lov om sosiale tjenester i Arbeids- og velferdsforvaltningen.",
            )
            pdf.addBlankLine()

            pdf.skrivTekstBold("Innhenting av personopplysningene dine")
            pdf.skrivTekst(
                "Du gir selv flere opplysninger når du søker om økonomisk sosialhjelp. I tillegg henter vi opplysninger som NAV har i sine registre, som for eksempel opplysninger om andre ytelser du har fra NAV. Vi henter også opplysninger fra andre offentlige registre som vi har lov til å hente informasjon fra, for eksempel opplysninger om arbeidsforhold fra Arbeidsgiver- og arbeidstakerregisteret.",
            )
            pdf.addBlankLine()

            pdf.skrivTekstBold("Lagring av personopplysningene dine")
            pdf.skrivTekst("Før du sender søknaden lagres opplysningene på nav.no")
            pdf.skrivTekstMedInnrykk(
                "Søknader som er påbegynt, men ikke fullført, blir lagret hos Arbeids- og velferdsdirektoratet i to uker. Deretter slettes de.",
                PdfGenerator.INNRYKK_2,
            )
            pdf.addBlankLine()
            pdf.skrivTekst("Etter du har sendt søknaden har kommunen din ansvaret for opplysningene om deg")
            pdf.skrivTekstMedInnrykk(
                "Når du sender søknaden din bruker vi KS (Kommunesektorens organisasjon) sin skytjeneste for digital post (Svarut).  Kommunen henter søknaden din i Svarut og lagrer opplysningene i sitt kommunale fagsystem.  Kommunen din har ansvaret for lagring og sletting av opplysningene dine både i Svarut og i fagsystemet . Arkivloven bestemmer hvor lenge opplysninger skal lagres. Ta kontakt med kommunen din hvis du har spørsmål om lagringstid.",
                PdfGenerator.INNRYKK_2,
            )
            pdf.addBlankLine()

            pdf.skrivTekstBold("Rettigheter som registrert")
            pdf.skrivTekst("Alle har rett på informasjon om og innsyn i egne personopplysninger etter personopplysningsloven.")
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Hvis opplysninger om deg er feil, ufullstendige eller unødvendige, kan du kreve at opplysningene blir rettet eller supplert etter personopplysningsloven. Du kan også i særlige tilfeller be om å få dem slettet, hvis ikke kommunen har en lovpålagt plikt til å lagre opplysningene som dokumentasjon. Slike krav skal besvares kostnadsfritt og senest innen 30 dager.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Du har også flere personvernrettigheter, blant annet såkalt <strong>rett til begrensning</strong>: Du kan i visse tilfeller ha rett til å få en begrenset behandling av personopplysningene dine. Hvis du har en slik rett, vil opplysningene bli lagret, men ikke brukt.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Du har også <strong>rett til å protestere</strong> mot behandling av personopplysninger: Det vil si at du i enkelte tilfeller kan ha rett til å protestere mot kommunens ellers lovlige behandling av personopplysninger. Behandlingen må da stanses, og hvis du får medhold vil opplysningene eventuelt bli slettet.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Du finner en samlet oversikt over dine rettigheter i Datatilsynets veileder De registrertes rettigheter etter nytt regelverk. Kommunen din vil også ha informasjon om behandling av personopplysninger på sine nettsider.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst("Alle spørsmål du har om behandling av personopplysningene dine må du rette til $navkontor.")
            pdf.addBlankLine()

            pdf.skrivTekstBold("Klagerett til Datatilsynet")
            pdf.skrivTekst(
                "Du har rett til å klage til Datatilsynet hvis du ikke er fornøyd med hvordan vi behandler personopplysninger om deg, eller hvis du mener behandlingen er i strid med personvernreglene. Informasjon om hvordan du går frem finner du på nettsidene til Datatilsynet.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst("Personvern og sikkerhet på nav.no")
            pdf.addBlankLine()

            val urisOnPage: MutableMap<String, String> = HashMap()
            urisOnPage["De registrertes rettigheter etter nytt regelverk"] =
                "https://www.datatilsynet.no/regelverk-og-skjema/veiledere/de-registrertes-rettigheter-etter-nytt-regelverk/"
            urisOnPage["Personvern og sikkerhet på nav.no"] =
                "https://www.nav.no/no/nav-og-samfunn/om-nav/personvern-i-arbeids-og-velferdsetaten/personvern-og-sikkerhet-pa-nav.no"
            urisOnPage["Datatilsynet"] = "https://www.datatilsynet.no/"

            addLinks(pdf, urisOnPage)

            pdf.skrivTekstBold("Bekreftet av bruker med følgende informasjonstekst")
            pdf.skrivTekst(
                "Jeg er kjent med at hvis opplysningene jeg har gitt ikke er riktige og fullstendige kan jeg miste retten til stønad.",
            )
            pdf.addBlankLine()
            pdf.skrivTekst(
                "Jeg er også klar over at jeg kan få krav om å betale tilbake det jeg har fått feilaktig utbetalt, og at jeg kan bli anmeldt til politiet hvis jeg med vilje oppgir feil opplysninger.",
            )
            pdf.addBlankLine()
        }
    }
}
