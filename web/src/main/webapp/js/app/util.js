if (!Array.prototype.last) {
    Array.prototype.last = function() {
        return this[this.length -1];
    }
}

if (!Array.prototype.contains) {
    Array.prototype.contains = function(val) {
        return $.inArray(val, this) > -1;
    }
}

function checkTrue(element) {
    if (element == undefined) {
        return false;
    }
    return element.toString() == 'true';
}

function scrollToElement(element) {
    var animationSpeed = 200;
    var offset = 100;
    var scrollPos = Math.max(element.offset().top - offset, 0);
    $('body, html').animate({
        scrollTop: scrollPos
    }, animationSpeed);
}

function fiksNavn(element, navn, tmpNavn) {
    var formCtrl = element.parent().controller('form');
    var inputElement = element.find('input');
    if (inputElement) {
        inputElement.attr('name', navn);
    }
    var currentElementCtrl = formCtrl[tmpNavn];
    formCtrl.$removeControl(currentElementCtrl);
    currentElementCtrl.$name = navn;
    formCtrl.$addControl(currentElementCtrl);
}

function verdiErLagretISoknadData(scope, nokkel) {
    if (scope.soknadData && scope.soknadData.fakta[nokkel]) {
        return true;
    }
    return false;
}

function deepClone(obj) {
    return $.extend(true, {}, obj);
}