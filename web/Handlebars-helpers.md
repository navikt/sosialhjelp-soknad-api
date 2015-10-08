# Handlebars-helpers
> Denne Markdown-filen genereres automatisk og endringer her vil overskrives.
> Oppdater Handlebars-helpers.hbs dersom du ønsker permanente endringer

For å generere oppsummeringerdokumenter som HTML, PDF eller XML har vi over tid bygget og registert en rekke
helpers til Handlebars som gjør det enklere å jobbe med innholdet i søknaden.
Denne fila innehoder info dersom man vil lage en ny helper til Handlebars eller lurer på 
hvilke helpers som er registert i dag.

#### Ny versjon av dokumentasjon i Handlebars-helpers.md

I testfila `RegistryAwareHelperTest` finnes det en testmetode `skrivRegisterteHelpersTilReadme` 
som kjører genererer denne fila på nytt og tar med eventuelle nye helpers. Denne kan kjøres individuelt, 
men kjører også automatisk sammen med det vanlige testoppsettet.

#### Ny helper

* Opprette en Spring-annotert klasse (`@Component`) `HelpernavnHelper.java` i pakken `no.nav.sbl.dialogarena.service.helpers`
* La klassen arve fra `RegistryAwareHelper<T>`
* Implementer de abstrakte metodene
    * Navnet på helperen må returneres fra `getNavn`
    * Beskrivelsen fra `getBeskrivelse` vil havne i denne fila som dokumentasjon
* Lag et eksempel på bruk under `/readme/helpernavn.hbs`. Denne vil også bli inkludert i dokumentasjonen under.
* Legg til tester

På dette formatet er det superklassen `RegistryAwareHelper` som vil registere helperen som er opprettet på
Handlebars-instansen som brukes for å generere oppsummeringsdokumenter.

## Eksisterende helpers

Tidlgere ble helpers laget som metoder rett i `HandleBarKjoerer.java` hvor de ble
registert inn eksplisitt via `handlebars.registerHelper("helpernavn", helpermetode())`,.

#### Statisk liste over helpers på gammelt registeringsformat
 
* adresse
* forFaktumHvisSant
* forBarnefakta
* formatterFodelsDato (deprecated og erstattet av formatterKortDato og formatterFnrTilKortDato)
* formatterLangDato
* hentLand
* forPerioder
* hvisFlereErTrue
* sendtInnInfo
* skalViseRotasjonTurnusSporsmaal

#### Helpers på nytt registreringsformat

* concat - Legger sammen alle parametrene til tekststring
* fnrTilKortDato - Formatterer et gyldig fødselnummer til dato på formatet dd.mm.aaaa
* forFakta - Finner alle fakta med en gitt key og setter hvert faktum som aktiv context etter tur. Har inverse ved ingen fakta.
* forFaktaMedPropertySattTilTrue - Finner alle fakta med gitt key som har gitt property satt til true
* forFaktum - Finner et faktum og setter det som aktiv context. Har også inverse om faktum ikke finnes. 
* forFaktumMedId - Returnerer et faktum med den gitte ID-en
* forFaktumTilknyttetBarn - Returnerer faktumet tilknyttet barnet i parent-context.
* forIkkeInnsendteVedlegg - Itererer over vedlegg som ikke er sendt inn
* forInnsendteVedlegg - Itererer over innsendte vedlegg på søknaden
* forVedlegg - Lar en iterere over alle påkrevde vedlegg på en søknad
* formaterDato - Formaterer en innsendt dato på et gitt format som også sendes inn
* harBarnetInntekt - Henter summen hvis barnet har inntekt. Må brukes innenfor en #forFaktum eller #forFakta helper. 
* hentFaktumValue - Returnerer verdien til et faktum tilhørende keyen som sendes inn
* hentSkjemanummer - Setter inn søknadens skjemanummer, også om det er en søknad for dagpenger
* hentTekst - Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.
* hentTekstMedFaktumParameter - Henter tekst fra cms for en gitt key, med verdien til et faktum som parameter. Faktumet hentes basert på key
* hvisHarDiskresjonskode - Viser innhold avhengig av om personalia indikerer diskresjonskode 6 (fortrolig) eller 7 (strengt fortrolig)
* hvisHarIkkeInnsendteDokumenter - Sjekker om søknaden har ikke-innsendte vedlegg
* hvisIkkeTom - Dersom variabelen ikke er tom vil innholdet vises
* hvisKunStudent - Sjekker om brukeren har en annen status enn student (f.eks sykmeldt, i arbeid osv.)
* hvisLik - Sjekker om to strenger er like
* hvisMer - Evaluerer en string til double og sjekker om verdien er mer enn grenseverdien gitt ved andre inputparameter
* hvisMindre - Evaluerer en string til integer og sjekker om verdien er mindre enn andre inputparameter
* hvisSant - Dersom variabelen er "true" vil innholdet vises
* kortDato - Formatterer en datostreng på formatet yyyy-mm-dd til dd.mm.aaaa
* lagKjorelisteUker - Bygger en nestet liste over uker for et betalingsvedtak, der ukene inneholder dager det er søkt for refusjon.
* sendtInnInfo - Tilgjengeliggjør informasjon om søknaden (innsendte vedlegg, påkrevde vedlegg og dato)
* toCapitalized - Gjør om en tekst til at alle ord starter med store bokstaver
* toLowerCase - Gjør om en tekst til kun små bokstaver
* variabel - Lager en variabel med en bestemt verdi som kun er tilgjengelig innenfor helperen


#### Eksempler

##### concat

```
{{ concat "a" "b" "c" "d" }}
```


##### fnrTilKortDato

```
{{fnrTilKortDato "27108034322"}}
```


##### forFakta

```
{{#forFakta "faktumKey"}}
   Faktum {{index}} med key "faktumKey" har value {{value}}
{{else}}
   Faktalisten er tom, det finnes ingen faktum med key "faktumKey".
{{/forFakta}}
```


##### forFaktaMedPropertySattTilTrue

```
{{#forFaktaMedPropertySattTilTrue "faktumnavn" "propertyKey"}}
    Faktumet "faktumnavn" har har propertien "propertyKey" og den satt til true.
{{else}}
    Faktumet har ikke property satt til true (enten false eller ikke noe).
{{/forFaktaMedPropertySattTilTrue}}
```


##### forFaktum

```
{{#forFaktum "faktumNavn"}}
    Faktum med key {{key}} finnes og kan aksesseres. {{value}} skriver f.eks ut verdien på faktumet. se Faktum klassen.
{{else}}
    faktum med key "faktumNavn" er ikke satt
{{/forFaktum}}
```


##### forFaktumMedId

```
{{#forFaktumMedId "faktumId"}}
    Faktum med id {{faktumId}} finnes og kan aksesseres. {{value}} skriver f.eks. ut verdien på faktumet. Se Faktum-klassen.
{{else}}
    Faktum med id "faktumId" finnes ikke.
{{/forFaktumMedId}}
```


##### forFaktumTilknyttetBarn

```
{{#forFaktum "barn"}}
    {{#forFaktumTilknyttetBarn "faktumNavn"}}
        Her har du faktumet (med gitt key) som er tilknyttet barnet gitt i parent-context (forFaktum).
        Se forFaktum for å vite med om hvordan faktumobjektet fungerer.
    {{else}}
        Om det ikke finnet noe faktum som er tilknyttet barnet vil den gå inn i else-contexten.
    {{/forFaktumTilknyttetBarn}}
{{/forFaktum}}
```


##### forIkkeInnsendteVedlegg

```
{{#forIkkeInnsendteVedlegg}}
    ikke innsendt: {{navn}}
{{else}}
    Ingen ikke-innsendte vedlegg
{{/forIkkeInnsendteVedlegg}}
```


##### forInnsendteVedlegg

```
{{#forInnsendteVedlegg}}
    innsendt: {{navn}}
{{else}}
    Ingen innsendte vedlegg
{{/forInnsendteVedlegg}}
```


##### forVedlegg

```
{{#forVedlegg}}
    vedlegg: {{navn}}

    {{#hvisLik innsendingsvalg "LastetOpp"}}
        lastet opp
    {{ else }}
        ikke lastet opp
    {{/hvisLik}}
    + andre verdier som ligger på vedleggene
{{else}}
    Ingen vedlegg
{{/forVedlegg}}
```


##### formaterDato

```
{{ukedag "2015-09-16" "EEEE"}}
{{ukedag variabel "d. MMMM YYYY"}}
```


##### harBarnetInntekt

```
{{#forFaktum "faktumNavn"}}
    {{#harBarnetInntekt}
        Gitt at wrapper-faktumet "faktumNavn" har to barnefaktum: "barnet.harinntekt" hvor verdi er "true", og
        "barnet.inntekt". Sistnevnte er tilgjengelig her slik at {{value}} skriver ut inntekten.
    {{else}}
        Barnet har ikke inntekt.
    {{/harBarnetInntekt}}
{{/forFaktum}}

```


##### hentFaktumValue

```
{{hentFaktumValue "faktum.key"}}
```


##### hentSkjemanummer

```
{{hentSkjemanummer}}
```


##### hentTekst

```
{{hentTekst "min.key" "param1" "param2"}}
{{hentTekst "min.key.uten.params"}}
```


##### hentTekstMedFaktumParameter

```
{{hentTekstMedFaktumParameter "cms.key" "faktum.key"}}
```


##### hvisHarDiskresjonskode

```
{{#hvisHarDiskresjonskode}}
    Jeg har diskresjonskode
    {{else}}
    jeg har IKKE noen diskresjonskode
{{/hvisHarDiskresjonskode}}
```


##### hvisHarIkkeInnsendteDokumenter

```
{{#hvisHarIkkeInnsendteDokumenter}}
    har ikke-innsendte dokumenter
{{else}}
    alt er innsendt
{{/hvisHarIkkeInnsendteDokumenter}}
```


##### hvisIkkeTom

```
{{#hvisIkkeTom "verdi"}}
    Verdien er ikke tom
{{else}}
    Verdien er tom
{{/hvisIkkeTom}}
```


##### hvisKunStudent

```
{{#hvisKunStudent}}
    Bare student
    {{else}}
        Ikke bare student
{{/hvisKunStudent}}
```


##### hvisLik

```
{{#hvisLik "verdi 1" "verdi 2"}}
    Verdiene er like
    {{else}}
    Verdiene er ikke like
{{/hvisLik}}
```


##### hvisMer

```
{{#hvisMer verdi "1"}}
    Verdi er mer enn 1
    {{else}}
    Verdi er lik eller mindre enn 1
{{/hvisMer}}
```


##### hvisMindre

```
{{#hvisMindre verdi "50"}}
    Verdi er mindre enn 50
    {{else}}
    Verdi er lik eller større enn 50
{{/hvisMindre}}
```


##### hvisSant

```
{{#hvisSant booleanString}}
    Gitt "true"
    {{else}}
    Gitt alt annet enn "true"
{{/hvisSant}}
```


##### kortDato

```
{{kortDato "2015-11-03"}}
```


##### lagKjorelisteUker

```
{{#lagKjorelisteUker properties}}
    uke: {{ukeNr}}
    {{#each dager}}
        {{dato}}: {{parkering}}
    {{/each}}
{{/lagKjorelisteUker}}
```


##### sendtInnInfo

```
{{#sendtInnInfo}}
    innsendte: {{sendtInn}}
    påkrevde: {{ikkeSendtInn}}
    innsendt dato: {{innsendtDato}}
{{/sendtInnInfo}}
```


##### toCapitalized

```
{{toCapitalized variabel}}
{{toCapitalized "mASSe caSe"}}
```


##### toLowerCase

```
{{toLowerCase variabel}}
{{toLowerCase "MaSSe Case"}}
```


##### variabel

```
{{#variabel "minvariabel" "verdi1"}}
    Forventer verdi1: {{minvariabel}}
{{/variabel}}
```

