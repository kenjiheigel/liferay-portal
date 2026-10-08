# Cross-Module Compile

Compiles two kinds of consumer of a changed module, neither of which any other validation compiles. The first is the `testIntegration` source of a `-test` module. The second is a module carrying `.lfrbuild-portal-deprecated`, which the default profile leaves out. `ant all` compiles neither, so this runs whatever **Full Portal Build** returns.

## Match

`^(modules|portal-impl|portal-kernel)/.+\.java$`

## Command

Run every command below from `${REPO_ROOT}`. `git grep` searches from the current directory down, so from anywhere else the sweeps silently narrow to a subtree.

### Consumers

Find consumers by two routes and take their union. A module both routes find is compiled once.

**By module.** Take the changed modules:

```bash
bash "${SKILL_DIR}/select_paths.sh" "${MERGE_BASE}" "${VALIDATION_FILE}" \
	| bash "${SKILL_DIR}/find_modules.sh" "${MERGE_BASE}" \
	| cut -d " " -f1 \
	| command grep '^modules/' \
	| sort --unique
```

For each changed module, take every module under the same parent directory whose name ends in `-test` and which has a `src/testIntegration` tree, so `apps:blogs:blogs-api` brings in `apps:blogs:blogs-test`. Add every `-test` module whose `build.gradle` declares the changed module, where `<path>` is the changed module's Gradle project path:

```bash
git grep --cached --files-with-matches --fixed-strings 'project(":<path>")' -- '*.gradle' \
	| bash "${SKILL_DIR}/find_modules.sh" "${MERGE_BASE}" \
	| cut -d " " -f1
```

**By type.** For each changed `.java` file that sits under `portal-impl`, `portal-kernel`, or a module whose name ends in `-api`, and for each changed file named `*Constants.java`, `*Service.java`, or `*Util.java`, take its simple type name and find the files that name it:

```bash
git grep --cached --files-with-matches --word-regexp '<TypeName>' -- ':(glob)modules/**/src/testIntegration/**/*.java'
```

```bash
git ls-files 'modules/**/.lfrbuild-portal-deprecated' \
	| while IFS= read -r marker
	do
		git grep --cached --files-with-matches --word-regexp '<TypeName>' -- ":(glob)$(dirname "${marker}")/src/main/**/*.java"
	done
```

Map each file to its module with `find_modules.sh`. From the first search, keep only modules whose name ends in `-test`.

Leave out `modules/dxp/apps/saml/saml-admin-rest-test` and every module under `modules/sdk`. Convert each module to its Gradle project path with `sed "s#^modules/##; s#/#:#g"`, and keep two sorted lists: the `testIntegration` consumers and the deprecated consumers.

When both lists are empty, compile nothing and report **NOT VERIFIED**, naming the changed modules as having no integration test consumer.

### Compile

Compile all the `testIntegration` consumers in one Gradle run, never one run per module. Gradle schedules the tasks in parallel itself, and every separate run pays the configuration cost again. Write the consumers' project paths, one per line, to `${CONSUMERS_FILE}`, such as `${LOG_DIR}/consumers.txt`:

```bash
TASKS=()

while IFS= read -r project_path
do
	TASKS+=(":${project_path}:compileTestIntegrationJava" --rerun)
done < "${CONSUMERS_FILE}"

("${REPO_ROOT}/gradlew" \
	--continue \
	--parallel \
	--project-dir "${REPO_ROOT}/modules" \
	"${TASKS[@]}")
```

Compile the deprecated consumers in a second run, since they exist only under their own profile and that profile leaves out the `-test` modules. Build `${TASKS}` the same way with `:compileJava` in place of `:compileTestIntegrationJava`, then:

```bash
("${REPO_ROOT}/gradlew" \
	--continue \
	--parallel \
	--project-dir "${REPO_ROOT}/modules" \
	-Dbuild.profile=portal-deprecated \
	"${TASKS[@]}")
```

Put `--rerun` after every task path, since it is an option on the task before it and forces only that one. Without it a warm tree prints `UP-TO-DATE` and the build cache restores `FROM-CACHE`, either way a green log in which the compile under test never ran. Do not pass `--no-build-cache`, which reruns the node and yarn bootstrap and leaves the tree broken for the next validation.

Count the executed compile lines and check the count against the list:

```bash
command grep --count --extended-regexp '^> Task :.*:compileTestIntegrationJava$' "${LOG}"
```

A consumer whose task line is missing, or ends in `UP-TO-DATE` or `FROM-CACHE`, was not compiled, so name it and report **NOT VERIFIED** for it.

### Verdict

FAIL when a compile reports an error naming something the diff changed, either a changed type or a file in a changed module, and name the consumer and the error.

An error that names nothing the diff changed is in code the branch did not touch, so it either predates the branch or comes from the environment. Report **NOT VERIFIED** for that consumer and name the error. A common shape is `package com.liferay.portal.kernel.model does not exist` for a package that plainly exists in the tree, which is a broken snapshot rather than the branch.

A compile can stop short of checking every consumer in two ways. `javac` stops at 100 errors and prints the line below, while a fatal abort, such as `error: cannot access` or an annotation processor crash, prints nothing comparable:

```bash
command grep --fixed-strings 'only showing the first' "${LOG}"
```

When either happened in a consumer, report **NOT VERIFIED** naming it.

PASS when every consumer on both lists printed an executed compile line and the build reported `BUILD SUCCESSFUL`.

## Checklist

```
- [ ] Compile testIntegration: <count> consumers in one run
- [ ] Compile deprecated: <count> consumers in one run
```

## Time Estimate

Under 1 min for a few dozen consumers on a warm tree, and about 4 min for every `testIntegration` module in the repository, or 11 min cold. Add about 1 min when there are deprecated consumers.