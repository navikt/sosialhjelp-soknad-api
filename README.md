[![Build image](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/build.yml)
[![Deploy til prod](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/deploy_prod.yml/badge.svg)](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/deploy_prod.yml)

# Sosialhjelp-soknad-api
Backend-applikasjon for søknad om økonomisk sosialhjelp.

## Henvendelser
Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

## Teknologi:
* Java/Kotlin
* JDK 17
* Gradle
* Spring Boot
* Oracle DB
* Redis (cache)

### Krav
* JDK 17

### Kjøring lokalt mot mock-alt
Start `Application.kt` med profilene `mock-alt,no-redis,log-console`.\
Krever at sosialhjelp-mock-alt-api også kjører lokalt.

### Tekster
Tekstfiler finnes her: `src/main/resources`.

### Samarbeid med frontend (fjerning av faktummodellen)
Backenden kommer til å lagre hele søknaden som en json-fil (internalsoknad), og ved endringer som blir sendt fra frontend underveis i utfyllingen vil hele filen lagres. Vi må regne med at det kan skje at man får konflikt ved oppdatering av søknadsdataene mens bruker fyller ut søknaden. Ved en konflikt vil backenden sende exception'et SamtidigOppdateringException til frontenden. Frontenden må da forsøke å oppdatere på nytt (det er naturlig at frontenden gjør dette siden det er den som har full oversikt over hva som er fylt ut i søknaden, og siden det er den som vil endre data). 

Ved innsending vil søknadsdata låses ned slik at man ikke kan endre dem mer (dette er for å unngå at det gjøres endringer etter at brukeren har trykket på send).

### Autentisering 
Alle endepunkt er autentisering `Azure AD B2C` cookie validert via `token-support`. 

I tillegg krever noen endepunkter et `access-token` fra `idporten`, som brukeren får via `sosialhjelp-login-api`. Dette brukes mot `FIKS` og mot `Husbanken`.


## Hvordan komme i gang
Se [Felles dokumentasjon for våre backend apper](https://github.com/navikt/digisos/blob/main/digisosdokumentasjon/docs/utviklerdokumentasjon/kom%20igang%20med%20utvikling.md#backend-gradle) for generell dokumentasjon av våre backend-apper.

