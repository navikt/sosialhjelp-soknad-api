'use strict';

/* Controllers */

function PersonaliaCtrl($scope) {
	$scope.personalia = {fornavn: 'Ingvild'};
	//$scope.personalia = PersonaliaService.query();

}

function SelectCtrl($scope){
    $scope.items = [
        {id:1, name: "Ja"},
        {id:2, name: "Nei"},
    ]
}