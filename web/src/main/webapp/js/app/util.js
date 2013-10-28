if (!Array.prototype.last) {
    Array.prototype.last = function() {
        return this[this.length -1];
    }
}

(function($) {
    $.fn.changeElementType = function(newType) {
        var attrs = {};

        $.each(this[0].attributes, function(idx, attr) {
            attrs[attr.nodeName] = attr.nodeValue;
        });

        this.replaceWith(function() {
            return $('<' + newType + '>', attrs).append($(this).contents());
        });
    }
})(jQuery);


function checkTrue(element) {
    if (element == undefined) {
        return false;
    }
    return element.toString() == 'true';
}