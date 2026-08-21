# JAXRS Resource Interfaces

## Trigger

The diff changes a hand-written `*ResourceImpl` under a REST Builder `resource/v<major>_<minor>` package. Keep that path scope: a `*ResourceImpl` outside such a package is usually a Service Builder model, where sharing an interface with the generated base is normal.

REST Builder generates `Base<Tag>ResourceImpl` with the interfaces the resource needs and scaffolds `<Tag>ResourceImpl` to extend it. Redeclaring one of them on the subclass changes how the service is registered. `JAXRSResourceTest.testInterfaces` rejects it, but only in CI's integration batch against a deployed portal, which is why LPD-100589 reached master.

## Match

`/resource/v[0-9]+_[0-9]+/.*ResourceImpl\.java$`

## Command

Static, no build. For each changed `*ResourceImpl.java` under a `resource/v<major>_<minor>` package:

1. Skip it when the class name starts with `Base`, or when the file carries `@generated`.

1. Read its `implements` clause and that of `Base<ClassName>.java` in the same directory, since that base name repeats across modules. Skip the file when no such base is there. Take the whole class declaration, from `public class` or `public abstract class` through the opening brace, since the generated clause wraps over several lines.

1. Compare raw type names, stripping type arguments.

Report any interface present in both, naming the class, the interface, and the base. Printing nothing passes.

## Autocommit

None. Dropping the interface also drops its import, and the developer should confirm the resource still registers as intended.

## Time Estimate

~10-20 sec (static, no build).