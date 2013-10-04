angular.module('app.routes',['ngRoute'])

.config(function($routeProvider, $locationProvider) {
    $routeProvider
  	  .when('/soknadliste', {templateUrl: '../html/templates/soknadliste.html'})
  	  .when('/personalia/:soknadId', {templateUrl: '../html/templates/personalia.html', controller: 'SoknadDataCtrl'})
      .when('/reell-arbeidssoker/:soknadId', {templateUrl: '../html/templates/reell-arbeidssoker.html', controller: 'HentSoknadDataCtrl'})
      .when('/arbeidsforhold', {templateUrl: '../html/templates/arbeidsforhold.html', controller: 'GrunnlagsdataCtrl'})
      .when('/informasjonsside', {templateUrl: '../html/templates/informasjonsside.html', controller: 'StartSoknadCtrl'})
      .when('/utslagskriterier', {templateUrl: '../html/templates/utslagskriterier.html', controller: 'GrunnlagsdataCtrl'})
      .when('/avbryt/:soknadId', {templateUrl: '../html/templates/avbryt.html', controller: 'AvbrytCtrl'})
      .when('/fortsettsenere', {templateUrl: '../html/templates/fortsettSenere.html', controller: 'SoknadDataCtrl'})
      .when('/slettet', {templateUrl: '../html/templates/soknadSlettet.html', controller: 'SlettetSoknadDataCtrl'})
      .otherwise({redirectTo: '/utslagskriterier'});

//    $locationProvider.html5Mode(true);
})