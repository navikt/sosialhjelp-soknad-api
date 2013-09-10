'use strict';

sendsoknad.factory('soknadService', function($rootScope) {
  var soknadService = {};
  return soknadService;
});

/**
* Service som henter en søknad fra henvendelse
*/
sendsoknad.factory('HentSoknadService', function($resource){
  return $resource('http://a34duvw22389.devillo.no:8181/sendsoknad/rest/soknad/:id', {id: '@id'});
});
