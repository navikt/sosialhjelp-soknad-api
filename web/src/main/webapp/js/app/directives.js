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


.directive('dato', function(dateFilter){
  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        var datoListe = viewValue.split('.');
        var dato = dateFilter(new Date(datoListe[2], datoListe[1]-1, datoListe[0]), 'dd.MM.yyyy');
        if(viewValue === dato){  
         ctrl.$setValidity('dato', true);
         return dato;
       } else {
         ctrl.$setValidity('dato', false);
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
      fraDato: '=',
      fraNavn: '@'
    },
    link: function(scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        if(typeof scope.fraDato === 'undefined' || typeof viewValue === 'undefined'){
          ctrl.$setValidity('framindre', false);
          return undefined;
        }
        

        var navn = scope.fraNavn;
        var til = viewValue.split('.');
        var fra = scope.fraDato.split('.');

        if(fratil(fra, til)){
        var fra = sc.varighetFra.split('.');

          ctrl.$setValidity('framindre', true);
          return viewValue;
        } 
        ctrl.$setValidity('framindre', false);
        return undefined;

      }); 

      scope.$watch('arbeidsforhold.permiteringFra', function(value) { 
        if(typeof value === 'undefined' || typeof ctrl.$viewValue === 'undefined') {
          ctrl.$setValidity('framindre', false);
          return undefined;
        }
        var til = ctrl.$viewValue.split('.');
        var fra = value.split('.');
        ctrl.$setValidity('framindre', fratil(fra, til));
      }, true);
    }
  };
})



function fratil(fra, til){
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


