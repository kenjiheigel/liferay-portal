# Per-Module Compile

## Trigger

A module is in the deploy set AND **Full Portal Build** did not deploy it. The latter holds when:

- **Full Portal Build** did not fire.

- OR **Full Portal Build** fired but the module lacks `.lfrbuild-portal` (so `ant all` did not deploy it).

A module is in the deploy set when it has changed sources or resources — Java, JS (`*.js`, `*.jsx`, `*.mjs`, `*.cjs`), TS, frontend resources (`*.{css,sass,scss}`, `*.ftl`, `*.jsp`, `*.jspf`), lockfiles (`package-lock.json`, `yarn.lock`), module `*.properties`, or OSGi configuration (`bnd.bnd`, `gradle.properties`, `package.json` keys other than `test`).

Exclude modules whose **only** Java change is under `src/testIntegration`. Integration Test Compile already runs `compileTestIntegrationJava` for those, and `-test` modules do not deploy a runtime bundle — `gradlew :path:deploy` would be redundant. A diff that touches `src/testIntegration` *and* anything else in the same module still puts the module in the deploy set.

Expand by consumers: for each changed module with an added, removed, or changed `public`/`protected` member — a method signature or a field/constant declaration (`^[-+]\s*(public|protected)\b`) — grep `modules/**/build.gradle` for `project(":<changed-module-path>")` and add every consumer to the deploy set. This applies to any module, not only `*-api`. The deploy set size N is used by [full-portal-build.md](full-portal-build.md)'s cost comparison.

Expand by shared build tooling: `modules/frontend-sdk/**` and `modules/node-scripts.config.js` feed every module's JavaScript build, so a change to either breaks deploys the diff never touched, and no `project(":...")` edge leads to them.

Add the modules whose `imports` array in `modules/node-scripts.config.js` is non-empty. Their builds run the import and export bridges. Strip the `@liferay/` prefix from each name and resolve it to a module directory under `modules/apps` or `modules/dxp/apps`. Reading the set from the config keeps it current.

Consumers the project graph cannot reach — archived, `portal-kernel`/`portal-impl`, and `testIntegration`-only consumers — are covered by [cross-module-compile.md](cross-module-compile.md).

Both behavior-change and surface-only edits fire this validation — the build verifies compile and resource bundling regardless of intent.

## Match

`^modules/.+\.(java|js|jsx|mjs|cjs|ts|tsx|css|scss|sass|ftl|jsp|jspf|properties)$|^modules/.+/(bnd\.bnd|gradle\.properties|package-lock\.json|yarn\.lock|package\.json)$`

## Command

Set up once, then deploy each module:

```bash
(cd "${REPO_ROOT}" && ant compile install-portal-snapshots)
```

The setup step is a precondition: it rebuilds the `portal-kernel`/`portal-impl` snapshot from the branch tree before any module compiles, so a module referencing a portal-core symbol is checked against the branch's kernel rather than a stale snapshot. A kernel change from a separate, not-yet-merged PR is only caught once local `master` includes it, since pr-check never fetches a remote.

```bash
("${REPO_ROOT}/gradlew" \
	--parallel \
	--project-dir "${REPO_ROOT}/modules" \
	:<path>:deploy)
```

## Checklist

```
- [ ] Setup: ant compile install-portal-snapshots
- [ ] (One subitem per deploy-set module:) Deploy <module path>
```

## Time Estimate

3 min setup + 1 min × ⌈N/4⌉.

A shared build tooling change puts 16 modules in the deploy set on its own, so budget about 4 min for the fan-out.