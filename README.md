# srepl

A tool that patches the repl to allow the (source) macro to be called on
values that are defined at the repl.

## Usage

Make sure the generated jar is on your classpath. If you build an Uberjar
then the Clojure runtime will already be included.

Launch as you'd launch a normal repl, but using srepl.core instead of clojure.main.

```bash
$ java -cp srepl-0.1.0-SNAPSHOT-standalone.jar:/path/to/jline-0.9.5.jar jline.ConsoleRunner srepl.core $*
```

```clojure
user=> (defn plus [a b] (+ a b))
#'user/plus
user=> (source plus)
(defn plus [a b] (+ a b))
nil
```

The s-expression is attached to the var as metadata with the key src. So the following works:

```clojure
user=> (-> plus meta :src eval (apply [2 3]))
5
```

Not that you'd ever do that.

## Future
This would be much smaller if it were in the repl already.

Also, it'd be nice to have the option of having the evaluator attach s-expressions to Vars.
However, this requires changes to the Compiler class.

## License

Copyright © 2012 Paula Gearon

Distributed under the Eclipse Public License, the same as Clojure.
