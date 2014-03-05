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
