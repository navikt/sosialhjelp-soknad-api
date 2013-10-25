if (!Array.prototype.last) {
    Array.prototype.last = function() {
        return this[this.length -1];
    }
}

function checkTrue(element) {
    if (element == undefined) {
        return false;
    }
    return element.toString() == 'true';
}