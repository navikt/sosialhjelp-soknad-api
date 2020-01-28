![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Build/badge.svg?branch=master)
![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Deploy%20Dev/badge.svg?)
![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Deploy%20Prod/badge.svg?)
![](https://github.com/navikt/sosialhjelp-soknad-api/workflows/Deploy%20GCP/badge.svg?)

## Sosialhjelp-soknad-api

### Kjøring lokalt med mock
Bruk MockSoknadsosialhjelpServer istedenfor DevSoknadsosialhjelpServer.

### Kjøring lokalt

Backenden kan startes ved å kjøre main-metoden i DevSoknadsosialhjelpServer. Den kjører på port 8181. I utgangspunktet kjører backenden lokalt mot en in memory-database, men hvis du ønsker å kjøre mot en faktisk database kan du lage en kopi av filen `oracledb.properties.default`, fjerne .default-endelsen og fylle inn verdiene for databasen du vil teste mot (`oracledb.properties` er ignorert i gitignore og vil ikke bli sjekket inn). 

Husk å kjøre mvn clean install (evt process-resources) før du kjører lokalt slik at du får med tekster. For å teste søknaden lokalt trenger du også å ha frontenden kjørende. 

For å se json-representasjon av søknaden: 
`http://localhost:8181/sosialhjelp/soknad-api/representasjon/json/110000001`

### Tekster

Sjekk ut det aktuelle tekstprosjektet og se README der. 

### Samarbeid med frontend (fjerning av faktummodellen)

Backenden kommer til å lagre hele søknaden som en json-fil (internalsoknad), og ved endringer som blir sendt fra frontend underveis i utfyllingen vil hele filen lagres. Vi må regne med at det kan skje at man får konflikt ved oppdatering av søknadsdataene mens bruker fyller ut søknaden. Ved en konflikt vil backenden sende exception'et SamtidigOppdateringException til frontenden. Frontenden må da forsøke å oppdatere på nytt (det er naturlig at frontenden gjør dette siden det er den som har full oversikt over hva som er fylt ut i søknaden, og siden det er den som vil endre data). 

Ved innsending vil søknadsdata låses ned slik at man ikke kan endre dem mer (dette er for å unngå at det gjøres endringer etter at brukeren har trykket på send). 
