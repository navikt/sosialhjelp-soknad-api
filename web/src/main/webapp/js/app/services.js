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

.factory('utslagskriterierService', function($resource){
    return $resource('/sendsoknad/rest/utslagskriterier/:uid',
        {uid: $.now()}
    );
})

