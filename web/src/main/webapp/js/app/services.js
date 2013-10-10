'use strict';

angular.module('app.services',['ngResource'])

/**
* Service som henter en søknad fra henvendelse
*/
.factory('soknadService', function($resource) {
	return $resource('/sendsoknad/rest/soknad/:action/:param',
        {param: '@param'},
        {
            create: { method: 'POST', params: {param: '@param', action: 'opprett'} },
            send: {method: 'POST', params: {param: '@param', action: 'send'}},
            delete: {method: 'POST', params: {param: '@param', action: 'delete'}}
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
                method: 'GET',
                cache: true  // Legg på caching senere???
            }
        });
})

.factory('tpsService', function($resource){
        return $resource('/sendsoknad/rest/soknad/:soknadId/personalia');
    })
