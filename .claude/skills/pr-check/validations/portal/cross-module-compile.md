# Cross-Module Compile

Compiles the two kinds of consumer of a changed API that no other validation compiles. The first is a module carrying `.lfrbuild-portal-deprecated`, which the default profile leaves out. The second is the `testIntegration` source of a `-test` module the branch did not change. Both depend on the kernel as a binary rather than through `project(...)`, so they are found by searching for the changed type.

## Match

`^portal-impl/src/.+\.java$|^portal-kernel/src/.+\.java$|^modules/.+-api/src/main/.+\.java$|^modules/.+/src/main/.+/[^/]*(Constants|Service|Util)\.java$`

## Preconditions

- Portal Classpath
- Portal Snapshots

## Command

Take the changed files:

```bash
bash "${SKILL_DIR}/select_paths.sh" "${MERGE_BASE}" "${VALIDATION_FILE}"
```

For each changed `.java` file, take its fully qualified type name, the package from its `package` line followed by the file name without `.java`, and list the files that reference it. A file outside that package has to import the type or spell out its fully qualified name, since the repository has no `com.liferay` wildcard imports, so search for that name rather than the simple one. A simple name collides with every unrelated type of the same name: `Test` matches each `import org.junit.Test`, which once put 473 modules in a consumer set whose true size was zero. A file in the same package needs no import, so search those for the simple name:

```bash
(cd "${REPO_ROOT}" && git grep --cached --files-with-matches --fixed-strings --word-regexp '<FullyQualifiedName>' -- 'modules/*.java')
(cd "${REPO_ROOT}" && git grep --all-match --cached --files-with-matches --fixed-strings --word-regexp -e 'package <package>;' -e '<TypeName>' -- 'modules/*.java')
```

Search the index rather than the working tree, since a recursive `command grep` over `modules` descends into `build` and `node_modules`. Every file either search lists, other than the changed file itself, is a consumer file.

Take the modules carrying `.lfrbuild-portal-deprecated` whose `src/main` holds a consumer file:

```bash
printf '%s\n' <consumer file>... \
	| command grep '/src/main/' \
	| bash "${SKILL_DIR}/find_modules.sh" "${MERGE_BASE}" \
	| cut -d " " -f1 \
	| sort --unique \
	| while IFS= read -r module
do
	if [[ -e ${REPO_ROOT}/${module}/.lfrbuild-portal-deprecated ]]
	then
		echo "${module}"
	fi
done \
	| sed "s#^modules/##; s#/#:#g"
```

Take the `-test` modules whose `src/testIntegration` holds a consumer file:

```bash
printf '%s\n' <consumer file>... \
	| command grep '/src/testIntegration/' \
	| bash "${SKILL_DIR}/find_modules.sh" "${MERGE_BASE}" \
	| cut -d " " -f1 \
	| command grep --regexp='-test$' \
	| sed "s#^modules/##; s#/#:#g" \
	| sort --unique
```

Both scans print Gradle project paths, written `<path>` below.

Cap the combined set at 8. When it exceeds the cap, skip the expansion for **every** symbol rather than part of it, and recommend Full Portal Build plus Integration Test Compile instead, since a partial expansion reports on an arbitrary subset while reading as a whole result.

Deprecated consumer (the only profile that includes it):

```bash
("${REPO_ROOT}/gradlew" \
	--project-dir "${REPO_ROOT}/modules" \
	-Dbuild.profile=portal-deprecated \
	:<path>:compileJava \
	--rerun)
```

testIntegration consumer:

```bash
("${REPO_ROOT}/gradlew" \
	--project-dir "${REPO_ROOT}/modules" \
	:<path>:compileTestIntegrationJava \
	--rerun)
```

Keep `--rerun`, which is an option on the compile task and follows the task path. Without it a warm tree reports `UP-TO-DATE` and the build cache restores `FROM-CACHE`, either way a green log in which the compile under test never ran. It forces the one task alone, which is why it is safe where `--no-build-cache` below is not.

A consumer that declares the producer with `project(":...")` resolves it from the working tree, so it compiles against the branch's own change. One that resolves a published artifact instead sees only what is released, and pr-check never fetches a remote, so a producing change that is still in review is invisible to it.

FAIL when a consumer compile reports `BUILD FAILED` naming one of the type names searched for, and report the consumer and that symbol. A failure naming none of them is already broken on the merge base, since this validation compiles consumers the branch did not touch, so report it and do not fail the branch.

A compile that aborts before reaching the consumer of the changed symbol never checked it, so discounting the failure is not the same as clearing the symbol. Two things stop a compile short, and the error count detects neither, since a truncated run and a fatal abort both report a small number:

```bash
command grep --fixed-strings 'only showing the first' "${LOG}"
```

That line is `javac` truncating at its 100 error cap. A fatal abort prints no marker at all, so also treat any `error: cannot access`, annotation processor crash, or run whose output ends without a diagnostic for the file holding the searched symbol as having stopped short.

When either applies, report **NOT VERIFIED** naming the consumer. Otherwise confirm the symbol was actually compiled by recompiling on its own the one file that names it, against the same classpath. A PASS here has to mean the symbol was compiled and was fine, never that the compiler never got to it. Do not look for the consumer's class files as evidence: `javac` skips generation once any error exists, so a failing run emits none whether or not it reached your symbol.

Judge only the `compile` task, since the graph drags in the repository's node and yarn bootstrap and a failure there stops the compile from ever running. Do not pass `--no-build-cache`, which reruns that bootstrap and leaves the tree broken for the next validation.

An empty consumer set is a PASS only when the scans were able to look. Assert the search root before believing an empty result:

```bash
[[ -d ${REPO_ROOT}/modules ]] || exit 1
```

`git grep` exits 1 with no output both for a clean scan and for a pathspec that matches nothing, so a search from the wrong directory is byte for byte what a genuinely clean scan returns.

## Checklist

```
- [ ] (One subitem per consumer, capped at 8:) Compile <module path> (deprecated | testIntegration)
```

## Time Estimate

~1 min per consumer compile.