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



