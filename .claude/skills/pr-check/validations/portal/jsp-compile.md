# JSP Compile

Compiles changed JSPs, so a scriptlet typo fails here rather than when Tomcat renders the page. Only modules have the task, so the JSPs in `portal-impl` and `portal-web` are out of scope.

## Match

` modules/.+\.(jsp|jspf)$`

## Command

Take the Gradle project paths of the changed JSPs' modules from the work list:

```bash
cut -d " " -f1 "${WORK_LIST}" | sed "s#^modules/##; s#/#:#g" | sort --unique
```

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