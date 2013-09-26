'use strict';

angular.module('app.services',['ngResource'])

/**
* Service som henter en søknad fra henvendelse
*/
.factory('soknadService', function($resource) {
	return $resource('/sendsoknad/rest/soknad/:param',
        {param: '@param'},
        {
            create: { method: 'PUT' }
        }
    );
})

.factory('grunnlagsdataService', function($resource){
    return $resource('/sendsoknad/rest/grunnlagsdata');
})