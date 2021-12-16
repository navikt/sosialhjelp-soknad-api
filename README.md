![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Build%20image/badge.svg?branch=master)
![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Deploy%20Dev/badge.svg?)
![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Deploy%20Prod/badge.svg?)
![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Deploy%20GCP/badge.svg?)

## Sosialhjelp-soknad-api

### Kjøring lokalt mot mock-alt
Bruk MockAltSoknadsosialhjelpServer istedenfor DevSoknadsosialhjelpServer.\
Krever at sosialhjelp-mock-alt-api også kjører lokalt.

### Kjøring lokalt
Backenden kan startes ved å kjøre main-metoden i DevSoknadsosialhjelpServer. Den kjører på port 8181. I utgangspunktet kjører backenden lokalt mot en in memory-database, men hvis du ønsker å kjøre mot en faktisk database kan du lage en kopi av filen `oracledb.properties.default`, fjerne .default-endelsen og fylle inn verdiene for databasen du vil teste mot (`oracledb.properties` er ignorert i gitignore og vil ikke bli sjekket inn). 

Husk å kjøre mvn clean install (evt process-resources) før du kjører lokalt slik at du får med tekster. For å teste søknaden lokalt trenger du også å ha frontenden kjørende. 

For å se json-representasjon av søknaden: 
`http://localhost:8181/sosialhjelp/soknad-api/representasjon/json/110000001`

### Tekster
Tekstfiler finnes her: `src/main/resources-filtered`.

### Samarbeid med frontend (fjerning av faktummodellen)
Backenden kommer til å lagre hele søknaden som en json-fil (internalsoknad), og ved endringer som blir sendt fra frontend underveis i utfyllingen vil hele filen lagres. Vi må regne med at det kan skje at man får konflikt ved oppdatering av søknadsdataene mens bruker fyller ut søknaden. Ved en konflikt vil backenden sende exception'et SamtidigOppdateringException til frontenden. Frontenden må da forsøke å oppdatere på nytt (det er naturlig at frontenden gjør dette siden det er den som har full oversikt over hva som er fylt ut i søknaden, og siden det er den som vil endre data). 

Ved innsending vil søknadsdata låses ned slik at man ikke kan endre dem mer (dette er for å unngå at det gjøres endringer etter at brukeren har trykket på send).

### Autentisering 
Alle endepunkt er autentisering `Azure AD B2C` cookie validert via `token-support`. 
Dette fordi `Saksoversikt-api` fortsatt er på SAML. 
I tillegg krever noen endepunkter et `access-token` fra `idporten`, som brukeren får via `sosialhjelp-login-api`. Dette brukes mot `FIKS` og mot `Husbanken`.

### Bruk av pakker fra Github Package Registry
For å kunne konsumere pakker fra Github Package Registry kjøres `mvn install --settings maven-settings.xml`, med `GITHUB_TOKEN: ${{ secrets.GITHUB_ACCESS_TOKEN }}` som env.variabel som injectes til `maven-settings.xml`.

Mer info: https://github.com/navikt/utvikling/blob/master/Konsumere%20biblioteker%20fra%20Github%20Package%20Registry.md

### Ktlint
Her brukes `maven-antrun-plugin` for sjekking og formattering av kotlin-kode - ref https://github.com/pinterest/ktlint#integration

To check code style - `mvn antrun:run@ktlint` (it's also bound to `mvn validate`).

To run formatter - `mvn antrun:run@ktlint-format`.