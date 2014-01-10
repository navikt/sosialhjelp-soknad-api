angular.module('nav.dagpengerdirective', [])
    .directive('apneBolker', [ '$timeout', function ($timeout) {
        return {
            require: '^form',
            link: function (scope, element, attrs, ctrl) {
                var bolkerIRiktigRekkefolge = ['reellarbeidssokerForm', 'arbeidsforholdForm', 'egennaeringForm', 'vernepliktForm', 'utdanningForm', 'ytelserForm', 'personaliaForm', 'barnetilleggForm'];
                var bolkIdMedFeil = '';

                $timeout(function () {
                    lukkAlleTaber();
                    if (ctrl.$invalid) {
                        var formMedFeil = '';
                        var index = bolkerIRiktigRekkefolge.length;
                        angular.forEach(ctrl.$error, function (verdi) {
                            for (var i = 0; i < verdi.length; i++) {
                                if (bolkenFinnes(verdi[i])) {
                                    if (bolkenErUferdigOgKommerForTidligereBolk(verdi[i], index)) {
                                        formMedFeil = verdi[i].$name;
                                        index = bolkerIRiktigRekkefolge.indexOf(verdi[i].$name);
                                    }
                                }
                            }
                        });

                        scope.$broadcast('OPEN_TAB', hentIdFraForm(formMedFeil));
                        var fokusElement = element.find("#" + hentIdFraForm(formMedFeil)).find('input');
                        scrollToElement(fokusElement, 400);
                    }
                }, 800);

                function bolkenErUferdigOgKommerForTidligereBolk(bolk, index) {
                    return bolk.$invalid && bolkerIRiktigRekkefolge.indexOf(bolk.$name) < index;
                }

                function bolkenFinnes(bolk) {
                    return bolkerIRiktigRekkefolge.indexOf(bolk.$name) > -1;
                }

                function hentIdFraForm(formNavn) {
                    return formNavn.split('Form')[0];
                }

                function lukkAlleTaber() {
                    var bolker = $('[data-accordion-group]');
                    scope.$broadcast('CLOSE_TAB', bolker);
                }
            }
        }
    }])

angular.module("nav.norskDatoFilter", []).filter("norskdato" , function() {
    return function(input) {
        var monthNames = ['Januar', 'Februar','Mars', 'April','Mai', 'Juni','Juli', 'August','September', 'Oktober','November', 'Desember']; 
        if(input) {
            var dag = input.substring(0,2);
            var mnd = input.substring(3,5);
            var year =input.substring(6,10);
            return dag +". " + monthNames[mnd-1] + " " + year;
        }
        return input;
    }
});