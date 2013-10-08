/**
 * Used to simulate key down events
 * @param el
 * @constructor
 */
var KeyPresser = function(el) {
    this.el = el;
};

KeyPresser.prototype.pressKey = function(keyCode) {
    var dfd = $.Deferred();
    this.el.trigger($.Event('keydown', {which: keyCode}));
    // Waits 1 ms before it sets the keydown as resolved (To allow events to propagate);
    setTimeout(dfd.resolve, 5);
    return dfd.promise();
};

KeyPresser.prototype.pressArrowDown = function() {
    return this.pressKey(40);
};

KeyPresser.prototype.pressArrowUp = function() {
    return this.pressKey(38);
};

KeyPresser.prototype.pressArrowLeft = function() {
    return this.pressKey(37);
};

KeyPresser.prototype.pressArrowRight = function() {
    return this.pressKey(39);
};

KeyPresser.prototype.pressESC = function() {
    return this.pressKey(27);
};