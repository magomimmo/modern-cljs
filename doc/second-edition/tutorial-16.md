# Tutorial 16 - TBD

bla bla

# Preview

# Introduction


Enter
[Task Options DSL](https://github.com/boot-clj/boot/wiki/Task-Options-DSL)
for `boot`.

Before proceeding with the next step, stop any `boot` related process.

## Task Options DSL

As reported in the above wiki page, `boot` is a servant of more owners
and it has to please all of them.

> `boot` tasks are intended to be used from the command line, from a REPL,
> in the project's build.boot file, or from regular Clojure
> namespaces. This requires support for two conceptually different
> calling conventions: with command line options and optargs, and as
> s-expressions in Clojure with argument values.
> 
> Furthermore, since tasks are boot's user interface it's important that
> they provide good usage and help documentation in both environments.

Let's say we want to call the `testing` task we defined above by
passing to it one or more directories to be added to the
`:source-paths` environment variable. At the moment in our project we
only have the `test/cljc` directory, but as soon as it gets more
complicated we'll probably have to add more testing namespaces
specifically dedicated CLJ or CLJS.

Even if you may name your testing namespaces as you want and place
them wherever you like, it's considered a good practices to keep the
testing namespaces specific for CLJ separated from the ones specific
for CLJS and, in turns, keep both of them separated from testing
namespaces which are portable on CLJ and CLJS as well.

At some point in time you'll probably end up with a directory layout
like the following:

```bash
test/
├── clj
├── cljc
└── cljs
```

which mimics the same directory layout for the source code

```bash
src/
├── clj
├── cljc
└── cljs
```

## Be prepared for changes

To be prepared for any testing scenario, while keeping the more
frequent one as simple as possible (i.e., `boot tdd`), we need to
approach the definitions of our tasks from top to bottom.

For example, we would like to be able to call the `tdd` task by
passing to it one or more test directories to be added to the
`classpath` of the project. Something like that:

```bash
boot tdd -t test/cljc
```
or like this

```bash
boot tdd -t test/clj -t test/cljs -t test/cljc
```

I know, you'd prefer the following form

```bash
boot tdd -t test/clj:test/cljs:test/cljc
```

Tasks options DSL (Domain Specific Language) offers 

like to be able to add paths of one or more directories
containing the unit test namespaces to be run.

At the same time we'd like to be able to directly call the `testing`
task from the command line as follows:

```bash
boot testing -t test/clj
boot testing -t test/cljs -t test/clj -t test/cljc
```

> NOTE 6: here we are using the
> [multiple options](https://github.com/boot-clj/boot/wiki/Task-Options-DSL#multi-options)
> features of the Task Options DSL.

### A dummy task

Let's temporarily create a dummy task in the `build.boot` to see how
all that could work:

```clj
(deftask dummy
  "A dummy task"
  [t dirs PATH #{str} ":source-paths"]
  (println *opts*))
```

Now run the CLJ REPL and evaluate few `dummy` calls at the terminal:

```bash
boot dummy
{}
boot dummy -t test/cljc
{:dirs #{test/cljc}}
boot dummy -t test/cljc -t test/cljs
{:dirs #{test/cljc test/cljs}}
boot dummy -t test/cljc -t test/cljs -t test/clj
{:dirs #{test/clj test/cljc test/cljs}}
```

Aside from the fact that the passed directories have been
de-stringified by the printer, we obtained what we wanted.

We can even please our users by showing them a short help:

```bash
boot dummy -h
A dummy task

Options:
  -h, --help       Print this help info.
  -t, --dirs PATH  Conj PATH onto :source-paths
```

### Update build.boot

We know enough to update the `testing` task and even to change its
name to `add-paths` by considering that all is does is to add one or
more directories to the `:source-paths` environment variable.

```clj
(deftask add-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (set-env! :source-paths #(into % dirs))
  identity)
```

Substitute the newly defined `add-paths` task to the previous
`testing` task in the `tdd` task definition and finally start it at
the terminal as usual:

```clj
(deftask tdd
  "Launch a TDD Environment"
  []
  (comp
   (serve :dir "target"
          :handler 'modern-cljs.core/app
          :resource-root "target"
          :reload true)
   (add-paths :dirs #{"test/cljc"})
   (watch)
   (reload)
   (cljs-repl)
   (test-cljs :out-file "main.js" 
              :js-env :phantom 
              :namespaces '#{modern-cljs.shopping.validators-test})
   (test :namespaces '#{modern-cljs.shopping.validators-test})))
```

```bash
boot tdd
...
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 24.912 sec
```clj

Everything is still working as expected. 

Now that we know how to parameterize a task by using the task options
DSL (Domain Specific Language), we'd like to apply this knowledge to
the `tdd` task as well.


Stay tuned for the next tutorial.

# Next Step - Tutorial 17 - TBD 


Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.


