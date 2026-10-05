# Testing Changes to PR Check

A change to this skill is prose, so reading it proves nothing. Test it by running the text against changes planted for the purpose on a branch that is never pushed. Judge the result from git and from the logs rather than from the summary a run prints.

## Selection

When a change touches a `## Match` section, the routing, the list in `SKILL.md`, or `select_paths.sh`, resolve the paths of recent commits on the base branch and select from them twice, once with the rules on the base branch and once with the change, and account for every validation and path that differs. Include a path the change must select and one it must not, so that a broken comparison reads as a disagreement rather than as agreement. Running `select_paths.sh` on a single validation is the quickest check of what it selects, and piping a single path into `find_modules.sh` the quickest check of its module.

## Commands

When a change touches a `## Command` or `## Autocommit` section, extract exactly that section, as the runner hands it to a subagent, and confirm the extract carries the change and nothing from the rest of the file. Set `${SKILL_DIR}`, `${MERGE_BASE}`, and `${VALIDATION_FILE}` the way the runner does, since a **Command** builds its paths, modules, and projects with `select_paths.sh` and has nothing to work on without them. Give the new text and the text on `master` to two subagents that know nothing about the change, with the same planted diff, and ask each for its verdict and the sentence that decided it. A change that works splits the verdicts. Identical verdicts mean the change made no difference where it is read.

When a command reimplements a rule another tool enforces, run both against the same planted cases and require them to agree, including a case that must fail on both sides. A command that crashes prints nothing, which reads as a pass, so check its exit status as well as its output.

Run every shell command in a validation under both zsh and bash. The Bash tool runs zsh on macOS, and zsh does not split an unquoted variable or command substitution into words, so under zsh a command that builds its arguments in a variable passes them to the tool as a single argument.

## Whole Runs

Run `/pr-check` from the root of a worktree that holds the planted changes, for example with `claude -p "/pr-check"`. A headless session cannot answer the question Pass 1 asks when the estimate exceeds 20 minutes, so say in the prompt to proceed without asking. Plant one change for each outcome to prove, each in its own commit, and check the verdicts against git, for example against the `<TICKET> SF` commit a formatter makes.

A branch in this repository always carries the rule change in its own diff, so a test run here also reports on the `.claude` files the change touched. That is expected.

To test the rules as the private repository runs them, use a worktree of `liferay-portal-ee` on `master-private` whose `pr-check` stub fetches from the local branch holding the change rather than from `upstream master`, by replacing its fetch with:

```bash
git fetch <liferay-portal checkout> <branch>
```