'use strict';
/**
*  Module
*
* Description
*/
angular.module('app.directives', ['app.services', 'nav.booleanradio', 'nav.cmstekster', 'nav.input', 'nav.sporsmalblokk'])

/*Hva med casene 1-242 osv? */

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

.directive('prosent', function(){
  return {
    replace:true,
    require: 'ngModel',

    link: function(scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        var INTEGER_REGEX = /^\-?\d*$/;
        if(INTEGER_REGEX.test(viewValue) && viewValue <= 100 && viewValue >=0) {
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
      fraDato: '=',
      tilDato: '='
    },
    link: function($scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        if(typeof $scope.fraDato === 'undefined' || typeof viewValue === 'undefined'){
          ctrl.$setValidity('framindre', false);
          $scope.tilDato = undefined;
          return undefined;
        }

        if(fraMindreEnnTil($scope.fraDato, viewValue)){
          ctrl.$setValidity('framindre', true);
          $scope.tilDato = new Date(ctrl.$viewValue);
          return viewValue;
        }
        ctrl.$setValidity('framindre', false);
        $scope.tilDato = undefined;
        return undefined;
      });

      $scope.$watch('fraDato', function(fraDatoValue) {
        var vw = ctrl.$viewValue;
        if(typeof fraDatoValue === 'undefined' || typeof ctrl.$viewValue === 'undefined') {
          ctrl.$setValidity('framindre', false);
        }

        if(fraMindreEnnTil(fraDatoValue, ctrl.$viewValue)){
          if(typeof $scope.tilDato === 'undefined') {
            $scope.tilDato = new Date(ctrl.$viewValue);
            ctrl.$setValidity('framindre', true);
          }
          ctrl.$setValidity('framindre', true);
        } else {
          ctrl.$setValidity('framindre', false);
          $scope.tilDato = undefined;
          return undefined;
        }

      });
    }
  };
})

.directive('booleanVerdi', function(){
return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attr, ngModel){
        function fraTekst(tekst) {
            if(tekst === "true") {
                return true;
            }
            return false;
        }
        ngModel.$formatters.push(fraTekst);
    }
};
})

.directive('knapprad', function ($location, $routeParams) {
    return {
        restrict: "E",
        replace: true,
        template: function() {
            var rad =  $('<div/>')
                .addClass('rad');

            var begrensning = $('<div/>').addClass('begrensning');
            rad.append(begrensning);

            var avbryt = $('<a/>')
                .attr('id', 'avbryt')
                .attr('href', '#/avbryt/' + $routeParams.soknadId)
                .text('Avbryt');

            var fortsettSenere = $('<a/>')
                .attr('id', 'fortsettSenere')
                .attr('href', '#/fortsettsenere')
                .text('Fortsett senere');

            begrensning
                .append($('<section/>')
                    .append(avbryt)
                    .append(fortsettSenere));

            return rad.prop('outerHTML');
        }
    };
})

.directive('fremdriftsindikator', function() {
    return {
        link: function(scope, element) {
            var image = angular.element('<img src="../img/ajaxloader/hvit/loader_hvit_48.gif"/>');
            element.after(image);
            image.hide();

            element.click(function() {
                image.show();
                element.hide();
            });
        }
    }
});


function fraMindreEnnTil(fra, til){
  var gyldig = false;
  if (fra.getTime() < til.getTime()) {
    gyldig = true
  }

  return gyldig;
}