'use strict';

angular.module('app.services',['ngResource'])

/**
* Service som henter en søknad fra henvendelse
*/
.factory('soknadService', function($resource) {
	return $resource('/sendsoknad/rest/soknad/:param/:action',
        {param: '@param'},
        {
            create: { method: 'PUT' },
            send: {method: 'POST', params: {param: '@param', action: 'send'}}
        }
    );
})

.factory('utslagskriterierService', function($resource) {
    return $resource('/sendsoknad/rest/utslagskriterier/:uid',
        {uid: new Date().getTime()}
    );
})

// Husk språkstøtte...?
.factory('enonicService', function($resource) {
    return $resource('/sendsoknad/rest/enonic/:side',
        {side: '@side'},
        {
            get: {
                method: 'GET'
//                cache: true  Legg på caching senere???
            }
        });
})

//.factory('tpsService', function($resource){
//    return $resource('/sendsoknad/rest/soknad/:soknadId/personalia');
//})

.factory('tpsService', function($resource){
    return {"fakta":
            {
            "fnr":
                {
                    "soknadId":1,
                    "key":"fnr",
                    "value":"***REMOVED***",
                    "type":null},"sammensattnavn":{"soknadId":1,"key":"sammensattnavn","value":"Bob Kåre","type":null},"mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"","type":null},"fornavn":{"soknadId":1,"key":"fornavn","value":"Bob","type":null},"etternavn":{"soknadId":1,"key":"etternavn","value":"Kåre","type":null},"adresser":[{"soknadId":1,"type":"BOSTEDSADRESSE","adresseEier": null, "gatenavn":"Blåsbortveien","husnummer":"8","husbokstav":"A","postnummer":"0368","poststed":"Oslo","gyldigFra":null,"gyldigTil":null,"postboksNavn":null,"postboksNummer":null}, {"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","adresseEier": "Bob Are" ,"gatenavn":"Blåsbortveien","husnummer":"9","husbokstav":"B","postnummer":"0368","poststed":"Oslo","gyldigFra":null,"gyldigTil":"1380884578155","postboksNavn":null,"postboksNummer":null}, {"soknadId":1,"type":"POSTADRESSE","adresseEier": "Bob Are" ,"gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":"0368","poststed":"Oslo","gyldigFra":null,"gyldigTil":"1380884578155","postboksNavn":"Falkum","postboksNummer":"55"}]}}
})