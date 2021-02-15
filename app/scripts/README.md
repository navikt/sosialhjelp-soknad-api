## Generering av språkfiler

Scriptet ```generer.js``` leser en generert språkfil, og 
skriver en tekstfil for hver tekst i mappen ./src/main/tekster/.

Eksempel:

```
  > cd scripts
  > npm install
  > node generer.js ..\..\soknadsosialhjelp\web\src\frontend\scripts\mock_data\soknadsosialhjelp_nb_NO.properties
``` 

Scriptet gjør det omvendte av ```mvn clean install -Ddev```.

### Issues

* Scriptet detekterer ikke ubrukte tekstnøkler
* Håndterer ikke filer som inneholder HTML kode. De skal ha filnavn som "html" i filnavnet.
