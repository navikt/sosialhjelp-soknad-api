angular.module('app.routes',['ngRoute'])

.config(function($routeProvider) {
    $routeProvider
  	  .when('/soknadliste', {templateUrl: '../html/templates/soknadliste.html'})
  	  .when('/personalia/:soknadId', {templateUrl: '../html/templates/personalia.html', controller: 'SoknadDataCtrl'})
      .when('/reell-arbeidssoker', {templateUrl: '../html/templates/reell-arbeidssoker.html', controller: 'PersonaliaCtrl'})
      .when('/arbeidsforhold', {templateUrl: '../html/templates/arbeidsforhold.html', controller: 'PersonaliaCtrl'})
      .when('/utslagskriterier', {templateUrl: '../html/templates/utslagskriterier.html'})
      .otherwise({redirectTo: '/utslagskriterier'});
})