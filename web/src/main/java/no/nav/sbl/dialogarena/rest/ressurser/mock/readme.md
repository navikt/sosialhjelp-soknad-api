# Bruk av endepunktene for å sette mockdata:

For hver gang man setter mockdata så må minimum frontend refresh'es.
For noen av mock-dataen må man starte søknaden på nytt for at de nye dataene blir lastet inn.

Oversikt:

Kan settes uten å restarte søknad
    Telefon
    Konto
    Arbeid
    Adresser
    Familie

Kan slettes uten å restarte søknad
    Telefon
    Konto
    Adresser

Trenger restart av søknad for å sette

Trenger restart av søknad for å slette
    Arbeid 
    Familie
    
    
## Adresse
#### Legg til liste med adresser
Send en POST requiest til 
```
http://localhost:8181/soknadsosialhjelp-server/internal/mock/tjeneste/adresser
```
med header: content-type: application/json
Og body
```
{
   "flereTreff": false,
   "adresseDataList": [
      {
         "kommunenummer": "1201",
         "kommunenavn": "Bergen",
         "adressenavn": "SANNERGATA",
         "husnummerFra": "0001",
         "husnummerTil": "0010",
         "postnummer": "1337",
         "poststed": "Leet",
         "geografiskTilknytning": "120102",
         "gatekode": "02081",
         "bydel": "120102",
         "husnummer": null,
         "husbokstav": null
      },
      {
         "kommunenummer": "1201",
         "kommunenavn": "Bergen",
         "adressenavn": "SANNERGATA",
         "husnummerFra": "0011",
         "husnummerTil": "9999",
         "postnummer": "1337",
         "poststed": "Leet",
         "geografiskTilknytning": "120107",
         "gatekode": "02081",
         "bydel": "120107",
         "husnummer": null,
         "husbokstav": null
      }
   ]
}
```
Slett / Tøm listen ved å sende en DELETE request til 
```
http://localhost:8181/soknadsosialhjelp-server/internal/mock/tjeneste/adresser
```

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
.../soknadsosialhjelp-server/internal/mock/tjeneste/arbeid
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
.../soknadsosialhjelp-server/internal/mock/tjeneste/arbeid
```
## OrganisasjonsMock
Send en POST request til:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/organisasjon
```
med body:
```
{
   "organisasjon": {
      "orgnummer": null,
      "navn": {
         "navnelinje": [
            "My life, for Aiur!"
         ]
      },
      "organisasjonDetaljer": null,
      "bestaarAvOrgledd": [],
      "inngaarIJuridiskEnhet": [],
      "virksomhetDetaljer": null
   }
}
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

## Utbetalinger
Send en POST til 

```
/soknadsosialhjelp-server/internal/mock/tjeneste/utbetalinger
```
med body f eks

```
{
    "posteringsdato": "2018-02-01",
    "utbetaltTil": {
        "aktoerId": "12345678910",
        "navn": "Dummy",
        "id": null,
        "diskresjonskode": null
    },
    "utbetalingNettobeloep": 3880,
    "utbetalingsmelding": null,
    "ytelseListe": [
        {
            "ytelsestype": {
                "value": "Barnetrygd",
                "kodeRef": null,
                "kodeverksRef": null
            },
            "ytelsesperiode": {
                "fom": "2018-02-01",
                "tom": "2018-02-01"
            },
            "ytelseskomponentListe": [
                {
                    "ytelseskomponenttype": "Ordinær og utvidet",
                    "satsbeloep": 0,
                    "satstype": null,
                    "satsantall": null,
                    "ytelseskomponentbeloep": 3880
                }
            ],
            "ytelseskomponentersum": 3880,
            "trekkListe": [],
            "trekksum": 0,
            "skattListe": [],
            "skattsum": 0,
            "ytelseNettobeloep": 3880,
            "bilagsnummer": "568269505",
            "rettighetshaver": {
                "aktoerId": "12345678910",
                "navn": "Dummy",
                "id": null,
                "diskresjonskode": null
            },
            "refundertForOrg": {
                "aktoerId": "000000000",
                "navn": null,
                "id": null
            }
        },
        {
            "ytelsestype": {
                "value": "Onkel Skrue penger",
                "kodeRef": null,
                "kodeverksRef": null
            },
            "ytelsesperiode": {
                "fom": "2018-02-01",
                "tom": "2018-02-01"
            },
            "ytelseskomponentListe": [
                {
                    "ytelseskomponenttype": "Sjekk",
                    "satsbeloep": 0,
                    "satstype": null,
                    "satsantall": null,
                    "ytelseskomponentbeloep": 10000.37
                },
                {
                    "ytelseskomponenttype": "Pengesekk",
                    "satsbeloep": 5000,
                    "satstype": "Dag",
                    "satsantall": 10,
                    "ytelseskomponentbeloep": 50000
                }
            ],
            "ytelseskomponentersum": 3880,
            "trekkListe": [],
            "trekksum": -500,
            "skattListe": [],
            "skattsum": -1337,
            "ytelseNettobeloep": 60000,
            "bilagsnummer": "568269566",
            "rettighetshaver": {
                "aktoerId": "12345678910",
                "navn": "Dummy",
                "id": null,
                "diskresjonskode": null
            },
            "refundertForOrg": {
                "aktoerId": "000000000",
                "navn": null,
                "id": null
            }
        }
    ],
    "utbetalingsdato": "2018-11-01",
    "forfallsdato": "2018-02-01",
    "utbetaltTilKonto": {
        "kontonummer": "32902095534",
        "kontotype": "Norsk bankkonto"
    },
    "utbetalingsmetode": "Norsk bankkonto",
    "utbetalingsstatus": "Utbetalt"
}

```
## NORG

Send en POST til 

```
/soknadsosialhjelp-server/internal/mock/tjeneste/norg
```
med body f eks

```json
{
  "0701": {
    "enhetId": 100000141,
    "navn": "NAV Horten",
    "enhetNr": "0701",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974605171"
  },
  "120107": {
    "enhetId": 100000249,
    "navn": "NAV \u00c5rstad",
    "enhetNr": "1208",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976830172"
  },
  "120102": {
    "enhetId": 100000250,
    "navn": "NAV Bergenhus",
    "enhetNr": "1209",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976830563"
  },
  "120106": {
    "enhetId": 100000251,
    "navn": "NAV Ytrebygda",
    "enhetNr": "1210",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976830652"
  },
  "120103": {
    "enhetId": 100000244,
    "navn": "NAV Fana",
    "enhetNr": "1202",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976829948"
  },
  "030105": {
    "enhetId": 100000046,
    "navn": "NAV Frogner",
    "enhetNr": "0312",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "874778702"
  },
  "1247": {
    "enhetId": 100000273,
    "navn": "NAV Ask\u00f8y",
    "enhetNr": "1247",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974600889"
  },
  "030102": {
    "enhetId": 100000049,
    "navn": "NAV Gr\u00fcnerl\u00f8kka",
    "enhetNr": "0315",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "870534612"
  },
  "030110": {
    "enhetId": 100000056,
    "navn": "NAV Grorud",
    "enhetNr": "0328",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974778866"
  },
  "030111": {
    "enhetId": 100000055,
    "navn": "NAV Stovner",
    "enhetNr": "0327",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "874778842"
  },
  "030103": {
    "enhetId": 100000048,
    "navn": "NAV Sagene",
    "enhetNr": "0314",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974778726"
  },
  "030114": {
    "enhetId": 100000051,
    "navn": "NAV Nordstrand",
    "enhetNr": "0318",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "970534679"
  },
  "030115": {
    "enhetId": 100000052,
    "navn": "NAV S\u00f8ndre Nordstrand",
    "enhetNr": "0319",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "972408875"
  },
  "120101": {
    "enhetId": 100000246,
    "navn": "NAV Arna",
    "enhetNr": "1204",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976829786"
  },
  "120108": {
    "enhetId": 100000245,
    "navn": "NAV \u00c5sane",
    "enhetNr": "1203",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976830784"
  },
  "120104": {
    "enhetId": 100000247,
    "navn": "NAV Fyllingsdalen",
    "enhetNr": "1205",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976830032"
  },
  "120105": {
    "enhetId": 100000248,
    "navn": "NAV Laksev\u00e5g",
    "enhetNr": "1206",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "976830121"
  },
  "030112": {
    "enhetId": 100000054,
    "navn": "NAV Alna",
    "enhetNr": "0326",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "970534644"
  },
  "030109": {
    "enhetId": 100000057,
    "navn": "NAV Bjerke",
    "enhetNr": "0330",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974778874"
  },
  "030101": {
    "enhetId": 100000050,
    "navn": "NAV Gamle Oslo",
    "enhetNr": "0316",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974778742"
  },
  "030108": {
    "enhetId": 100000058,
    "navn": "NAV Nordre Aker",
    "enhetNr": "0331",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974778882"
  },
  "030104": {
    "enhetId": 100000047,
    "navn": "NAV St.Hanshaugen",
    "enhetNr": "0313",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "971179686"
  },
  "030116": {
    "enhetId": 100000047,
    "navn": "NAV St.Hanshaugen",
    "enhetNr": "0313",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "971179686"
  },
  "030106": {
    "enhetId": 100000060,
    "navn": "NAV Ullern",
    "enhetNr": "0335",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "971022051"
  },
  "030107": {
    "enhetId": 100000059,
    "navn": "NAV Vestre Aker",
    "enhetNr": "0334",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "970145311"
  },
  "030113": {
    "enhetId": 100000053,
    "navn": "NAV \u00d8stensj\u00f8",
    "enhetNr": "0321",
    "antallRessurser": 0,
    "status": null,
    "orgNivaa": null,
    "type": null,
    "organisasjonsnummer": null,
    "sosialeTjenester": null,
    "orgNrTilKommunaltNavKontor": "974778807"
  }
}

```



