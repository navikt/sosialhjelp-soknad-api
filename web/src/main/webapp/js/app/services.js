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
.factory('tekstService', function($resource) {
    return $resource('/sendsoknad/rest/enonic/:side',
        {},
        {
            get: {
                method: 'GET'
            }
        });
})

.factory('tpsService', function($resource){
        return $resource('/sendsoknad/rest/soknad/:soknadId/personalia');
    })


.factory('StartSoknadService', ['data', '$resource', '$q', function(data, $resource, $q) {
    var deferred = $q.defer();
    var soknadType = window.location.pathname.split("/")[3];

    $resource('/sendsoknad/rest/soknad/opprett/' + soknadType).get(
        function(result) { // Success
            data.soknad = result;
            deferred.resolve(result);
        },
        function() { // Error
            deferred.reject('Klarte ikke laste tekster');
        }
    );
    return deferred.promise;
}])
