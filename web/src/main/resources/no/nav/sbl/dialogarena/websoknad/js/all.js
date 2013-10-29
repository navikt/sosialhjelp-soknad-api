function scrollToElement(element, offset) {
    if (offset === undefined) {
        offset = 100;
    }

    $(window).scrollTop(Math.max(element.offset().top - offset, 0), 0);
}