# Tutorial 8 - Learn by contributing

In this tutorial we're going to learn more about CLJS by trying to
patch it for solving the problem we found in the [last tutorial][1].

> NOTE 1: The content of this tutorial is not specifically dedicated
> to CLJS. If you're not interested in understanding how to patch the
> CLJS compiler you can safely jump to a subsequent tutorial.

## Introduction

Let's recap the problem we met in the [previous tutorial][1]. We
declared three different project builds, namely `:dev`, `:pre-prod`
and `:prod`, corresponding to three different compilation mode, namely
`:whitespace`, `:simple` and `:advanced`. Each build got the CLJS
source code to be compiled from the same source directory configured
by setting `:source-path` option in the corresponding section of the
options of the build.

We then considered that having an active brepl connection in the
production build could expose our application to security weakness and
we did not find any better way to exclude the brepl connection than
duplicate the entire code base, but the file cointaining the
connection iteself (i.e. "modern_cljs/connect.clj").

This code duplication is going to create a maintenance nigthmare we'd
like to avoid by trying to patch `lein-cljsbuild`. Where we have to
start from?

## Top-down approach

By flying at 10 thousand feet from ground, we could consider the
`lein-cljsbuild` plugin as a very nice interface to drive the
underlying CLJS compiler. Solving the code duplication problem means
to patch the underlying CLJS compiler and to substitute in
`lein-cljsbuild` the original CLJS compiler with the patched one.

The wiki of `lein-cljsbuild` [documents][3] how to set up a project to
enable the use of an arbitrary version of the CLJS compiler.

We need to:
* create a directory in the project home
* checkout a CLJS compiler in that directory
* configure `project.clj` to use that CLJS compiler

### Create a new directory in the project home

```bash
$ cd path/to/modern-cljs # cd in the project home directory
$ mkdir compiler
$ cd compiler
```

### Checkout a CLJS compiler in the new directory

```bash
$ git clone git://github.com/clojure/clojurescript.git
$ cd clojurescript
$ ./script/bootstrap
```

### Configure project.clj

Here is the interested code snippet of `project.clj`.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"
                 "compiler/clojurescript/src/clj"
                 "compiler/clojurescript/src/cljs"]
  ...
  ...)
```

Notice that we added both `clj` and `cljs` source code directories to
lein `:source-paths` option.

### Sanity check

To verify that everything is still working, do as follows:

```bash
$ cd path/to/modern-cljs # cd in the project home directory
$ lein cljsbuild clean # delete any previous CLJS compilation
$ lein cljsbuild once # launch all builds using the selected CLJS compiler
$ lein ring server
$ lein trampoline cljsbuild repl-listen # from project home in a new terminal
```

You can now visit `http://localhost:3000/login-dbg.html" to verify
that the brepl is stil working as expected.

> NOTE 2: you should receive a lot of wornings during the cljsbuild
> compilation of domina. Don't warry about them.

## Requirements specification

Before proceeding with any patch, we need to specity the requirements to
be satisfied for solving our code duplication problem. Let's go back to
the portion of `project.clj` where we declared each build.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...

  :cljsbuild {:builds
              {
               :dev
               {:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/modern_dbg.js"
                           :optimizations :whitespace
                           :pretty-print true}}
               :prod
               {:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/modern.js"
                           :optimizations :advanced}}
               :pre-prod
               {:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/modern_pre.js"
                           :optimizations :simple
                       ...}}}})
```

Now imagine for a moment to have an available option to exclude a CLJS
source file from a build. Ask yourself in which section of the build
you would like to configure it. There are few alternatives.

We could set an `:exclude` option at the same hierarchical level of
the `:source-path` option.

```clojure
            :prod
               {:source-path "src/cljs"
                :exclude "modern_cljs/connect.clj"
                :compiler {:output-to "resources/public/js/modern.js"
                           :optimizations :advanced}}
```

Or we could set the `:exclude` option at the same hierarchical level of
the `:output-to` option.

```clojure
            :prod
               {:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/modern.js"
                           :exclude "modern_cljs/connect.clj"
                           :optimizations :advanced}}
```

> NOTE 3: Even if I personally consider the former alternative more
> ergonomic, we're going to choose the latter because, as we'll see
> later, it requires to patch CLJS compiler only

Are we sure that in the future we would always need to exclude one
file only from compilation? I'm not sure at all. We're not going to
pay just for asking and we could then be more demanding. Pretend that
we could exclude a list of source files or even a list of source
files and source directories like in the following hypothetical
sample.

```clojure
            :prod
               {:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/modern.js"
                           :exclude ["modern_cljs/connect.cljs"
                                                             "modern_cljs/exclude_dir"]
                           :optimizations :advanced}}
```

Starting from an accurate requirement, we can now think on how to
connect our envisioned `:exclude` compilation option with the
underlying CLJS compiler.

## CLJS compiler

In the CLJS [Quick Start][2] guide you can read the following

> cljsc is convenient when a command-line tool is required or when a file
> or project only needs to be compiled once. While developing, it is much
> faster to use the build function from the Clojure (not ClojureScript)
> REPL:

```clojure
(require '[cljs.closure :as cljsc])
(doc cljsc/build)
-------------------------
cljs.closure/build
([source opts])
  Given a source which can be compiled, produce runnable JavaScript.
```

It seems that if we want to patch the CLJS compiler, the `build`
function defined in the `cljs.closure` namespace is the first one we
should take care of. Indeed, by observing the following `build` call
sample, it can be seen that it accepts a CLJS source and an options
map, the same options map we already met in `lein-cljsbuild`.

```clojure
(cljsc/build "hello.cljs" {:optimizations :advanced :output-to "hello.js"})
```

Take a look at the `build` function definition to see where the
source compilation really happens. Here is the interested code snippet
from  `compiler/clojurescript/src/clj/cljs/closure.clj`.


```clojure
(defn build
  "Given a source which can be compiled, produce runnable JavaScript."
  [source opts]
  ...
  ...
  (let [...
        ...]
        (let [compiled (-compile source all-opts)
              ...]
                                 ....)
```

### compile-dir

The private `compile` function behaviour, which is responsable for the
CLJS compilation, depends on the `source` argument type, as we can
verify by looking at the following extensions of `Compilable` protocol
defined in the same file

```clojure
(extend-protocol Compilable

  File
  (-compile [this opts]
    (if (.isDirectory this)
      (compile-dir this opts)
      (compile-file this opts)))
  ...
  ...)
```

`compile-dir`, called when `source` argument is a directory, is the
next function we need to follow the definition of.

```clojure
(defn compile-dir
  "Recursively compile all cljs files under the given source
  directory. Return a list of JavaScriptFiles."
  [^File src-dir opts]
  (let [out-dir (output-directory opts)]
    (map compiled-file
         (comp/compile-root src-dir out-dir))))
```

`compile-dir` accepts as arguments a source directory and a map of
compilation options. It next calls `compile-root`, defined in the
`compiler.clj`, by passing it the received source directory and the
output directory extracted from the received option maps (defaulted to
"out" directory).

`compile-dir` is the first function we need to patch to support the
envisioned `:exclude` compilation option. We have to change its
implementation by passing the value of `:exclude` keyword as third
argument to the internally called `compile-root` function. Here is the
updated definition of `compile-dir` that has to be substituted to the
original one in `closure.clj` which resides in the
`compiler/clojurescript/src/clj` directory.

```clojure
(defn compile-dir
  "Recursively compile all cljs files, but the excluded ones,
  under the given source directory. Return a list of JavaScriptFiles."
  [^File src-dir opts]
  (let [out-dir (output-directory opts)
        exclude (:exclude opts)]
    (map compiled-file
         (comp/compile-root src-dir out-dir exclude))))
```

> NOTE 4: `(:exclude opts)` is `nil` when `:exclude` option is not set
> in the options map.

### compile-root

Take now a look at `compile-root` which is defined in
`compiler.clj`. Here is the orginal definition of `compile-root`.

```clojure
(defn compile-root
  "Looks recursively in src-dir for .cljs files and compiles them to
   .js files. If target-dir is provided, output will go into this
   directory mirroring the source directory structure. Returns a list
   of maps containing information about each file which was compiled
   in dependency order."
  ([src-dir]
     (compile-root src-dir "out"))
  ([src-dir target-dir]
     (let [src-dir-file (io/file src-dir)]
       (loop [cljs-files (cljs-files-in src-dir-file)
              output-files []]
         (if (seq cljs-files)
           (let [cljs-file (first cljs-files)
                 output-file ^java.io.File (to-target-file src-dir-file target-dir cljs-file)
                 ns-info (compile-file cljs-file output-file)]
             (recur (rest cljs-files) (conj output-files (assoc ns-info :file-name (.getPath output-file)))))
           output-files)))))
```

Notice that `compile-root` is not declared private to its namespace,
which means we should try our best to maintain stable its
interface. `compile-root` is already defined with multiple arities and
we're going to add a third one which accepts a vector of source files
and/or source directories to be excluded as last argument.  Here is
the new `compile-root` definition to be substituted to the orginal
one.

```clojure
(defn compile-root
  "Looks recursively in src-dir for .cljs files, but the excluded
   ones, and compiles them to .js files. If target-dir is provided,
   output will go into this directory mirroring the source directory
   structure. Returns a list of maps containing information about each
   file which was compiled in dependency order."
  ([src-dir]
     (compile-root src-dir "out"))
  ([src-dir target-dir]
     (compile-root src-dir target-dir nil))
  ([src-dir target-dir exclude]
     (let [src-dir-file (io/file src-dir)]
       (loop [cljs-files (cljs-files-in src-dir-file (exclude-file-names src-dir exclude))
              output-files []]
         (if (seq cljs-files)
           (let [cljs-file (first cljs-files)
                 output-file ^java.io.File (to-target-file src-dir-file target-dir cljs-file)
                 ns-info (compile-file cljs-file output-file)]
             (recur (rest cljs-files) (conj output-files (assoc ns-info :file-name (.getPath output-file)))))
           output-files)))))
```

As you can observe, we modified `compile-root` by:

* adding a new arity which accepts as third argument the value of
`:exclude` option passed by `compile-dir`;
* changing the call to `cljs-files-in`, which now receives the
  expasion produced by `exclude-file-names` as second argument.

### exclude-file-names

`exclude-file-names` is a new function which accept a source-dir and a
vector of source files and/or directories as arguments and produces
their expansion as a complete set of source files which live in
source-dir and are to be excluded from compilation.

Here is `exclude-file-names` definition.

```clojure
(defn exclude-file-names
  "Return a set of absolute paths of files that must be excluded"
  [dir exclude-vec]
  (set (filter #(.endsWith ^String % ".cljs")
          (map #(.getAbsolutePath ^java.io.File %)
               (mapcat #(let [dir-path (.getAbsolutePath ^java.io.File dir)]
                          (file-seq (io/file (str dir java.io.File/separator %))))
                       exclude-vec)))))
```

> The previous definition of `exclude-file-names` contains a inattention
> error (which doesn't shows itself when run) and could be implemented by
> reducing the number of times it traverses the excluce vector of source
> files and/or directories.
>
> In the subsequent tutorial we're going to implement `eclude-file-names`
> in a better way. This is a typical scenario in which, even if you're not
> fanatic about testing the code (so do I), you should appreciate to have
> some test: same interface, different implementation (not to be confused
> with the usual concept of polymorphism).

### cljs-files-in

`cljs-files-in` is the last function we have to update to support the
envisioned `:exclude` option. Here is the updated definition.

```clojure
(defn cljs-files-in
  "Return a sequence of all .cljs files in the given directory."
  ([dir] (cljs-files-in dir nil))
  ([dir exclude-set]
     (filter #(let [name (.getName ^java.io.File %)
                    path (.getAbsolutePath ^java.io.File %)]
                (and (.endsWith name ".cljs")
                     (not= \. (first name))
                     (not (contains? cljs-reserved-file-names name))
                     (not (contains? exclude-set path))))
             (file-seq dir))))
```

As you can see, we added a new arity to manage the expanded set of
CLJS source files to be excluded from compilation. The files exclusion
is obtained by adding a new `and` clause to the anonymous function
passed to `filter` HFO.

### project.clj

To test our patched CLJS compiler we first need to add the `:exclude`
option to the `:compiler` mode in the `:prod` build. Here is the final
`project.cljs`

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ; clojure source code path
  :source-paths ["src/clj"
                 "compiler/clojurescript/src/clj"
                 "compiler/clojurescript/src/cljs"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 ; compojure dependency
                 [compojure "1.1.5"]
                 ; domina dependency
                 [domina "1.0.0"]]
  :plugins [; cljsbuild plugin
            [lein-cljsbuild "0.2.9"]
            ; ring plugin
            [lein-ring "0.7.5"]]
  ; ring tasks configuration
  :ring {:handler modern-cljs.core/handler}
  ; cljsbuild tasks configuration
  :cljsbuild {:builds
              {
               :dev
               {; clojurescript source code path
                :source-path "src/cljs"
                ; Google Closure Compiler options
                :compiler {; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"
                           ; minimum optimization
                           :optimizations :whitespace
                           ; prettyfying emitted JS
                           :pretty-print true}}
               :prod
               {; clojurescript source code path
                :source-path "src/cljs"
                ; Google Closure Compiler options
                :compiler {; the name of emitted JS script file
                           :output-to "resources/public/js/modern.js"
                           ; the name of the CLJS source file to be
                           ; excluded from compilation
                           :exclude ["modern_cljs/connect.cljs"]
                           ;advanced optimization
                           :optimizations :advanced}}
               :pre-prod
               {; some path as above
                :source-path "src/cljs"
                :compiler {; different output name
                           :output-to "resources/public/js/modern_pre.js"
                           ; simple optmization
                           :optimizations :simple
                           ; no need prettyfication
                           }}}})
```

## Sanity check

We can now run `lein-cljsbuild` to check the patched CLJS compiler.

```bash
$ cd path/to/modern-cljs # cd in modern-cljs project home directory
$ lein cljsbuild clean # delete any previous compilation
$ lein cljsbuild once # compile all builds in project.clj
```

> NOTE 5: you should receive a lot of wornings during the cljsbuild
> compilation of domina. Don't warry about them.

To verify that `:dev` and `:pre-prod` builds still contain the generated
code to create the client-side connection with the brepl, do as follows:

```bash
$ tail -n 1 resources/public/js/modern_dbg.js
clojure.browser.repl.connect.call(null, "http://localhost:9000/repl");
$ tail -n 1 resources/public/js/modern_pre.js
clojure.browser.repl.connect.call(null, "http://localhost:9000/repl");
$
```

As you can see both `modern_dgb.js` and `modern_pre.js` files emitted
by the patched CLJS compiler still have the call to connect the
browser with the brepl server.

On the contrary, `modern.js`, which as been emitted by excluding
`modern_cljs/connect.cljs` form compilation, does not contain anymore
the connection with the brepl server as you can verify by yourself.

```bash
$ tail resources/public/js/modern.js
  return v(v(c) ? document.getElementById : c) ? document.getElementById(a).onsubmit = b : l
});
da("modern_cljs.login.validate_form", function() {
  var a = se(Rd("email")), b = se(Rd("password"));
  if((a = 0 < Fb(lf(a))) ? 0 < Fb(lf(b)) : a) {
    return g
  }
  alert("Please, complete the form!");
  return m
});
$
```

## Final notes

> NOTE 6: To complete the CLJS compiler patch we should modify
> `compiler/clojurescript/bin/cljsc.clj` as well, but for the purpose of
> this tutorial this last code change is not needed.

> NOTE 7: The content of this tutorial is the result of the work of
> [Federico Boniardi][4] and [Francesco Agozzino][5] during their
> efforts in learning CLJ/CLJS under my direction. I want to thank
> them for the good job they did.

# Next step - It's better to be safe than sorry (Part.1)

In the [next tutorial][6] we're going find the motivation and the room for testing.

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[2]: https://github.com/clojure/clojurescript/wiki/Quick-Start
[3]: https://github.com/emezeske/lein-cljsbuild/wiki/Using-a-Git-Checkout-of-the-ClojureScript-Compiler
[4]: https://github.com/federico-b
[5]: https://github.com/agofilo
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
