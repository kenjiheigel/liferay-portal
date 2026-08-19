# Module Registration

## Trigger

A `.lfrbuild-portal` or `.lfrbuild-ci` marker is added or removed. Each enrolls a module in a build that never saw it before, and a marker-only diff touches no source, so [per-module-compile.md](per-module-compile.md) does not fire.

`.lfrbuild-portal` marks a module for `ant all`. The build discovers modules by scanning for these markers at configuration time, so a newly marked module's `project(":...")` dependencies resolve for the first time — a reference to an unmarked module fails with `Project with path ':...' could not be found`. The marker alone suffices; no `ant setup-sdk` first.

`.lfrbuild-ci` adds a `:<path>:deploy` task to the `marker.files.lfrbuild.ci.enabled` pass in [build.xml](../../../../build.xml). The module is built and deployed rather than merely configured, so one that configures cleanly but fails to compile still aborts the bundle build.

## Match

`(^|/)\.lfrbuild-(ci|portal(-private|-public)?)$`

## Command

For each added or removed marker, convert its module directory to a Gradle project path (strip `modules/`, `/` to `:`, prefix `:` — `modules/apps/mcp/mcp-server-rest-test` becomes `:apps:mcp:mcp-server-rest-test`), then run the task that matches the marker.

For `.lfrbuild-portal*`, force configuration:

```bash
("${REPO_ROOT}/gradlew" \
	--project-dir "${REPO_ROOT}/modules" \
	:<path>:help)
```

`help` evaluates the target's `build.gradle` without compiling, so a dangling `project(":...")` fails at configuration. Include `-test` modules — no other validation configures them.

For `.lfrbuild-ci`, deploy, because that is what the marker makes the build do:

```bash
("${REPO_ROOT}/gradlew" \
	--project-dir "${REPO_ROOT}/modules" \
	:<path>:deploy)
```

Take the markers from the diff rather than from a `find`. Liferay npm packages carry a copy of the module directory with its marker, so a scan of the working tree turns up `.lfrbuild-ci` paths that are not modules — `build.xml`'s `<dirset>` excludes `**/node_modules/**` for that reason.

## Checklist

```
- [ ] (One subitem per added/removed marker:) Configure or deploy <module path>
```

## Time Estimate

~10-20 sec per `.lfrbuild-portal*` marker (configuration only). ~1 min per `.lfrbuild-ci` marker (deploy).