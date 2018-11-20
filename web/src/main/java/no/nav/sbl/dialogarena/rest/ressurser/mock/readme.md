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

