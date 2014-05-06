describe('cmstekster', function () {
    var element, divElement, inputElement, htmlElement, anchorElement, direkteElement, scope;

    beforeEach(module('nav.cmstekster'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {
            'tittel.key': 'Min tittel',
            'input.value': 'Dette er value',
            'a.lenketekst': 'http://helt-riktig.com'
        }});
        $provide.value("data", {});
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        inputElement = '<input value="{{ \'input.value\' | cmstekst }}">';
        anchorElement = '<a href="{{ \'a.lenketekst\' | cmstekst }}"></a>';
        direkteElement = '<div class="direkte-innsatt">{{"tittel.key" | cmstekst }}</div>';

        element = angular.element('<div>' + divElement + inputElement + htmlElement + anchorElement + direkteElement +  '</div>');

        $compile(element)(scope);
        scope.$apply();
    }));

    describe("cmstekst", function() {
        it("skal sette inn cmstekst direkte", function() {
            expect(element.find(".direkte-innsatt").html()).toBe("Min tittel");
        });

        it("skal sette inn cmstekst i input", function() {
            expect(element.find("input").attr("value")).toBe("Dette er value");
        });

        it("skal sette inn lenketekst", function() {
            expect(element.find("a").attr("href")).toBe("http://helt-riktig.com");
        });
    });
});
describe('cmsteksterVisCmsnokkler', function () {
    var element, divElement, inputElement, htmlElement, anchorElement, direkteElement, scope;

    beforeEach(module('nav.cmstekster'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {
            'tittel.key': 'Min tittel',
            'input.value': 'Dette er value',
            'a.lenketekst': 'http://helt-riktig.com'
        }});
        $provide.value("data", {});
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        inputElement = '<input value="{{ \'input.value\' | cmstekst }}">';
        anchorElement = '<a href="{{ \'a.lenketekst\' | cmstekst }}"></a>';
        direkteElement = '<div class="direkte-innsatt">{{"tittel.key" | cmstekst }}</div>';

        element = angular.element('<div>' + divElement + inputElement + htmlElement + anchorElement + direkteElement +  '</div>');
        scope.visCmsnokkler = true;

        $compile(element)(scope);
        scope.$apply();
    }));

    describe("cmstekst", function() {
        it("skal sette inn cmstekst direkte", function() {
            expect(element.find(".direkte-innsatt").html()).toBe("Min tittel [tittel.key]");
        });
    });
});
describe('cmsvedlegg', function () {
    var element, compile, scope;

    beforeEach(module('nav.cmstekster'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {
            'tittel.key': 'Min tittel',
            'input.value': 'Dette er value',
            'a.lenketekst': 'http://helt-riktig.com'
        }});
        $provide.value("data", {});
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        compile = $compile;
        scope.$apply();
    }));

    describe("cmsvedlegg", function() {
        it("hvis cmsvedlegg ikke har satt en verdi skal cmsProps kun opprette et object", function() {
            element = angular.element(
                '<div data-cmsvedlegg data-nav-faktum="etnavfaktum"></div>');

            compile(element)(scope);

            expect(typeof scope.cmsProps === 'object').toBe(true);
        });
        it("hvis cmsvedlegg har satt en verdi skal cmsProps.ekstra settes til denne verdien", function() {
            element = angular.element(
                '<div data-cmsvedlegg="enverdi" data-nav-faktum="etnavfaktum"></div>');

            compile(element)(scope);

            expect(typeof scope.cmsProps === 'object').toBe(true);
            expect(scope.cmsProps.ekstra).toBe("enverdi");
        });
    });
});
describe('configUrl', function () {
    var element, compile, scope;

    beforeEach(module('nav.cmstekster'));

    beforeEach(module(function ($provide) {
        $provide.value("data", {
            config: {
                'enurl.url': 'www.nav.no'
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        compile = $compile;
        scope.$apply();
    }));

    describe("configUrl", function() {
        it("hvis data.config inneholder urlen, skal denne returneres av filteret", function() {
            element = angular.element(
                '<div>{{"enurl" | configUrl }}</div>');

            compile(element)(scope);
            scope.$apply();

            expect(element.text()).toBe('www.nav.no');
        });
        it("hvis cmsvedlegg ikke har satt en verdi skal cmsProps kun opprette et object", function() {
            element = angular.element(
                '<div>{{"enurlsomikkefinnes" | configUrl }}</div>');

            compile(element)(scope);
            scope.$apply();

            expect(element.text()).toBe('');
        });
    });
});