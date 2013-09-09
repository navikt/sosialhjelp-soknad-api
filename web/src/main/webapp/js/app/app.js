'use strict';

/* App Module */

var app = angular.module('sendsoknad', ['ngRoute'])

  app.config(['$routeProvider', function($routeProvider) {
	  alert("app.js");
    $routeProvider
  	  .when('/dagpenger', {templateUrl: '../html/templates/dagpenger.html', controller: PersonaliaCtrl})
      .when('/dagpenger2', {templateUrl: '../html/templates/dagpenger2.html', controller: PersonaliaCtrl})
      .when('/wiz', {templateUrl: 'templates/wizardtwo.html', controller: WizardCtrl})
      .otherwise({redirectTo: '../html/templates/dagpenger.html'});
}]);

  app.factory('soknadService', function($rootScope) {
    var soknadService = {};
    return soknadService;
  });

  var INTEGER_REGEX = /^\-?\d*$/;

/*Hva med casene 1-242 osv */
    app.directive('landskodevalidering', function(){
    return {
      require: 'ngModel',
      link: function(scope, elm, attrs, ctrl){
        ctrl.$parsers.unshift(function(viewValue){
          var kode = viewValue.slice(1, viewValue.length);
          if(viewValue.charAt(0) === '+' && INTEGER_REGEX.test(kode)) {
            ctrl.$setValidity('feil', true);
          } else {
            ctrl.$setValidity('feil', false);
          }
        });
      }
    };

  });

  app.directive('mobilnummer', function(){
  	return {
  		require: 'ngModel',
  		link: function(scope, elm, attrs, ctrl){
  			ctrl.$parsers.unshift(function(viewValue){
  				if(INTEGER_REGEX.test(viewValue) && viewValue.length === 8) {
  					ctrl.$setValidity('feil', true);
  					return parseFloat(viewValue.replace(',', '.'));
  				} else {
  					ctrl.$setValidity('feil', false);
  					return undefined;
  				}
  			});
  		}
  	};

  });



