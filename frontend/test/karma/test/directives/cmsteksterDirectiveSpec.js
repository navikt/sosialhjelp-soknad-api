describe('cmstekster', function () {
    var element, divElement, inputElement, htmlElement, anchorElement, direkteElement, $scope;

    beforeEach(module('nav.cmstekster'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {
            'tittel.key': 'Min tittel',
            'input.value': 'Dette er value',
            'div.html': '<h1>htmltest</h1>',
            'a.lenketekst': 'http://helt-riktig.com'
        }});
        $provide.value("data", {});
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope;

        inputElement = '<input value="{{ \'input.value\' | cmstekst }}">';
        htmlElement = '<div class="html" cmshtml="div.html">tester cms</div>';
        anchorElement = '<a href="{{ \'a.lenketekst\' | cmstekst }}"></a>';
        direkteElement = '<div class="direkte-innsatt">{{"tittel.key" | cmstekst }}</div>';

        element = angular.element('<div>' + divElement + inputElement + htmlElement + anchorElement + direkteElement +  '</div>');

        $compile(element)($scope);
        $scope.$apply();
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


    describe("cmshtml", function() {
        it("skal sette inn html", function() {
            expect(element.find(".html").html()).toBe("<h1>htmltest</h1>");
        });
    });
});