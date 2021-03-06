# SonarTS [![Build Status](https://travis-ci.org/SonarSource/SonarTS.svg?branch=master)](https://travis-ci.org/SonarSource/SonarTS) [![NPM version](https://badge.fury.io/js/tslint-sonarts.svg)](http://badge.fury.io/js/tslint-sonarts) [![Quality Gate](https://next.sonarqube.com/sonarqube/api/badges/gate?key=sonarts)](https://next.sonarqube.com/sonarqube/dashboard?id=sonarts) [![Coverage](https://next.sonarqube.com/sonarqube/api/badges/measure?key=sonarts&metric=coverage)](https://next.sonarqube.com/sonarqube/component_measures/domain/Coverage?id=sonarts)

Static code analyzer for TypeScript detecting bugs and suspicious patterns in your code.

**Follow us on [twitter](https://twitter.com/sonardash)** <br>

_[To analyze pure JavaScript code, see SonarJS](https://github.com/SonarSource/sonarjs)_

How does it work?

* The [TypeScript compiler](https://github.com/Microsoft/TypeScript/wiki/Using-the-Compiler-API) provides [**AST**](https://en.wikipedia.org/wiki/Abstract_syntax_tree) and **type** information
* On top of it we build the **symbol model** and the [**control flow**](https://en.wikipedia.org/wiki/Control_flow_graph) model
* Some rules are based on AST equivalence (like [no-all-duplicated-branches][`no-all-duplicated-branches`] or [no-identical-expressions][`no-identical-expressions`]).
* We use **[live variable analysis](https://en.wikipedia.org/wiki/Live_variable_analysis)** to detect [dead stores][`no-dead-store`]
* Experimentally, some of the rules are progressively starting to use **[symbolic execution](https://en.wikipedia.org/wiki/Symbolic_execution)** to catch data-flow-related bugs [`no-gratuitous-expressions`]

## Rules

### Bug Detection :bug:

Rules in this category aim to find places in code which has a high chance to be bugs, i.e. don't work as indented. 
Most of the time this is a result of bad copy-paste ([`no-identical-conditions`]) or improvident API usage ([`no-misleading-array-reverse`]).
Some rules are raising issues on unused values ([`no-useless-increment`]), which is at best wasted code and at worst a bug.

* All branches in a conditional structure should not have exactly the same implementation ([`no-all-duplicated-branches`])
* Logical OR should not be used in switch cases ([`no-case-with-or`])
* Collection sizes and array length comparisons should make sense ([`no-collection-size-mischeck`]) ([`requires type-check`])
* Collections elements should not be overwritten unconditionally ([`no-element-overwrite`]) ([`requires type-check`])
* Destructuring patterns should not be empty ([`no-empty-destructuring`])
* Related "if/else if" statements and "cases" in a "switch" should not have the same condition ([`no-identical-conditions`])
* Identical expressions should not be used on both sides of a binary operator ([`no-identical-expressions`])
* Function parameters, caught exceptions and foreach variables' initial values should not be ignored ([`no-ignored-initial-value`]) ([`requires type-check`])
* Return values should not be ignored when function calls don't have any side effects ([`no-ignored-return`]) ([`requires type-check`])
* `Array.reverse` should not be used misleadingly ([`no-misleading-array-reverse`]) ([`requires type-check`])
* Non-existent operators '=+', '=-' and '=!' should not be used ([`no-misspelled-operator`])
* Variables should not be self-assigned ([`no-self-assignment`]) ([`requires type-check`])
* Errors should not be created without being thrown ([`no-unthrown-error`])
* The output of functions that don't return anything should not be used ([`no-use-of-empty-return-value`]) ([`requires type-check`])
* Values should not be uselessly incremented ([`no-useless-increment`])
* Types without members, 'any' and 'never' should not be used in type intersections ([`no-useless-intersection`])

### Code Smell Detection :pig:

Code Smells issues, or Maintainability issues, are raised for places of code which might be costly to change in the future ([`cognitive-complexity`]).
These rules also help to keep the high code quality and readability ([`no-commented-code`], [`no-identical-functions`], [`use-primitive-type`]).
And finally some rules report issues on different suspicious code patters ([`no-dead-store`], [`no-gratuitous-expressions`]).

* Cognitive Complexity of functions should not be too high ([`cognitive-complexity`])
* Functions should not be too complex ([`mccabe-complexity`])
* Getters and setters should access the expected fields ([`no-accessor-field-mismatch`]) ([`requires type-check`])
* `delete` should not be used on arrays ([`no-array-delete`]) ([`requires type-check`])
* Functions should not have too many lines of code ([`no-big-function`])
* Sections of code should not be "commented out" ([`no-commented-code`])
* Dead stores should be removed ([`no-dead-store`]) ([`requires type-check`])
* String literals should not be duplicated ([`no-duplicate-string`])
* Two branches in a conditional structure should not have exactly the same implementation ([`no-duplicated-branches`])
* Nested blocks of code should not be left empty ([`no-empty-nested-blocks`])
* Extra semicolons should be removed ([`no-extra-semicolon`])
* Conditions should not always evaluate to "true" or to "false" ([`no-gratuitous-expressions`])
* Credentials should not be hard-coded ([`no-hardcoded-credentials`])
* Functions should not have identical implementations ([`no-identical-functions`])
* Functions should use "return" consistently ([`no-inconsistent-return`])
* Multiline string literals should not be used ([`no-multiline-string-literals`])
* Increment (++) and decrement (--) operators should not be used in a method call or mixed with other operators in an expression ([`no-nested-incdec`])
* Boolean literals should not be redundant ([`no-redundant-boolean`])
* Redundant pairs of parentheses should be removed ([`no-redundant-parentheses`])
* Primitive return types should be used ([`no-return-type-any`]) ([`requires type-check`])
* Conditionals should start on new lines ([`no-same-line-conditional`])
* "switch" statements should have at least 3 "case" clauses ([`no-small-switch`])
* Statements should be on separate lines ([`no-statements-same-line`])
* Jump statements should not be used unconditionally ([`no-unconditional-jump`])
* Multiline blocks should be enclosed in curly braces ([`no-unenclosed-multiline-block`])
* Array contents should be used ([`no-unused-array`]) ([`requires type-check`])
* Redundant casts and not-null assertions should be avoided ([`no-useless-cast`]) ([`requires type-check`])
* Variables should be declared before they are used ([`no-variable-usage-before-declaration`]) ([`requires type-check`])
* Functions should not have too many parameters ([`parameters-max-number`])
* Local variables should not be declared and then immediately returned or thrown ([`prefer-immediate-return`]) ([`requires type-check`])
* Wrapper objects should not be used for primitive types ([`use-primitive-type`]) ([`requires type-check`])
* Type aliases should be used ([`use-type-alias`]) ([`requires type-check`])

[`cognitive-complexity`]: ./sonarts-core/docs/rules/cognitive-complexity.md
[`mccabe-complexity`]: ./sonarts-core/docs/rules/mccabe-complexity.md
[`no-accessor-field-mismatch`]: ./sonarts-core/docs/rules/no-accessor-field-mismatch.md
[`no-all-duplicated-branches`]: ./sonarts-core/docs/rules/no-all-duplicated-branches.md
[`no-array-delete`]: ./sonarts-core/docs/rules/no-array-delete.md
[`no-big-function`]: ./sonarts-core/docs/rules/no-big-function.md
[`no-case-with-or`]: ./sonarts-core/docs/rules/no-case-with-or.md
[`no-collection-size-mischeck`]: ./sonarts-core/docs/rules/no-collection-size-mischeck.md
[`no-commented-code`]: ./sonarts-core/docs/rules/no-commented-code.md
[`no-dead-store`]: ./sonarts-core/docs/rules/no-dead-store.md
[`no-duplicate-string`]: ./sonarts-core/docs/rules/no-duplicate-string.md
[`no-duplicated-branches`]: ./sonarts-core/docs/rules/no-duplicated-branches.md
[`no-element-overwrite`]: sonarts-core/docs/rules/no-element-overwrite.md
[`no-empty-destructuring`]: ./sonarts-core/docs/rules/no-empty-destructuring.md
[`no-empty-nested-blocks`]: ./sonarts-core/docs/rules/no-empty-nested-blocks.md
[`no-extra-semicolon`]: ./sonarts-core/docs/rules/no-extra-semicolon.md
[`no-gratuitous-expressions`]: ./sonarts-core/docs/rules/no-gratuitous-expressions.md
[`no-hardcoded-credentials`]: ./sonarts-core/docs/rules/no-hardcoded-credentials.md
[`no-identical-conditions`]: ./sonarts-core/docs/rules/no-identical-conditions.md
[`no-identical-expressions`]: ./sonarts-core/docs/rules/no-identical-expressions.md
[`no-identical-functions`]: ./sonarts-core/docs/rules/no-identical-functions.md
[`no-ignored-initial-value`]: ./sonarts-core/docs/rules/no-ignored-initial-value.md
[`no-ignored-return`]: ./sonarts-core/docs/rules/no-ignored-return.md
[`no-inconsistent-return`]: ./sonarts-core/docs/rules/no-inconsistent-return.md
[`no-misleading-array-reverse`]: ./sonarts-core/docs/rules/no-misleading-array-reverse.md
[`no-misspelled-operator`]: ./sonarts-core/docs/rules/no-misspelled-operator.md
[`no-multiline-string-literals`]: ./sonarts-core/docs/rules/no-multiline-string-literals.md
[`no-nested-incdec`]: ./sonarts-core/docs/rules/no-nested-incdec.md
[`no-redundant-boolean`]: ./sonarts-core/docs/rules/no-redundant-boolean.md
[`no-redundant-parentheses`]: ./sonarts-core/docs/rules/no-redundant-parentheses.md
[`no-return-type-any`]: ./sonarts-core/docs/rules/no-return-type-any.md
[`no-same-line-conditional`]: ./sonarts-core/docs/rules/no-same-line-conditional.md
[`no-self-assignment`]: ./sonarts-core/docs/rules/no-self-assignment.md
[`no-small-switch`]: ./sonarts-core/docs/rules/no-small-switch.md
[`no-statements-same-line`]: ./sonarts-core/docs/rules/no-statements-same-line.md
[`no-unconditional-jump`]: ./sonarts-core/docs/rules/no-unconditional-jump.md
[`no-unenclosed-multiline-block`]: ./sonarts-core/docs/rules/no-unenclosed-multiline-block.md
[`no-unthrown-error`]: ./sonarts-core/docs/rules/no-unthrown-error.md
[`no-unused-array`]: ./sonarts-core/docs/rules/no-unused-array.md
[`no-use-of-empty-return-value`]: ./sonarts-core/docs/rules/no-use-of-empty-return-value.md
[`no-useless-cast`]: ./sonarts-core/docs/rules/no-useless-cast.md
[`no-useless-increment`]: ./sonarts-core/docs/rules/no-useless-increment.md
[`no-useless-intersection`]: ./sonarts-core/docs/rules/no-useless-intersection.md
[`no-variable-usage-before-declaration`]: ./sonarts-core/docs/rules/no-variable-usage-before-declaration.md
[`parameters-max-number`]: ./sonarts-core/docs/rules/parameters-max-number.md
[`prefer-immediate-return`]: ./sonarts-core/docs/rules/prefer-immediate-return.md
[`use-primitive-type`]: ./sonarts-core/docs/rules/use-primitive-type.md
[`use-type-alias`]: ./sonarts-core/docs/rules/use-type-alias.md

[`requires type-check`]: https://palantir.github.io/tslint/usage/type-checking/

## Prerequisites

Node.js (>=6.x).

## Use in TSLint

* If you don't have TSLint yet configured for your project follow [these instructions](https://github.com/palantir/tslint#installation--usage).
* Install `tslint-sonarts`

```sh
npm install tslint-sonarts      # install in your project
npm install tslint-sonarts -g   # or install globally
```

* Add `tslint-sonarts` to your `tslint.json` `extends` property:

```javascript
{
  "extends": ["tslint:recommended", "tslint-sonarts"]
}
```

* Some of the rules in SonarTS require type information. So in order to provide as much value as possible run TSLint with **type-checker**, for example:

```
tslint --project ./tsconfig.json 'src/**/*.{ts,tsx}'
```

## Use in SonarQube

SonarTS is available as plugin for SonarQube. [SonarQube](https://www.sonarqube.org/) is an open source platform for continuous inspection of code quality.
Thanks to the platform, SonarTS provides additional features:

* Code coverage import
* Duplication detection
* Various metrics
* More [rules](https://rules.sonarsource.com/typescript)

See the documentation [here](https://docs.sonarqube.org/display/PLUG/SonarTS) and example project [here](https://github.com/SonarSource/SonarTS-example/).

Also available online on :cloud: [SonarCloud](https://sonarcloud.io/)

## Contributing

You want to participate to the development of our TypeScript analyzer?
Have a look at our [contributor](./CONTRIBUTING.md) guide!
