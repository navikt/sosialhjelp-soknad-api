# Bruk av endepunktene for å sette mockdata:

## Telefonnummer
Send en POST request med tom body og telefonnummer slik:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/telefon/{99887766}
```

Slett systemregistrert telefonnummer slik:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/telefon/slett
```

## Kontonummer
Send en POST request med tom body og kontonummer slik:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/kontonummer/{12345678903}
```

Slett systemregistrert kontonummer slik:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/kontonummer/slett
```


## Arbeid
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



Slett systemregistrert kontonummer slik:
Send en DELETE request til:
```
.../soknadsosialhjelp-server/internal/mock/tjeneste/arbeid/forhold
```

