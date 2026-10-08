# 202: Sort Sortable Sequences Alphabetically

When a sequence of sibling items has no order imposed by dependencies or logic, sort it alphabetically. This applies to consecutive method calls on the same object, sibling declarations such as fields, methods, and functions, and lists of string or key literals. The source formatter already sorts two of these sequences, so leave them to it: `ParameterOrderCheck` sorts the parameters of a method or constructor, and `ConsecutiveMethodCallsOrderCheck` sorts a contiguous block of calls to different setters on one variable by method name. The Liferay source formatter sorts variable, field, and method names case insensitively in natural order (see `JavaTermComparator` in `modules/util/source-formatter`), so `_criterions` sorts before `_criterionType` because case is ignored. Literal strings sort case sensitively in ASCII order, with spaces and digits before letters, uppercase letters before lowercase, and `null` last. A builder chain sorts too: the calls between the chain's opening call and its terminal `build()` are alphabetical. Calls to the same setter on one object sort by argument.

Several constraints override the default sort. Setter calls on a generated model mirror the defining schema rather than sort alphabetically: a ServiceBuilder entity follows the column order in its `service.xml`, and a REST DTO follows the field order in its `rest-openapi.yaml`; the source formatter's `JavaServiceObjectCheck` rewrites an entity setter block into that order, so alphabetizing it by hand is reverted on the next run. An entry that must come first or last — an initialization step, a default, an "all" option — is separated from the sorted block by a blank line, so the reader knows the leading or trailing position is intentional and the rest of the order carries no further meaning. In a constructor body, assignments from constructor parameters come first and follow the parameter order, with any derived assignments after them (see `JavaConstructorParametersCheck` in the same source formatter directory). A group of functions or methods first orders public before private, then sorts alphabetically within each group, which the source formatter's `SHFunctionOrderCheck` enforces for shell scripts.

**Rationale:** A sequence in arbitrary order forces the reader to scan all of it to find one item and scatters new items into random positions, which makes diffs noisier. One predictable order lets any item be found at a glance and gives every addition an obvious place. Sorting yields to a real constraint: when one item depends on, or must run before, another, that order wins over alphabetical order.

A violation is a sequence of sibling, order independent items left in an order that is not alphabetical — getter calls on one object, calls to the same setter on one object, sibling declarations, or the entries of a literal list. Do not report the order of a parameter list or of a block of different setters on one variable; the source formatter reports those.

**Example:** commit `19d4dbf` alphabetized shell functions; `d461c0b` fixed a string list to the case sensitive order `" 1 "`, `"0"`, `"0, 1"`, `"true"`, `null`; `f264e2c` sorted a test method into its alphabetical position among its siblings. The diff below sorts a builder chain.

```diff
 Foo foo = builder.start(
 	arg
-).gamma(
-	gammaArg
 ).alpha(
 	alphaArg
 ).beta(
 	betaArg
+).gamma(
+	gammaArg
 ).build();
```