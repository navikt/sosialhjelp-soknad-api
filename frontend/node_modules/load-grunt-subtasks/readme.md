# load-grunt-subtasks

> Load multiple grunt tasks from subprojects using globbing patterns

Based on the wonderful [load-grunt-tasks](https://github.com/sindresorhus/load-grunt-tasks) by [Sindre Sorhus](http://sindresorhus.com), but rather than loading all of the grunt tasks from the current module, it allows you to load other grunt tasks from inside of `node_modules`.

## Install

Install with [npm](https://npmjs.org/package/load-grunt-subtasks): `npm install --save-dev load-grunt-subtasks`


## Example config

By default, it will load all of the `grunt-*` tasks within `node_modules/*/node_modules`

```js
// Gruntfile.js

module.exports = function (grunt) {
  // load all grunt-* tasks from within the `shared` module
  require('load-grunt-subtasks')(grunt);

  grunt.initConfig({});
  grunt.registerTask('default', []);
}
```


## Usage examples

### Load all grunt tasks

```js
require('load-grunt-subtasks')(grunt);
```

Equivalent to:

```js
require('load-grunt-subtasks')(grunt, {
  pattern: 'grunt-*'
});
```

### Load all grunt-contrib tasks

```js
require('load-grunt-subtasks')(grunt, {
  pattern: 'grunt-contrib-*'
});
```

### Load all grunt-contrib tasks and another non-contrib task

```js
require('load-grunt-subtasks')(grunt, {
  pattern: ['grunt-contrib-*', 'grunt-shell']
});
```

### Load all grunt-contrib tasks excluding one

You can exclude tasks using the negate `!` globbing pattern:

```js
require('load-grunt-subtasks')(grunt, {
  pattern: ['grunt-contrib-*', '!grunt-contrib-coffee']
});
```

### Set a specific base to search for grunt tasks

```js
require('load-grunt-subtasks')(grunt, {
  base: './node_modules/shared/'
});
```

### Set multiple bases to search for grunt tasks

```js
require('load-grunt-subtasks')(grunt, {
  base: ['./node_modules/shared/', './node_modules/devDep']
});
```

### All options in use

```js
require('load-grunt-subtasks')(grunt, {
  pattern: ['grunt-contrib-*', 'lumbar'],
  base: './node_modules/shared/'
});
```

## Options

### pattern

Type: `String|Array`
Default: `'grunt-*'`

By default `grunt-*` will be used as the [globbing pattern](https://github.com/isaacs/minimatch).

### base

Type: `String|Array`
Default: `'./node_modules/*/node_modules'`

Note that `base` will be searched for literally, and not globbed

## License

MIT © [Patrick Kettner](https://github.com/patrickkettner)
