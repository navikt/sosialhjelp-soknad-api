'use strict';

angular.module('app.services',['ngResource'])

.factory('soknad', function($rootScope) {
  var soknadService = {};
  
  return soknadService;
})

/**
* Service som henter en søknad fra henvendelse
*/


.factory('soknadService', function($resource, $http){
	//$http.defaults.withCredentials = true;
	//$http.defaults.useXDomain = true;
	//delete $http.defaults.headers.common["X-Requested-With"];
	//return $resource('http://A34DUVW22583.devillo.no:8181/sendsoknad/rest/soknad/:id', {id: '@id'});
	return $resource('/sendsoknad/rest/soknad/:id', {id: '@id'});
})
