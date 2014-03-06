describe('stegindikator', function () {
    var element, scope, timeout;

    beforeEach(module('nav.dagpengerdirective', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
        $provide.value("$cookieStore", {
            get: function () {
                return false;
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        element = angular.element(
            '<form name="">' +
                '<div data-apne-bolker> ' +
                '</form>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
        scope.apneTab = function () {
        };
        timeout = $timeout;
    }));

    describe('apneBolker', function () {
        it('apneTab skal åpne første invalid bolk hvis cookien ikke er satt', function () {
            spyOn(scope, 'apneTab');
            timeout.flush();
            expect(scope.apneTab).toHaveBeenCalled();
        });
    });
});
describe('stegindikator', function () {
    var element, scope, timeout;

    beforeEach(module('nav.dagpengerdirective', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
        $provide.value("$cookieStore", {
            get: function () {
                return true;
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        element = angular.element(
            '<form name="">' +
                '<div data-apne-bolker> ' +
                '</form>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
        scope.apneTab = function () {
        };
        timeout = $timeout;
    }));

    describe('apneBolker', function () {
        it('apneTab skal ikke kalles hvis cookien er satt', function () {
            spyOn(scope, 'apneTab');
            expect(scope.apneTab).wasNotCalled();
        });
    });
});
describe('norskDatoFilter', function () {
    beforeEach(module('nav.norskDatoFilter'));

    describe('norskDatoFilter', function () {
        it('norskDatoFilter skal returnere maned pa norsk', inject(function ($filter) {
            var norskdato = $filter('norskdato');
            expect(norskdato("")).toBe("");
            expect(norskdato("11-02-2013")).toBe('11. Februar 2013');
        }));
    });
});
describe('tilleggsopplysninger', function () {
    var element, scope, form, name;
    beforeEach(module('nav.tilleggsopplysninger'));
    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        element = angular.element(
            '<form name="form">' +
                '<div class="spm-blokk validert">' +
                '<div data-valider-fritekst> ' +
                '<input type="text" data-ng-model="modell" required="true" name="inputname" >' +
                '</div>' +
                '</div>' +
                '</form>');

        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        name = form.inputname;
        scope.$apply();
    }));

    describe('validerFritekst', function () {
        it('tilleggsopplysninger skal ikke få klassen validert hvis formen ikke er valid og lukket', function () {
            var validertElement = element.find('div').first();

            expect(validertElement.hasClass('validert')).toBe(false);
        });
        it('tilleggsopplysninger skal ikke få klassen validert hvis formen ikke er valid og åpen', function () {
            var validertElement = element.find('div').first();
            validertElement.addClass('open');
            expect(validertElement.hasClass('validert')).toBe(false);
            expect(validertElement.hasClass('open')).toBe(true);
        });
        it('tilleggsopplysninger skal få klassen validert hvis formen er valid og åpen', function () {
            var validertElement = element.find('div').first();
            validertElement.addClass('open');

            name.$setViewValue("Ikke tom");
            scope.$apply();

            expect(validertElement.hasClass('validert')).toBe(true);
            expect(validertElement.hasClass('open')).toBe(true);
        });
        it('tilleggsopplysninger skal ikke få klassen validert hvis formen er valid og lukket som er det første som skjer når en soknad startes', function () {
            var validertElement = element.find('div').first();

            name.$setViewValue("Ikke tom");
            scope.$apply();

            expect(validertElement.hasClass('validert')).toBe(false);
            expect(validertElement.hasClass('open')).toBe(false);
        });
    });
});
describe('sjekkBoklerValiditet', function () {
    var element, scope, form, name1, name2, name3, name4, fakta, ngformname, ngformname2, ngformname3, ngformname4;

    beforeEach(module('nav.sjekkBoklerValiditet'));
    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'bolker',
            properties: {
                'bolkvalidert': "true",
                'bolkikkevalidert': "false"
            },
                $save: function(){}
            }
        ];
    $provide.value("data", {
        fakta: fakta,
        finnFaktum: function (key) {
            var res = null;
            fakta.forEach(function (item) {
                if (item.key == key) {
                    res = item;
                }
            });
            return res;
        },
        finnFakta: function (key) {
            var res = [];
            fakta.forEach(function (item) {
                if (item.key === key) {
                    res.push(item);
                }
            });
            return res;
        },
        leggTilFaktum: function (faktum) {
            fakta.push(faktum);
        }
    });
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        element = angular.element(
            '<form name="form">' +
                '<div class="spm-blokk1" id="bolkvalidert" data-sjekk-validert="true">' +
                    '<div data-ng-form="ngname">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname1" >' +
                    '</div>' +
                '</div>' +
                '<div class="spm-blokk2" id="bolkikkevalidert" data-sjekk-validert="false">' +
                    '<div data-ng-form="ngname2">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname2" >' +
                    '</div>' +
                '</div>' +
                '<div class="spm-blokk3" id="bolkikkevalidert" data-is-open="true" data-sjekk-validert="true">' +
                    '<div data-ng-form="ngname3">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname3" >' +
                    '</div>' +
                '</div>' +
                '<div class="spm-blokk3a" id="bolkikkevalidert" data-is-open="false" data-sjekk-validert="true">' +
                    '<div data-ng-form="ngname3">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname3" >' +
                    '</div>' +
                '</div>' +
                '<div class="spm-blokk3b" id="bolkikkevalidert" data-sjekk-validert="true">' +
                    '<div data-ng-form="ngname3">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname3" >' +
                    '</div>' +
                '</div>' +
                '<div class="spm-blokk4 validert" id="bolkikkevalidert" data-sjekk-validert="false">' +
                    '<div data-ng-form="ngname4">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname4" >' +
                '   </div>' +
                '</div>' +
            '</form>');

        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        ngformname = form.ngname;
        ngformname2 = form.ngname2;
        ngformname3 = form.ngname3;
        ngformname4 = form.ngname4;
        name1 = ngformname.inputname1;
        name2 = ngformname2.inputname2;
        name3 = ngformname3.inputname3;
        name4 = ngformname4.inputname4;
        scope.$apply();
    }));

    describe('validerFritekst', function () {
        it('bolker som er validert skal få klassen validert', function () {
            var validertElement = element.find('.spm-blokk1').first();
            expect(validertElement.hasClass('validert')).toBe(true);
        });
        it('bolker som er validert skal få klassen validert', function () {
            var validertElement = element.find('.spm-blokk2').last();
            expect(validertElement.hasClass('validert')).toBe(false);
        });
        it('skjer endring i bolken og har validert-klassen skal fortsatt ikke ha validertklassen', function () {
            var validertElement = element.find('.spm-blokk1').first();
            expect(validertElement.hasClass('validert')).toBe(true);

            name1.$setViewValue("Ikke tom");
            var form = element.find('[data-ng-form]');
            form.addClass("ng-dirty");
            scope.$apply();

            expect(validertElement.hasClass('validert')).toBe(true);
        });
        it('Element som ikke har validertklasse får dette hvis bolken skal settes til validert ved første åpning og den er åpen', function () {
            var validertElement = element.find('.spm-blokk3');
            expect(validertElement.hasClass('validert')).toBe(true);
        });
        it('Element som ikke har validertklasse får ikke dette hvis bolken er lukket selv om det skal få det ved første åpning', function () {
            var validertElement = element.find('.spm-blokk3a');
            expect(validertElement.hasClass('validert')).toBe(false);
            var validertElement = element.find('.spm-blokk3b');
            expect(validertElement.hasClass('validert')).toBe(false);
        });
        it('Element som har validertklasse, og skal ikke få validert på første steg, og blir endret skal ikke ha validertklassen lenger', function () {
            var validertElement = element.find('.spm-blokk4');
            expect(validertElement.hasClass('validert')).toBe(true);
            name4.$setViewValue("Ikke tom");
            scope.$apply();
            expect(validertElement.hasClass('validert')).toBe(false);
        });
        it('Element som har validertklasse, og skal ikke få validert på første steg, og blir endret skal ikke ha validertklassen lenger', function () {
            var validertElement = element.find('.spm-blokk4');
            expect(validertElement.hasClass('validert')).toBe(true);
            name4.$setViewValue("Ikke tom");
            var form = validertElement.find('[data-ng-form]');
            form.addClass("ng-dirty");
            scope.$apply();
            expect(validertElement.hasClass('validert')).toBe(false);
        });
    });
});
describe('scrollTilbakeDirective', function () {
    var element, scope, timeout;

    beforeEach(module('nav.scroll.directive', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
        $provide.value("$cookieStore", {
            get: function () {
                return {aapneTabs: "tab", gjeldendeTab: "#tab", faktumId:1}
            },
            remove: function(key) {}
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        element = angular.element(
            '<form name="">' +
                '<div data-scroll-tilbake-directive>' +
                    '<div id="tab" class="test">' +
                        '<div id="tab1"></div>' +
                    '</div>' +
                    '<div class="knapp-leggtil-liten"></div>' +
                '</div>' +
            '</form>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
        scope.apneTab = function () {
        };
        timeout = $timeout;
    }));

    describe('apneBolker', function () {
        it('apneTab skal ikke kalles hvis cookien er satt', function () {
            spyOn(scope, 'apneTab');
            timeout.flush();
            expect(scope.apneTab).toHaveBeenCalledWith("tab");
        });
    });
});
describe('scrollTilbakeDirective', function () {
    var element, scope, timeout;

    beforeEach(module('nav.scroll.directive', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
        $provide.value("$cookieStore", {
            get: function () {
                return false;
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        element = angular.element(
            '<form name="">' +
                '<div data-scroll-tilbake-directive> ' +
                '</form>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
        scope.apneTab = function () {
        };
        timeout = $timeout;
    }));

    describe('scrollTilbakeDirective', function () {
        it('ApneTab skal ikke bli kalt hvis cookien ikke inneholder noe', function () {
            spyOn(scope, 'apneTab');
            timeout.flush();
            expect(scope.apneTab).wasNotCalled();
        });
    });
});
describe('validerSkjema', function () {
    var element, scope, timeout, event, deferred, q, $httpBackend;
    event = $.Event("click");

    beforeEach(module('nav.validerskjema', 'nav.cmstekster', 'templates-main', 'app.services'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'bolker',
                value: "",
                properties: {
                    iden: 'false'
                },
                $save: function(){
                    deferred = q.defer();
                    return deferred.promise;
                }
            }
        ];

        $provide.value("data", {
            fakta: fakta,
            finnFaktum: function (key) {
                var res = null;
                fakta.forEach(function (item) {
                    if (item.key == key) {
                        res = item;
                    }
                });
                return res;
            },
            finnFakta: function (key) {
                var res = [];
                fakta.forEach(function (item) {
                    if (item.key === key) {
                        res.push(item);
                    }
                });
                return res;
            },
            leggTilFaktum: function (faktum) {
                fakta.push(faktum);
            },
            soknad: {soknadId: 1}
        });
        $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
        $provide.value("$routeParams", {});
        $provide.value("validertKlasse", {});
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout, $q, $injector) {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.expectPOST('/sendsoknad/rest/soknad/delsteg/1/vedlegg?').
            respond('');
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<div data-valider-skjema>' +
                    '<div class="accordion-group">' +
                    '</div>' +
                '</div>' +
                '</form>');
        q = $q;
        $compile(element)(scope);
        scope.$apply();
        timeout = $timeout;
        scope.fremdriftsindikator = {};
        scope.grupper = [{
            valideringsmetode: function(key){},
        id: "iden",
        validering: false}];
    }));

    describe('validerSkjema', function () {
        it('validerSkjema skal kalle event preventDefault', function () {
            spyOn(event, 'preventDefault');
            scope.validerSkjema(event);
            expect(event.preventDefault).toHaveBeenCalled();
        });
        it('fremdriftsinidikatoren skal bli satt til true', function () {
            scope.validerSkjema(event);
            expect(scope.fremdriftsindikator.laster).toBe(true);
        });
        it('grupper som ikke er validert skal bli satt til true', function () {
            expect(scope.grupper[0].validering).toBe(false);
            scope.validerSkjema(event);
            expect(scope.grupper[0].validering).toBe(true);
        });
        it('hver av valideringsmetodene til gruppene skal bli kalt hvis bolken ikke er validert', function () {
            spyOn(scope.grupper[0], 'valideringsmetode');
            scope.validerSkjema(event);
            timeout.flush();
            deferred.resolve();
            scope.$root.$digest();
            expect(scope.grupper[0].valideringsmetode).toHaveBeenCalledWith(false);
        });
    });
});