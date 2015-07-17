## Handlebars-helpers
> Denne markdown-filen genereres automatisk og endringer her vil overskrives. 
> Oppdater Handlebars-helpers.hbs dersom du ønsker permanente endringer

For å generere oppsummeringerdokumenter som html, pdf eller xml har vi over tid bygget og registert en rekke 
helpers til Handlebars som gjør det enklere å jobbe med innholdet i søknaden.
Denne fila innehoder info dersom man vil lage en ny helper til Handlebars eller lurer på 
hvilke helpers som er registert i dag.

### Hvordan generere ny versjon av Handlebars-helpers.md

I testfila `RegistryAwareHelperTest` finnes det en testmetode `skrivRegisterteHelpersTilReadme` 
som kjører genererer denne fila på nytt og tar med eventuelle nye helpers. Denne kan kjøres individuelt, 
men kjører også automatisk sammen med det vanlige testoppsettet.

### Jeg vil lage en ny helper!

For å registere ny helpers må man opprette en spring-annoterte klasse `***Helper.java` 
i pakken `no.nav.sbl.dialogarena.service.helpers` og arve fra klassen `RegistryAwareHelper<T>`. 
Parent-klassen vil selv sørge for å registere alle helpers til handlebarskjøreren med helpernavn fra `getName`.

### Eksisterende helpers

Tidlgere ble helpers laget som metoder rett i `HandleBarKjoerer.java` hvor de ble
registert inn eksplisitt via `handlebars.registerHelper("helpernavn", helpermetode());`, 
men dette er gått bort i fra.  

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

#### Dynamisk liste over helpers på nytt registeringsformat

* hvisKode6Eller7 - Viser innhold avhengig av om personalia indikerer diskresjonskode 6 (fortrolig) eller 7 (strengt fortrolig)
* variabel - Lager en variabel med en bestemt verdi som kun er tilgjengelig innenfor helperen