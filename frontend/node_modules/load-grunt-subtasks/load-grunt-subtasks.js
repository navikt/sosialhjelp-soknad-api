'use strict';
var globule = require('globule');

function arrayify(el) {
  return Array.isArray(el) ? el : [el];
}

module.exports = function (grunt, options) {
  options = options || {};

  var patterns = arrayify(options.pattern || ['grunt-*']);
  var bases = arrayify(options.base || globule.find(['./node_modules/*/node_modules'], {filter: 'isDirectory', prefixBase: true}));
  var tasks = [];

  patterns.push('grunt-*');

  patterns = patterns.map(function(pattern) {
    return pattern + '/tasks/**';
  });

  patterns.push('!grunt', '!grunt-cli');

  bases.forEach(function(base) {
    tasks = tasks.concat(globule.find(patterns, {srcBase: base, prefixBase: true, filter: 'isFile'}));
  });

  tasks.forEach(function(task) {
    task = '../../' + task;
    require(task)(grunt);
  });
};
