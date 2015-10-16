# Handlebars-helpers
> Denne markdown-filen genereres automatisk og endringer her vil overskrives.
> Oppdater Handlebars-helpers.hbs dersom du ønsker permanente endringer

For å generere oppsummeringerdokumenter som html, pdf eller xml har vi over tid bygget og registert en rekke 
helpers til Handlebars som gjør det enklere å jobbe med innholdet i søknaden.
Denne fila innehoder info dersom man vil lage en ny helper til Handlebars eller lurer på 
hvilke helpers som er registert i dag.

#### Ny versjon av dokumentasjon i Handlebars-helpers.md

I testfila `RegistryAwareHelperTest` finnes det en testmetode `skrivRegisterteHelpersTilReadme` 
som kjører genererer denne fila på nytt og tar med eventuelle nye helpers. Denne kan kjøres individuelt, 
men kjører også automatisk sammen med det vanlige testoppsettet.

#### Ny helper

* Opprette en spring-annotert klasse `HelpernavnHelper.java` i pakken `no.nav.sbl.dialogarena.service.helpers`
* La klassen arve fra `RegistryAwareHelper<T>`
* Implementer de abstrakte metodene
    * Navnet på helperen må returneres fra `getNavn`
    * Beskrivelsen fra `getBeskrivelse` vil havne i denne fila som dokumentasjon
* Lag et eksempel på bruk under `/readme/Helpernavn.hbs`, denne vil også bli inkludert i dokumentasjonen under.

På dette formatet er det superklassen `RegistryAwareHelper` som vil registere helperen som er opprettet på
Handlebars-instansen som brukes for å generere oppsummeringsdokumenter.

## Eksisterende helpers

Tidlgere ble helpers laget som metoder rett i `HandleBarKjoerer.java` hvor de ble
registert inn eksplisitt via `handlebars.registerHelper("helpernavn", helpermetode())`,.

#### Statisk liste over helpers på gammelt registeringsformat
 
* adresse
* forFaktum
* forFaktumHvisSant
* forFakta
* forBarnefakta
* forFaktaMedPropertySattTilTrue
* formatterFodelsDato
* formatterLangDato
* hvisSant
* hvisEttersending
* hvisMindre
* hvisMer
* hvisLik
* hvisIkkeTom
* hentTekst
* hentTekstMedParameter
* hentTekstMedFaktumParameter
* hentLand
* forVedlegg
* forPerioder
* hentSkjemanummer
* hentFaktumValue
* hvisFlereErTrue
* sendtInnInfo
* forInnsendteVedlegg
* forIkkeInnsendteVedlegg
* hvisHarIkkeInnsendteDokumenter
* concat
* skalViseRotasjonTurnusSporsmaal
* hvisLikCmsTekst
* toLowerCase
* hvisKunStudent
* harBarnetInntekt

#### Helpers på nytt registeringsformat

* hvisHarDiskresjonskode - Viser innhold avhengig av om personalia indikerer diskresjonskode 6 (fortrolig) eller 7 (strengt fortrolig)
* variabel - Lager en variabel med en bestemt verdi som kun er tilgjengelig innenfor helperen


#### Eksempler

##### hvisHarDiskresjonskode

```
{{#hvisHarDiskresjonskode}}
    Jeg har diskresjonskode
    {{else}}
    jeg har IKKE noen diskresjonskode
{{/hvisHarDiskresjonskode}}
```


##### variabel

```
{{#variabel "minvariabel" "verdi1"}}
    Forventer verdi1: {{minvariabel}}
{{/variabel}}
```

