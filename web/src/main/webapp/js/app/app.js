'use strict';

/* App Module */
var sendsoknad = angular.module('sendsoknad', ['ngRoute', 'ngResource']);

sendsoknad.config(function($routeProvider) {
    $routeProvider
  	  .when('/soknadliste', {templateUrl: '../html/templates/soknadliste.html'})
  	  .when('/dagpenger', {templateUrl: '../html/templates/dagpenger.html', controller: PersonaliaCtrl})
      .when('/reell-arbeidssoker', {templateUrl: '../html/templates/reell-arbeidssoker.html', controller: PersonaliaCtrl})
      .when('/arbeidsforhold', {templateUrl: '../html/templates/arbeidsforhold.html', controller: PersonaliaCtrl})
      .otherwise({redirectTo: '/dagpenger'});
});

  var INTEGER_REGEX = /^\-?\d*$/;

/*Hva med casene 1-242 osv */
    sendsoknad.directive('landskodevalidering', function(){
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

  sendsoknad.directive('mobilnummer', function(){
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



