describe('stegindikator', function () {
    var element, scope, timeout;

    beforeEach(module('nav.dagpengerdirective', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
        $provide.value("$cookieStore", {
            get: function() {
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
        scope.apneTab = function(){};
        timeout = $timeout;
    }));

    describe('apneBolker', function() {
        it('apneTab skal åpne første invalid bolk hvis cookien ikke er satt', function() {
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
            get: function() {
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
        scope.apneTab = function(){};
        timeout = $timeout;
    }));

    describe('apneBolker', function() {
        it('apneTab skal ikke kalles hvis cookien er satt', function() {
            spyOn(scope, 'apneTab');
            expect(scope.apneTab).wasNotCalled();
        });
    });
});
describe('norskDatoFilter', function () {
    beforeEach(module('nav.norskDatoFilter'));

    describe('norskDatoFilter', function() {
        it('norskDatoFilter skal returnere maned pa norsk', inject(function ($filter) {
            var norskdato = $filter('norskdato');
            expect(norskdato("")).toBe("");
            expect(norskdato("11-02-2013")).toBe('11. Februar 2013');
        }));
    });
});
