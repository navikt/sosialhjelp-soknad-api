var isIE8 = navigator.userAgent.match(/MSIE 8/);

if (!window.location.origin) {
    window.location.origin = window.location.protocol + "//" + window.location.host;
}

/**
 * Kutter en string som er for lang, men beholder filnavnet
 *
 * @param stringElem elementet som inneholder stringen som skal kuttes
 * @param offset valgfritt element for å kutte ned tittelen enda mer
 */
function truncateString(stringElem, offset) {
    if (offset === undefined) {
        offset = 0;
    }
    var scrollWidth = stringElem.css("overflow", "hidden")[0].scrollWidth;
    stringElem.css("overflow", "visible");
    var width = stringElem.innerWidth();

    if (scrollWidth > width) {
        var filename = stringElem.html();
        var filenameEnd = filename.substring(filename.lastIndexOf('.') - 2);
        var ratio = width / scrollWidth;
        var index = filename.length * ratio - filenameEnd.length;
        var nexText = filename.substring(0, index - offset).concat('...' + filenameEnd);
        stringElem.html(nexText);
    }
}

/**
 * Scroller så feilmeldinger vises
 *
 * @param elementId ID til elementet
 */
function popToErrorMessage(elementId) {
    scrollToElement(elementId, 300);
}

/**
 * Scroller til ett element
 *
 * @param elementId ID til elementet
 * @param offset hvor mange pixel over elementet man skal scrolle til
 */
function scrollToElement(elementId, offset) {
    if (offset === undefined) {
        offset = 100;
    }

    $(window).scrollTop(Math.max($('#' + elementId).offset().top - offset, 0));
}

function preloadImage(elementId, url) {
    var image = new Image();
    var element = $("#" + elementId);
    var dokumentInformasjon = element.closest('.dokumentInnhold').find('.dokument-informasjon');
    $(image).addClass("forside ferdig");
    image.src = url;
    if (image.complete) {
        element.replaceWith(image);
        dokumentInformasjon.show();
        image.onload = function () {
        };
    } else {
        image.onload = function () {
            setTimeout(function() {
                element.replaceWith(image);
                dokumentInformasjon.show();
                image.onload = function () {
                };
            }, 1000);
        }
    }
}

/**
 * Setter alle element som matcher en gitt selector til å ha høyde lik maks høyde av alle elementene
 *
 * @param selector jQuery-selector
 */
function setDivHeightToMatchSiblings(selector) {
    var elements = $(selector);
    var height = Math.max.apply(null, elements.map(function() {
        return $(this).height();
    }).get());
    elements.height(height);
}

function trackEventGA(page, event) {
    if (typeof _gaq !== "undefined") {
        _gaq.push(['_trackEvent', page, event]);
    }
}

function onClickCloseTooltip(element) {
    element.click(function () {
        $('.visTooltip').tooltip('hide');
        return false;
    });
}

function addTextTooltip(json) {
    var component = $(json.componentSelector);

    options = {
        html: true,
        title: json.content
    }

    component.addClass("visTooltip");
    component.tooltip(options);
    component.click(function(e) {
        $('.visTooltip').each(function() {
            if (this !== component[0]) {
                $(this).tooltip('hide');
            }
        });
        onClickCloseTooltip($('.lukk-tooltip'));

        return false;
    });
}