'use strict';

angular.module('services', ['ngResource'])

.factory('soknad', function($rootScope) {
  var soknadService = {};
  return soknadService;
})

/**
* Service som henter en søknad fra henvendelse
*/

.factory('soknadService', function($resource){
	return $resource('/sendsoknad/rest/soknad/:id', {id: '@id'});
})
