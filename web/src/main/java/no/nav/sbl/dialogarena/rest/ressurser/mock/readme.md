# Bruk av endepunktene for å sette mockdata:

## Telefonnummer
##### Legg til systemregistrert telefonnummer
Send en POST request til:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/telefon
```
med header: content-type: application/json

Og body:
```
{
  "verdi":"99887766"
}
```

##### Slett systemregistrert telefonnummer
Send en DELETE request
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/telefon
```

## Kontonummer
##### Legg til systemregistrert kontonummer
Send en POST request til:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/kontonummer
```
med header: content-type: application/json

Og body:
```
{
	"verdi" : "12345678903"
}
```
##### Slett systemregistrert kontonummer
Send en DELETE request til 
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/kontonummer
```


## Arbeid
##### Legg til liste av systemregistrert arbeidsforhold
Send en POST request til:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/arbeid/forhold
```
med body:
```
{
  "arbeidsforhold" : [ {
    "arbeidsforholdIDnav" : 0,
    "ansettelsesPeriode" : {
      "periode" : {
        "fom" : 1388617200000,
        "tom" : 1545260400000
      }
    },
    "arbeidsavtale" : [ {
      "stillingsprosent" : 100
    } ],
    "arbeidsgiver" : {
      "orgnummer" : "123"
    }
  }, {
    "arbeidsforholdIDnav" : 0,
    "ansettelsesPeriode" : {
      "periode" : {
        "fom" : 1388617200000,
        "tom" : 1545260400000
      }
    },
    "arbeidsavtale" : [ {
      "stillingsprosent" : 100
    } ],
    "arbeidsgiver" : {
      "arbeidsgivernummer": "lala",
      "navn": "Historisk arbeidsgiver"
    }
  }, {
    "arbeidsforholdIDnav" : 0,
    "ansettelsesPeriode" : {
      "periode" : {
        "fom" : 1388617200000,
        "tom" : 1545260400000
      }
    },
    "arbeidsavtale" : [ {
      "stillingsprosent" : 100
    } ],
    "arbeidsgiver" : {
      "ident": {
        "ident": "12345678901"
      }
    }
  }

  ]
}

```
med header: content-type: application/json


##### Slett alle systemregistrert arbeidsforhold

Send en DELETE request til:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/arbeid/forhold
```

## Person og Familierelasjoner
##### Legg til
Send en POST til
```
/soknadsosialhjelp-server/internal/mock/tjeneste/person
```
med body
```
{
   "diskresjonskode": null,
   "bankkonto": null,
   "bostedsadresse": null,
   "sivilstand": {
      "sivilstand": {
         "value": "GIFT",
         "kodeRef": null,
         "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Sivilstander"
      },
      "fomGyldighetsperiode": null,
      "tomGyldighetsperiode": null,
      "endringstidspunkt": null,
      "endretAv": null,
      "endringstype": null
   },
   "statsborgerskap": {
      "land": {
         "value": "NOR",
         "kodeRef": null,
         "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Landkoder"
      },
      "endringstidspunkt": null,
      "endretAv": null,
      "endringstype": null
   },
   "harFraRolleI": [
      {
         "harSammeBosted": null,
         "tilRolle": {
            "value": "BARN",
            "kodeRef": null,
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Familierelasjoner"
         },
         "tilPerson": {
            "diskresjonskode": null,
            "bankkonto": null,
            "bostedsadresse": null,
            "sivilstand": null,
            "statsborgerskap": null,
            "harFraRolleI": [],
            "ident": {
               "ident": "***REMOVED***",
               "type": {
                  "value": "FNR",
                  "kodeRef": null,
                  "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Personidenter"
               }
            },
            "kjoenn": null,
            "personnavn": {
               "etternavn": "Mockmann",
               "fornavn": "Dole",
               "mellomnavn": null,
               "sammensattNavn": null,
               "endringstidspunkt": null,
               "endretAv": null,
               "endringstype": null
            },
            "personstatus": null,
            "postadresse": null,
            "doedsdato": {
               "doedsdato": 1391295600000,
               "endringstidspunkt": null,
               "endretAv": null,
               "endringstype": null
            },
            "foedselsdato": null
         },
         "endringstidspunkt": null,
         "endretAv": null,
         "endringstype": null
      },
      {
         "harSammeBosted": true,
         "tilRolle": {
            "value": "BARN",
            "kodeRef": null,
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Familierelasjoner"
         },
         "tilPerson": {
            "diskresjonskode": null,
            "bankkonto": null,
            "bostedsadresse": null,
            "sivilstand": null,
            "statsborgerskap": null,
            "harFraRolleI": [],
            "ident": {
               "ident": "***REMOVED***",
               "type": {
                  "value": "FNR",
                  "kodeRef": null,
                  "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Personidenter"
               }
            },
            "kjoenn": null,
            "personnavn": {
               "etternavn": "Mockmann",
               "fornavn": "Ole",
               "mellomnavn": null,
               "sammensattNavn": null,
               "endringstidspunkt": null,
               "endretAv": null,
               "endringstype": null
            },
            "personstatus": null,
            "postadresse": null,
            "doedsdato": null,
            "foedselsdato": null
         },
         "endringstidspunkt": null,
         "endretAv": null,
         "endringstype": null
      },
      {
         "harSammeBosted": false,
         "tilRolle": {
            "value": "BARN",
            "kodeRef": null,
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Familierelasjoner"
         },
         "tilPerson": {
            "diskresjonskode": null,
            "bankkonto": null,
            "bostedsadresse": null,
            "sivilstand": null,
            "statsborgerskap": null,
            "harFraRolleI": [],
            "ident": {
               "ident": "***REMOVED***",
               "type": {
                  "value": "FNR",
                  "kodeRef": null,
                  "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Personidenter"
               }
            },
            "kjoenn": null,
            "personnavn": {
               "etternavn": "Mockmann",
               "fornavn": "Doffen",
               "mellomnavn": null,
               "sammensattNavn": null,
               "endringstidspunkt": null,
               "endretAv": null,
               "endringstype": null
            },
            "personstatus": null,
            "postadresse": null,
            "doedsdato": null,
            "foedselsdato": null
         },
         "endringstidspunkt": null,
         "endretAv": null,
         "endringstype": null
      },
      {
         "harSammeBosted": true,
         "tilRolle": {
            "value": "EKTE",
            "kodeRef": null,
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Familierelasjoner"
         },
         "tilPerson": {
            "diskresjonskode": null,
            "bankkonto": null,
            "bostedsadresse": null,
            "sivilstand": null,
            "statsborgerskap": null,
            "harFraRolleI": [],
            "ident": {
               "ident": "***REMOVED***",
               "type": {
                  "value": "FNR",
                  "kodeRef": null,
                  "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Personidenter"
               }
            },
            "kjoenn": null,
            "personnavn": {
               "etternavn": "Duck",
               "fornavn": "Daisy",
               "mellomnavn": null,
               "sammensattNavn": null,
               "endringstidspunkt": null,
               "endretAv": null,
               "endringstype": null
            },
            "personstatus": null,
            "postadresse": null,
            "doedsdato": null,
            "foedselsdato": {
               "foedselsdato": 124070400000,
               "endringstidspunkt": null,
               "endretAv": null,
               "endringstype": null
            }
         },
         "endringstidspunkt": null,
         "endretAv": null,
         "endringstype": null
      }
   ],
   "ident": {
      "ident": "***REMOVED***",
      "type": {
         "value": "FNR",
         "kodeRef": null,
         "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Personidenter"
      }
   },
   "kjoenn": null,
   "personnavn": {
      "etternavn": "Mockmann",
      "fornavn": "Donald",
      "mellomnavn": "D.",
      "sammensattNavn": null,
      "endringstidspunkt": null,
      "endretAv": null,
      "endringstype": null
   },
   "personstatus": null,
   "postadresse": null,
   "doedsdato": null,
   "foedselsdato": {
      "foedselsdato": -205113600000,
      "endringstidspunkt": null,
      "endretAv": null,
      "endringstype": null
   }
}
```
##### Sett tilbake til default person
Send en DELETE til
```
/soknadsosialhjelp-server/internal/mock/tjeneste/person
```



