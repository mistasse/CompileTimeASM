CompileTimeASM
==============
This post-compilation processor decompiles classes you give it a path for as arguments, and looks for known annotations.

At the moment, the processor has 3 purposes:

- Allow tail recursion in for a method
- Rename a method to be used as a dollar, or any name else
- Modify the min required JRE version for loading this class file

The annotation also disappear from the source code once the class recompiled. So if the retention is on "RUNTIME", it's only for a short time.
