angular.module('sendsoknad', ['ngRoute', 'ngResource']).
    config(function($routeProvider, $locationProvider) {
		alert("wefwhef" + document.baseURI);
		$routeProvider.when('sendSoknad/startSoknad/:soknadType', {templateUrl: "html/Dagpenger.html", controller: FaktaCtrl});
		$routeProvider.when('/barnebidrag', {templateUrl: "html/Barnebidrag.html"});
		$routeProvider.when('/barnetrygd', {templateUrl: "html/Barnetrygd.html"});
		$routeProvider.otherwise({redirectTo: '/'});
	});

	angular.module('sendsoknad', ['ngResource']).
	    factory('PersonaliaService', function($resources){
            return $resource('/soknad/:soknadId')
	});
