# JSP Compile

Compiles changed JSPs, so a scriptlet typo fails here rather than when Tomcat renders the page. Only modules have the task, so the JSPs in `portal-impl` and `portal-web` are out of scope.

## Match

` modules/.+\.(jsp|jspf)$`

## Command

Take the changed JSPs from the diff:

```bash
MERGE_BASE=$(git merge-base HEAD master)

git diff --name-only "${MERGE_BASE}...HEAD" -- ':/modules/*.jsp' ':/modules/*.jspf'
```

Group them by their owning module (the nearest ancestor with a `bnd.bnd`), and convert each module directory to a Gradle project path by stripping `modules/` and replacing `/` with `:`.

Per affected module:

```bash
"${REPO_ROOT}/gradlew" \
	--parallel \
	--project-dir "${REPO_ROOT}/modules" \
	:<path>:compileJSP
```

`compileJSP` runs in two stages — `generateJSPJava` (JSP → Java) then `JavaCompile` — and surfaces both syntax errors and unresolved method/class references.

FAIL when a module reports `BUILD FAILED`, and report the module and the compiler error. A run that produced neither `BUILD SUCCESSFUL` nor `BUILD FAILED` did not finish, which is also a FAIL — a JSP that was never compiled is not one that compiled cleanly. PASS when every selected module reports `BUILD SUCCESSFUL`.

## Checklist

```
- [ ] JSP compile: <module path>
```

## Time Estimate

~30 sec - 2 min per module.