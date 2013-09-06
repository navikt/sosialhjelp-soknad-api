'use strict';

/* App Module */

angular.module('sendsoknad', ['ngResource']).
  config(['$routeProvider', function($routeProvider) {
  	alert("Test");
  $routeProvider.
  	  when('/soknadliste', {templateUrl: 'templates/soknadliste.html'}).
  	  when('/dagpenger', {templateUrl: 'templates/dagpenger.html', controller: PersonaliaCtrl}).
      otherwise({redirectTo: '/soknadliste'});
}]);


angular.module('sendsoknad', ['ngResource']).
    factory('PersonaliaService', function($resources){
        return $resource('/soknad/:soknadId')
});
