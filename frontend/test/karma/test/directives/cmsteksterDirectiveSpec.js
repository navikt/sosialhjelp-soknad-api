describe('cmstekster', function () {
    var element;
    var divElement;
    var inputElement;
    var htmlElement;
    var anchorElement;
    var direkteElement;
    var $scope;

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
               
        divElement = '<div>{{ \'tittel.key\' | cmstekst }}</div>';
        inputElement = '<input value="{{ \'input.value\' | cmstekst }}">';
        htmlElement = '<div class="html" cmshtml="div.html">tester cms</div>';
        anchorElement = '<a href="{{ \'a.lenketekst\' | cmstekst }}"></a>';

        element = angular.element('<div>' + divElement + inputElement + htmlElement + anchorElement + '</div>');

        $compile(element)($scope);
        $scope.$apply();
    }));

    describe("cmstekster", function() {
        it("skal sette inn cmstekst", function() {
            expect(element.find("div").html()).toBe("Min tittel");
        });

        it("skal sette inn cmstekst i input", function() {
            expect(element.find("input").attr("value")).toBe("Dette er value");
        });
    });

    describe("cmshtml", function() {
        it("skal sette inn html", function() {
            expect(element.find(".html").html()).toBe("<h1>htmltest</h1>");
        });
    });
    
    describe("cmslenketekster", function() {
        it("skal sette inn lenketekst", function() {
            expect(element.find("a").attr("href")).toBe("http://helt-riktig.com");
        });
    });
});