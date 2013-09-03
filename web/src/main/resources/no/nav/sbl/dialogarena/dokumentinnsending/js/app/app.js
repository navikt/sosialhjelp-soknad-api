angular.module('sendsoknad', ['ngRoute']).
	config(function($routeProvider, $locationProvider) {
		console.log(document.baseURI);
		$routeProvider.when('/dagpenger', {templateUrl: "html/Dagpenger.html", controller: FaktaCtrl});
		$routeProvider.when('/barnebidrag', {templateUrl: "html/Barnebidrag.html"});
		$routeProvider.when('/barnetrygd', {templateUrl: "html/Barnetrygd.html"});
		$routeProvider.otherwise({redirectTo: '/'});
	})