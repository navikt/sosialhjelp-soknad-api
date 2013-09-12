angular.module('brukerdata', ['services'])

function SoknadDataCtrl_http($scope, $http) {
	$http({method: 'GET', url: '/sendsoknad/rest/soknad/' + 1}).
		success(function (data, status) {
			$scope.soknadData = data
		}).
		error(function(data, status){
			alert("En feil skjedde");
		});
}

function SoknadDataCtrl($scope, soknadFactory) {
	var soknadData = soknadFactory.get({id: 1}, function()  {
		soknadData.gosysId = 'Ny gosysId';
		soknadData.$save({id: 1});
	});
}