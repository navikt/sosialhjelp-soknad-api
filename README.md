## Sendsoknad

### Kjøring lokalt

Backenden kan startes ved å kjøre main-metoden i DevSoknadsosialhjelpServer. Den kjører på port 8181. I utgangspunktet kjører backenden lokalt mot en in memory-database, men hvis du ønsker å kjøre mot en faktisk database kan du lage en kopi av filen `oracledb.properties.default`, fjerne .default-endelsen og fylle inn verdiene for databasen du vil teste mot (`oracledb.properties` er ignorert i gitignore og vil ikke bli sjekket inn). 

Husk å kjøre mvn clean install (evt process-resources) før du kjører lokalt slik at du får med tekster. For å teste søknaden lokalt trenger du også å ha frontenden kjørende. 

For å se json-representasjon av søknaden: 
`http://localhost:8181/sosialhjelp/soknad-api/representasjon/json/110000001`

 ### Deploy til testmiljø på Heroku

 Forutsetter at [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli) er installert og at man har opprettet
 og autentisert mot egen Heroku-konto:

 ```bash
 heroku auth:login
 heroku container:login
```

 For å opprette applikasjon og deploye til Heroku:

 ```bash
 heroku create en-kul-ny-feature-server
 ./heroku-build.sh
 ```
  
 Hvis applikasjonen allerede eksisterer i Heroku, kan app name angis ved deploy:
 
 ```bash
 ./heroku-build.sh --app-name=en-kul-ny-feature-server
 ``` 
 
 Eventuelt kan applikasjonen settes som en git remote:
 
 ```bash
 git remote add heroku https://git.heroku.com/en-kul-ny-feature-server.git
 ```
 
 Etter deploy vil backenden være tilgjengelig på `https://www.digisos-test.com/en-kul-ny-feature/sosialhjelp/soknad-api/`.
 (Altså **ikke** `https://www.digisos-test.com/en-kul-ny-feature-server/sosialhjelp/soknad-api`)

### Tekster

Sjekk ut det aktuelle tekstprosjektet og se README der. 

### Samarbeid med frontend (fjerning av faktummodellen)

Backenden kommer til å lagre hele søknaden som en json-fil (internalsoknad), og ved endringer som blir sendt fra frontend underveis i utfyllingen vil hele filen lagres. Vi må regne med at det kan skje at man får konflikt ved oppdatering av søknadsdataene mens bruker fyller ut søknaden. Ved en konflikt vil backenden sende exception'et SamtidigOppdateringException til frontenden. Frontenden må da forsøke å oppdatere på nytt (det er naturlig at frontenden gjør dette siden det er den som har full oversikt over hva som er fylt ut i søknaden, og siden det er den som vil endre data). 

Ved innsending vil søknadsdata låses ned slik at man ikke kan endre dem mer (dette er for å unngå at det gjøres endringer etter at brukeren har trykket på send).

### Bygging

Applikasjonen bruker Oracle JDBC-driver og enkelte interne avhengigheter som ikke er tilgjengelige fra byggserver. Den benytter seg derfor av
et bygg-image som inneholder alle avhengighetene, og kan hentes fra GitHub package registry i bygg-pipeline etter Docker login:

```
FROM maven:3.6.2-jdk-11

WORKDIR /root

COPY m2_home.tar.gz .
RUN tar xvzf m2_home.tar.gz
RUN rm m2_home.tar.gz
```

For å bygge imaget må `m2_home.tar.gz` lastes ned fra
[Microsoft Teams](https://navno.sharepoint.com/sites/Digisos532/Shared%20Documents/Utviklingteamet/backend%20github%20relatert/m2_home.tar.gz)
til en lokal folder med en `Dockerfile` med innholdet over, og bygges og pushes med kommandoene:

```
docker build -t docker.pkg.github.com/navikt/sosialhjelp-soknad-api/builder:0.2-jdk-11 .
docker push docker.pkg.github.com/navikt/sosialhjelp-soknad-api/builder:0.2-jdk-11
```

Dette krever at man er logget inn mot `docker.pkg.github.com` lokalt med et GitHub personal access token som har scopet `write:packages` og
SSO aktivert for `navikt`-organisasjonen.
