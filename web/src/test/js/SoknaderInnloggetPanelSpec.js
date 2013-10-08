describe("SoknaderInnloggetPanel", function () {
    "use strict";

    it("Should show the number of started applications", function () {
        var soknaderInnloggetPanel = new window.Inngangsporten.SoknaderInnloggetPanel($('<section></section>'), {"antall":2}, 'Du har {} påbegynte søknader');
        soknaderInnloggetPanel.visPaabegynteSoknader();
        expect(soknaderInnloggetPanel.$el.find('p').get(0).outerHTML).toBe('<p class="paabegynte">Du har 2 påbegynte søknader</p>');
    });

    it("Should replace existing number of started applications with new value", function () {
        var soknaderInnloggetPanel = new window.Inngangsporten.SoknaderInnloggetPanel($('<section><p class="paabegynte">Du har 2 påbegynte søknader</p></section>'), {"antall":3}, 'Du har {} påbegynte søknader');
        soknaderInnloggetPanel.visPaabegynteSoknader();
        expect(soknaderInnloggetPanel.$el.find('p').get(0).outerHTML).toBe('<p class="paabegynte">Du har 3 påbegynte søknader</p>');
    });

    it("Should not show the number of applications if it's zero", function () {
        var soknaderInnloggetPanel = new window.Inngangsporten.SoknaderInnloggetPanel($('<section></section>'), {"antall":0}, 'Du har {} påbegynte søknader');
        soknaderInnloggetPanel.visPaabegynteSoknader();
        expect(soknaderInnloggetPanel.$el.find('p').length).toBe(0);
    });

    it("Should not show the number of applications if it's undefined", function () {
        var soknaderInnloggetPanel = new window.Inngangsporten.SoknaderInnloggetPanel($('<section></section>'), undefined, 'Du har {} påbegynte søknader');
        soknaderInnloggetPanel.visPaabegynteSoknader();
        expect(soknaderInnloggetPanel.$el.find('p').length).toBe(0);
    });
});
