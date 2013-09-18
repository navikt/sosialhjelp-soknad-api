'use strict';
/**
*  Module
*
* Description
*/
angular.module('app.directives', [])

/*Hva med casene 1-242 osv */

.directive('landskodevalidering', function(){
  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        var INTEGER_REGEX = /^\-?\d*$/;
        var kode = viewValue.slice(1, viewValue.length);
        if(viewValue.charAt(0) === '+' && INTEGER_REGEX.test(kode)) {
          ctrl.$setValidity('feil', true);
        } else {
          ctrl.$setValidity('feil', false);
        }
      });
    }
  };

})

.directive('mobilnummer', function(){
	return {
		require: 'ngModel',
		link: function(scope, elm, attrs, ctrl){
			ctrl.$parsers.unshift(function(viewValue){
        var INTEGER_REGEX = /^\-?\d*$/;
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

})

.directive('prosent', function(dateFilter){
  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl){
      var INTEGER_REGEX = /^\-?\d*$/;
      ctrl.$parsers.unshift(function(viewValue){
        if(INTEGER_REGEX.test(viewValue) && viewValue <= 100) {
       ctrl.$setValidity('prosent', true);
       return viewValue;
     } else {
       ctrl.$setValidity('prosent', false);
       return undefined;
     }
   });
    },
  };

})

.directive('datotil', function(){
  return {
    replace: true,
    require: 'ngModel',
    
    scope: {
      fraDato: '='
    },
    link: function($scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        if(typeof $scope.fraDato === 'undefined' || typeof viewValue === 'undefined'){
          ctrl.$setValidity('framindre', false);
          return undefined;
        }        

        if($scope.fraDato < viewValue){
          ctrl.$setValidity('framindre', true);
          return viewValue;
        } 
        ctrl.$setValidity('framindre', false);
        return undefined;
      }); 

      $scope.$watch('fraDato', function(value) {   
        if(typeof value === 'undefined' || typeof ctrl.$viewValue === 'undefined') {
          ctrl.$setValidity('framindre', false);
          return undefined;
        }
        var gyldig = false
        if(value < ctrl.$viewValue) {
          gyldig = true
        }
        ctrl.$setValidity('framindre', gyldig);
      });
    }
  };
})

.directive('modBrukerFaktum', function(){
  return {
      restrict: 'A',
    link: function($scope, element, attrs) {
      if($scope.soknadData.fakta) {
        $scope.soknadData.fakta[attrs.name] = {"soknadId":$scope.soknadData.soknadId, "key":attrs.name,"value":element.val()};
      }
    }
  };
})


function fraFoertil(fra, til){
  var gyldig = false;
  if(parseInt(til[2]) > parseInt(fra[2])){
    gyldig = true;
  } else if (parseInt(til[2]) === parseInt(fra[2])) {
    if(parseInt(til[1]) > parseInt(fra[1])) {
      gyldig = true;
    } else if (parseInt(til[1]) === parseInt(fra[1])) {
      if (parseInt(til[0]) > parseInt(fra[0])) {
        gyldig = true;
      }
    }
  }
  return gyldig;
}



