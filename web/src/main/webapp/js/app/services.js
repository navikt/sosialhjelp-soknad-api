'use strict';

angular.module('app.services',['ngResource'])

/**
* Service som henter en søknad fra henvendelse
*/
.factory('soknadService', function($resource) {
	return $resource('/sendsoknad/rest/soknad/:param/:action',
        {param: '@param'},
        {
            create: { method: 'POST' },
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
                method: 'GET',
                cache: true
            }
        });
})

.factory('tpsService', function($resource){
        return $resource('/sendsoknad/rest/soknad/:soknadId/personalia');
    });