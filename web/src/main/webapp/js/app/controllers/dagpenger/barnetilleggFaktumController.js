angular.module('nav.barnetilleggfaktum',[])
    .controller('BarnetilleggFaktumCtrl', ['$scope', function ($scope) {
    	$scope.barnetillegg = {'key': 'barnetillegg', 
    						   'value': {
    						   		'fnr': $scope.b.value.fnr,
    						   		'avsjekket': false
    						   }
    						};

    	$scope.$watch('barnetillegg.value.avsjekket', function(newValue, oldValue, scope) {
    			console.log('nerlkdfjglkdfjg');	
    	});

    	


    }]);