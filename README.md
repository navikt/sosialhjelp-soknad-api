## Sendsoknad

### Sync av tekster til Enonic test

Dersom man ønsker å ta i bruk `modig-content:sync` er denne beskrevet i `pom.xml`. 
Et alternativ er å legge følgende funksjon under `C:/Users/<ident>/.bash_profile` eller der du har din source-fil til bash, 
og kjøre den i git bash med kommandoen `enonicsync`. 
Denne versjonen er stilt inn på T8, og for å skjekke urlen kan du se under [Enonic miljøer](http://confluence.adeo.no/pages/viewpage.action?pageId=70209947). 
Tidligere har det kun vært admin på FSS som er åpen for modig-content-sync.

    function enonicsync() {
    	env='10.51.9.185'
    	echo '****************************'
    	echo 'Syncer tekster med Enonic T8'
    	echo 'Enviroment: '$env
    	echo '****************************'
    	read -p "Username:" username
    	read -s -p "Password:" password 
    	mvn modig-content:sync -Denonic.username=$username -Denonic.password=$password -Denonic.url=http://$env:8080
    }



Dette kan også løses med en kombinasjon av alias og funksjoner for å forenkle prosessen, spesielt om man ikke ønsker å måtte skrive inn passordet hver gang. Husk å endre til riktig brukernavn og passord i funksjonen under.

    function syncEnonic() {
	    mvn modig-content:sync -Denonic.username={{BRUKERNAVN}} -Denonic.password={{PASSORD}} -Denonic.url=http://$1:8080
    }

    alias esync='syncEnonic'
    alias esynct8='esync 10.51.9.185'

### Flytt tekster ut fra enonic
* Endre dialogen sin kravdialogInformasjon implementasjon slik at brukerEnonic returnerer false
* Lag et nytt git-repo hvor tekstene skal ligge - se soknadforeldrepenger-tekster for eksempel. Les gjerne readme for fremgangsmåte
* I app-config legges til fileLibrary som refererer til hvor det ble satt i tekster-appen at tekstene skal legges på disk
* Slett tekster-property-filen tilhørende dialogen og menypunkt-path i hovedpom'en
* Legg til ny property i environment-test.properties-fil som peker til propertiesfilene som blir bygget i tekster-appen (blir brukt i contentConfig for å finne tekstbundlene, se folder.foreldrepenger.path for eks)
* Hvis det er den siste teksten, vurder og bytte ut NavMessageSource med utave-biblioteket


