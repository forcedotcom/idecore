Summary
===

Everything here is going to change, hence the word "internal" in the
package name.

Don't rely on it.

Explanation
===

The information we retrieve from the Tooling API's completions?type=apex
is woefully inadequate to do any serious analysis.  I'm going to
leverage the new Apex Compiler (jorje) to do proper type resolution
instead of rewriting it from scratch inside Eclipse.  The only thing
prohibiting me to do so is the "shape" of completions?type=apex is not
compatible with what the new compiler expects.

In future releases, I will change the "shape" of the
completions?type=apex endpoint to support what the new compiler wants.
Then we can take advantage of the proper symbol resolution to determine
the type.

