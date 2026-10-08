# 703: Log and Exception Message Form

The one thing to avoid in a log or exception message is a single complete sentence that ends with a period. The trailing period on a lone sentence is the problem, not the wording.

These are all correct and must not be flagged:

- A phrase or a single clause with no ending period, such as `PageSpeed scan completed`, `Scan is missing a domain`, or `Unable to start the scan`. A missing period here is correct, never a violation.
- A title, such as `Quota Exceeded`.
- Two or more sentences, each ending with a period, such as `Quota exceeded. Try again later.`

Only a lone `This is a sentence.` needs fixing: drop the period to make it a phrase, or add a second sentence.

**Rationale:** A line with no trailing period reads as a label, and two or more sentences read as a paragraph. The single sentence with one trailing period is the awkward middle, so it is the only form to avoid.

Per rule 001, if a class's existing messages already follow a different form, match the class rather than this rule.

The source formatter already reports a lone sentence ending with a period in the message of a thrown exception or a `_log` call in Java, when the message is a literal string or a concatenation of literals and other expressions (`ExceptionMessageCheck` and `LogMessageCheck`), and in the message passed to `_die`, `_error`, `_log`, or `_warn` in a shell script (`SHMessageCheck`). Leave those to it. This rule covers every other log, error, and exception message: shell output such as `echo`, messages in YAML, Groovy, and other files, and a Java message passed to another logger or built with no string literal at all.

A violation is exactly one thing: a single complete sentence that ends with a period, in a message the source formatter does not check. A message without a trailing period is never a violation, even if it reads as a full sentence.