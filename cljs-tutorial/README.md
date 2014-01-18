# cljs-tutorial

A port for Emacs users of [LightTable ClojureScript Tutorial][1] by
[David Nolen][2] based on [cljs-start][3] lein-template.

## Prerequisites

The same as [cljs-start][3] + Emacs:

* JDK >= 6.0 (7.0 it's better)
* Leiningen >= 2.3.4
* PhantomJS >= 1.9.1
* Emacs >= 24.x with [cider][4] package installed

    > NOTE: I did not test `cljs-tutorial` with others editors/IDE but
    > you should be able to replicate the same interactive experience
    > with any editor/IDE able to talk with nREPL.

## Quickstart

Clone the `modern-cljs` repo:

```bash
git clone https://github.com/magomimmo/modern-cljs.git
```

Open the `core.cljs` file from the
`cljs-tutorial/src/cljs/cljs_tutorial` directory in your Emacs and
then issue the `C-c M-j` shortcut which corresponds to the `M-x
cider-jack-in` command.

Be patient because the command needs to download all the libs included
in the project, to compile the ClojureScript code and finally to
launch both the nREPL server and client components.

When the command completes, it shows you the Clojure nREPL prompt in a
new Emacs Buffer:

```clj
; CIDER 0.5.0alpha (package: 20131210.726) (Clojure 1.5.1, nREPL 0.2.3)
user> 
```

Run the ClojureScript nREPL against the phantomjs JavaScript runtime
(PhantomJS Connected REPL) . If you receive more WARNINGs about Symbol
ISomething not being a protocol, don't care about them because they do
not compromise the running of the tutorial.

```clj
user> (cemerick.austin.repls/exec) ; to run the PhantomJS Connected REPL
Browser-REPL ready @ http://localhost:51631/1017/repl/start
WARNING: Symbol ILookup is not a protocol at line 940 /Users/mimmo/Developer/modern-cljs/cljs-tutorial/src/cljs/cljs_tutorial/core.cljs
...
WARNING: Symbol IMap is not a protocol at line 1108 /Users/mimmo/Developer/modern-cljs/cljs-tutorial/src/cljs/cljs_tutorial/core.cljs
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user> 
```

Now all you have to do is to evaluate form by form the `core.cljs`
file by positioning the cursor after the form and typing the usual
`C-c C-e` shortcut.

# ATTENTION NOTE

The tutorial source code contains references to JS entities not
available in the phantomjs JavaScript runtime. In such a case if you
evaluate any form including them you'll get an error.

If it disturbs you, just use the Browser Connected REPL instead of the
PhantomJS Connected REPL as follows:

```clj
user> (run) ; to run a local web-server based on ring/compojure/enlive libs
#<Server org.eclipse.jetty.server.Server@76e788e1>
user> (browser-repl) ; to run the Browser Connected REPL
Browser-REPL ready @ http://localhost:51665/4760/repl/start
WARNING: Symbol ILookup is not a protocol at line 940 /Users/mimmo/Developer/modern-cljs/cljs-tutorial/src/cljs/cljs_tutorial/core.cljs
...
WARNING: Symbol IMap is not a protocol at line 1108 /Users/mimmo/Developer/modern-cljs/cljs-tutorial/src/cljs/cljs_tutorial/core.cljs
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user> 
```

Now visit the `http://localhost:3000` to activate the Browser
Connected REPL and evaluate form by form the tutorial code from the
`core.cljs` source file by typing `C-c C-e` shortcut after the closing
parens of each form.

Enjoy.

## License

Copyright © 2014 Giacomo (Mimmo) Cosenza aka @magomimmo.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: https://github.com/swannodette/lt-cljs-tutorial
[2]: https://github.com/swannodette
[3]: https://github.com/magomimmo/cljs-start
[4]: https://github.com/clojure-emacs/cider

