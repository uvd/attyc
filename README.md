# ATTyC

### Pre-Alpha

**A**ngular**T**emplate**Ty**pe**C**hecker is a command line tool that uses typescript to verify angularjs templates. You supply the names and types of the variables that the template uses, and attyc will find any bugs in your template.

*Note*: Attyc isn't intended to conclusively prove that a template is correct, AFAIK this is impossible in angularjs. Its goal is to give you a bit more confidence that the template is correct. If you want absolute confidence that your templates don't contain errors, upgrade to angular 2 and use the AOT compiler.


## Getting Started

To install run `npm install -g attyc`

Attyc needs some metadata about your template to work out the types of template variables. It expects the first comment in the template to contain an [edn](https://github.com/edn-format/edn) vector of maps containing the keys `:name` (variable name), `:type` (variable type) and optionally `:import` (path to import the type from). For example:

```html
<!-- [{:name "ctrl" :type "Controller" :import "./controller"}] -->
```

Once you've added this metadata, run `attyc [glob matching template files]`

## Known Issues

* Attyc works best when you use the "controller as" syntax. If you don't, you'll have to specify every single scope variable you use in the template metadata (ain't nobody got time for that).
* Other than in the `ng-init` and `ng-repeat` attributes, variable declaration is ignored.
* Attyc assumes that if an attribute contains a variable name, then it is an angular expression. This may not always be true.

## Building it yourself

* Clone the repo
* Make sure you have leiningen installed
* Run `lein figwheel`
* In another terminal, run `node dist/main.js`
* Figwheel will rebuild the code whenever it changes

For a production build, run `lein cljsbuild once prod`.