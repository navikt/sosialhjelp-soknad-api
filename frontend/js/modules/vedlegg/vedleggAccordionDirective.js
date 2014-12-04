angular.module('nav.vedlegg.accordion', [])
    .directive('behandletVedlegg', function () {
        return {
            replace: true,
            template: '<div class="vedleggsnavn"><p class="mini behandlet" data-ng-if="status !== \'VedleggKreves\'" data-ng-bind-html="\'vedlegg.behandlet.\' +  status  | cmstekst "></p></div>',
            scope: {
                status: '='
            }
        };
    })
    .directive('vedleggAccordion', function () {
        return {
            replace: true,
            templateUrl: '../js/modules/vedlegg/template/vedleggbolkTemplate.html',
            scope: {
                forventning: '='
            },
            link: function (scope) {

                scope.vedleggEr = function(status) {
                    return scope.forventning.innsendingsvalg === status;
                };

                scope.ekstraVedleggFerdig = function () {
                    if(scope.forventning.skjemaNummer === 'N6') {
                        return scope.forventning.navn !== null && scope.forventning.navn !== undefined;
                    }
                    return true;
                };

                scope.vedleggFerdigBehandlet = function() {
                    return scope.ekstraVedleggFerdig() && !scope.vedleggEr('VedleggKreves');
                };
            }
        };
    });


