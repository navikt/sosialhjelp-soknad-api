[![Build image](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/build.yml)
[![Deploy til prod](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/deploy_prod.yml/badge.svg)](https://github.com/navikt/sosialhjelp-soknad-api/actions/workflows/deploy_prod.yml)

# Sosialhjelp-soknad-api

Backend-applikasjon for søknad om økonomisk sosialhjelp.

## Henvendelser

Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For Nav-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

## Teknologi:

* Java/Kotlin
* JDK 21
* Gradle
* Spring Boot
* PostgreSQL db
* Valkey (cache)

### Krav

* Java (JDK) 21

### Kjøring lokalt mot mock-alt

Start `Application.kt` med profilene `mock-alt,local,no-redis,log-console`.
Kjør opp nødvendige bakgrunnsapper fra repoet digisos-docker-compose.

For å autentisere seg mot GAR som beskrevet under, skal det være nok å kjøre en `nais login`.
Hvis ikke, følg instruksjonen under.

Vi har endret fra Github Container Registry til Google Artifact Registry for pakker (images). For å autentisere mot
GAR må man først ha installert `gcloud cli` og autentisere seg med `gcloud auth login`. <br>
Videre gjør man følgende for å autentisere Docker Daemon mot GAR: <br>
`gcloud auth configure-docker europe-north1-docker.pkg.dev` <br>
`gcloud auth print-access-token | docker login -u oauth2accesstoken
--password-stdin https://europe-north1-docker.pkg.dev`

Dersom du får `Error: Cannot perform an interactive login from a non TTY device` i kommandoen over, kan du prøve å kjøre denne kommandoen i stedet:
`ACCESS_TOKEN=$(gcloud auth print-access-token) echo $ACCESS_TOKEN | docker login -u oauth2accesstoken --password-stdin https://europe-north1-docker.pkg.dev`

**OBS!** Pga. hvordan MiljoUtils er implementert må profilene også defineres i miljøvariabelen SPRING_PROFILES_ACTIVE.

### Tekster

Tekstfiler finnes her: `src/main/resources`.

### 28.01.2025 -> Ny datamodell
Midlertidig lagring av data skjer ikke lenger ved en stor json-fil, men ved respektive tabeller i databasen. Når søknaden er
ferdig utfylt av bruker vil json-strukturen genereres og sendes til KS FIKS.

### Samarbeid med frontend (fjerning av faktummodellen) (Gjelder ikke fra 28.01.2025 -> ny datamodell)

Backenden kommer til å lagre hele søknaden som en json-fil (internalsoknad), og ved endringer som blir sendt fra
frontend underveis i utfyllingen vil hele filen lagres. Vi må regne med at det kan skje at man får konflikt ved
oppdatering av søknadsdataene mens bruker fyller ut søknaden. Ved en konflikt vil backenden sende exception'et
SamtidigOppdateringException til frontenden. Frontenden må da forsøke å oppdatere på nytt (det er naturlig at frontenden
gjør dette siden det er den som har full oversikt over hva som er fylt ut i søknaden, og siden det er den som vil endre
data).

Ved innsending vil søknadsdata låses ned slik at man ikke kan endre dem mer (dette er for å unngå at det gjøres
endringer etter at brukeren har trykket på send).

### Autentisering

Alle endepunkt er autentisering `Azure AD B2C` cookie validert via `token-support`.

I tillegg krever noen endepunkter et `access-token` fra `idporten`, som brukeren får via `sosialhjelp-login-api`. Dette
brukes mot `FIKS` og mot `Husbanken`.

## Hvordan komme i gang

Se [Felles dokumentasjon for våre backend apper](https://github.com/navikt/digisos/blob/main/digisosdokumentasjon/docs/utviklerdokumentasjon/kom%20igang%20med%20utvikling.md#backend-gradle)
for generell dokumentasjon av våre backend-apper.
