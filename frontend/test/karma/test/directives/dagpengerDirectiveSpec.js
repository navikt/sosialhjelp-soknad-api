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
    var element, scope, form, name1, name2, fakta, ngformname, ngformname2;

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
                '<div class="spm-blokk" id="bolkvalidert" data-sjekk-validert="true">' +
                    '<div data-ng-form="ngname">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname1" >' +
                    '</div>' +
                '</div>' +
                '<div class="spm-blokk" id="bolkikkevalidert" data-sjekk-validert="false">' +
                    '<div data-ng-form="ngname2">' +
                    '<input type="text" data-ng-model="modell" required="true" name="inputname2" >' +
                    '</div>' +
                '</div>' +
                '</form>');

        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        ngformname = form.ngname;
        ngformname2 = form.ngname2;
        name1 = ngformname.inputname1;
        name2 = ngformname2.inputname2;
        scope.$apply();
    }));

    describe('validerFritekst', function () {
        it('bolker som er validert skal få klassen validert', function () {
            var validertElement = element.find('.spm-blokk').first();
            expect(validertElement.hasClass('validert')).toBe(true);
        });
        it('bolker som er validert skal få klassen validert', function () {
            var validertElement = element.find('.spm-blokk').last();
            expect(validertElement.hasClass('validert')).toBe(false);
        });
        it('skjer endring i bolken og har validert-klassen skal fortsatt ikke ha validertklassen', function () {
            var validertElement = element.find('div').first();
            expect(validertElement.hasClass('validert')).toBe(true);

            name1.$setViewValue("Ikke tom");
            var form = element.find('[data-ng-form]');
            form.addClass("ng-dirty");
            scope.$apply();

            expect(validertElement.hasClass('validert')).toBe(true);
        });
        it('skjer endring i bolken og ikke har validert-klassen skal fortsatt ikke ha validertklassen', function () {
            var validertElement = element.find('spm-blokk').last();
            expect(validertElement.hasClass('validert')).toBe(false);

            name2.$setViewValue("Ikke tom");
            var form = element.find('[data-ng-form]');
            form.addClass("ng-dirty");
            scope.$apply();

            expect(validertElement.hasClass('validert')).toBe(false);

        });
    });
});
