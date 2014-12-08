angular.module('nav.vedlegg.accordion', [])
    .directive('behandletVedlegg', function () {
        return {
            replace: true,
            templateUrl: '../js/modules/vedlegg/template/vedleggAccordionBehandletTemplate.html',
            scope: {
                status: '='
            },

            link: function(scope) {
                scope.getCmsStatusNokkel = function() {
                    return 'vedlegg.behandlet.' + scope.status;
                };
                scope.vedleggKreves = function () {
                    return scope.status !== 'VedleggKreves';
                };
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


