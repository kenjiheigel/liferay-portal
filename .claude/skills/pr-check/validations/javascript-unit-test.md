# JavaScript Unit Tests

## Trigger

Fires when one of these changed:

- JS or TS source with behavior intent (logic added, removed, or modified). Surface-only edits (renames, formatting, comments, JSDoc) do not fire this validation. The build's bundling step is enough.

- A JS-relevant `package.json` key (`dependencies`, `devDependencies`, `scripts.build`, `scripts.test`).

- A lockfile (`package-lock.json`, `yarn.lock`) fires regardless of intent, because a transitive-dependency pin can affect any code path.

## Match

`^modules/.+\.(js|jsx|mjs|cjs|ts|tsx)$|^modules/.+/(package\.json|package-lock\.json|yarn\.lock)$`

## Selection

Run the module's **full Jest suite** for any matched change; do not select individual specs by name. With no `test` script there is no suite, so record that and continue to the sweeps below rather than exiting.

**Cross-module consumer snapshots.** The changed module's own suite does not cover a snapshot stored in a *consumer* module. When the changed module is published as a package, also run affected consumers:

1. Read the changed module's `package.json` `name` (for example `@clayui/*` for clay packages).

1. Grep other modules' `package.json` `dependencies`/`devDependencies` for that name.

1. Run the full suite of each consumer that declares a `test` script.

Cap the consumer set at 8. When more modules depend on the package, note the blast radius rather than running them all.

**Stale stub lists.** A module that stubs a shared package family for Jest keeps its own copy of the stub list, so adding a package to that family leaves every copy stale. For each third-party package the diff adds to the changed module's `dependencies` or imports, take its npm scope and run every module whose `jest-setup.config.js` or `jest-setup.config.ts` mocks a package in that scope. Do not cap this set; only 24 modules define a Jest setup at all.

**Oversized suites.** When a module's suite is large enough to blow the time budget, fall back to the spec named for each changed source (`Foo.test.tsx` for `Foo.tsx`) plus every changed file under the module's `test`, `tests`, or `__tests__` tree, and note the reduced scope in the result.

## Command

Run the full suite:

```bash
(cd <module> && npm test)
```

## Checklist

Add one subitem per affected module:

```
- [ ] <module path>: full suite
```

## Time Estimate

~1 - 5 min per module suite. The oversized-suite fallback caps larger ones.