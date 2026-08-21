# DS Reference Satisfiability

## Trigger

The diff either declares a component that registers no service, or adds a reference to one.

`@Component(service = {})` registers under no service type, so nothing can look the class up. Any `@Reference` to it stays unsatisfied and the *referencing* component silently never activates, with no compile error — only the log assertor catches it, after deployment. LPD-97011 shipped four such references.

## Match

`\.java$`

## Command

Static, no build. Use `command grep` for working-tree scans, since the shell's `grep` wrapper can redirect them.

Collect the risky class names from the diff, in both directions the defect arrives from:

- A changed `.java` file declaring `@Component` with `service = {}`. Its class name is the filename.

- A field the diff adds, declared `<visibility> <Type> _<name>;`, whose `*/<Type>.java` declares `@Component` with `service = {}`.

Then list every `@Reference` in the repository and keep those whose declared field type is exactly a collected class, since matching as a substring makes a collected `Foo` match `FooImpl`:

```bash
git grep --after-context=30 --fixed-strings -e '@Reference' -- '*.java'
```

Take the field declaration that follows each annotation rather than a fixed offset from it. An annotation carrying `policy`, `policyOption`, or `target` pushes its declaration well down the window.

Report each as the unsatisfiable class with the file and field referencing it. Printing nothing passes.

## Autocommit

None. Registering the class (`service = <Class>.class`) or dropping the reference is a design choice, not a repair a check should make.

## Time Estimate

~10-20 sec, mostly the repository-wide `@Reference` scan.