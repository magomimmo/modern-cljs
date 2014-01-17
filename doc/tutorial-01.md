# Tutorial 1 - The Basics

In the first tutorial you are going to create and configure a very basic
[CLJS][1] project using [leiningen 2][2] and [lein-cljsbuild][3] plugin.

[Leiningen][2] is a build management system for CLJ
projects. [lein-cljsbuild][3] is a leiningen plugin specialized in
managing CLJS projects.

## Create a Clojure (CLJ) project

If not already done, install [leiningen][2] and create a new project
named `modern-cljs`.

```bash
lein new modern-cljs
```

I'm assuming that you added `lein` to your `$PATH` environment variable.

## Modify the project structure

Create the directories to host both CLJ and CLJS source files and rename
the leiningen generated `src/modern_cljs` directory to reflect the new
directory structure.

```bash
cd modern-cljs
mkdir -p src/{clj,cljs/modern_cljs}
mv src/modern_cljs/ src/clj/
```

> NOTE 1: due to [java difficulties][4] in managing hyphen "-" (or other
> special characters) in package names, substitute an underscore for any hyphen
> in corresponding directory names.

> NOTE 2: At the moment we don't care about the `test` directory and we
> leave it as it is. We'll take care of it in a later tutorial.

Create a directory structure to host static resources of the project
(i.e., html pages, js script files, css stylesheet files, etc.)

```bash
mkdir -p resources/public/{js,css}
```

You should end up with the following directory structure

```bash
├── LICENSE
├── README.md
├── doc
│   └── intro.md
├── project.clj
├── resources
│   └── public
│       ├── css
│       └── js
├── src
│   ├── clj
│   │   └── modern_cljs
│   │       └── core.clj
│   └── cljs
│       └── modern_cljs
└── test
    └── modern_cljs
        └── core_test.clj

12 directories, 6 files
```

## Edit project.clj

You now need to edit `project.clj` to:

* update the `source-paths` of CLJ source code of the project;
* add and configure [lein-cljsbuild][3] plugin in `project.clj`.

Here is the original leiningen generated `project.clj`

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]])
```

And here is the updated version where we added the `lein-cljsbuild`
plugin, the `:cljsbuild` and the `:source-paths` configurations

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  ;; CLJ AND CLJS source code path
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.5.1"]]

  ;; lein-cljsbuild plugin to build a CLJS project
  :plugins [[lein-cljsbuild "1.0.0"]]

  ;; cljsbuild options configuration
  :cljsbuild {:builds
              [{;; CLJS source code path
                :source-paths ["src/cljs"]

                ;; Google Closure (CLS) options configuration
                :compiler {;; CLS generated JS script filename
                           :output-to "resources/public/js/modern.js"

                           ;; minimal JS optimization directive
                           :optimizations :whitespace

                           ;; generated JS code prettyfication
                           :pretty-print true}}]})
```

> ATTENTION NOTE
>
> The careful reader would have noted that the `:source-paths` option
> pertaining the project, the so called Leiningen `:source-paths`, has
> been valued by adding to it the pathaname value of the `:source-paths`
> specific to the `:cljsbuild` build.
>
> This is because the `cljsbuild` plugin does not automatically add back
> to the project `classpath` the CLJS pathnames configured in its own
> `:source-paths` option.
>
> This is an almost hidden requirement which, if not satisfied, will
> generate bugs very difficult to be caught.
>
> Many thanks to [David Nolen][7] and [Chas Emerick][9] for having
> [pointed me][8] to this trouble.

## Create a CLJS source file

After having configured `project.clj`, create a CLJS file inside
`src/cljs/modern_cljs` directory with the following code

```clojure
(ns modern-cljs.modern)

(.write js/document "Hello, ClojureScript!")
```

Save the file as `modern.cljs`.

> NOTE 3: Please note that the filename extension for ClojureScript source
> code is *cljs*, not *clj*.

## Create an HTML page

Create a simple html file and include a `script` tag pointing to the value
of `:output-to` keyword of `project.clj`. Save the file as `simple.html` in
`resources/public/` directory.

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Simple CLJS</title>
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</head>
<body>
    <!-- pointing to cljsbuild generated js file -->
    <script src="js/modern.js"></script>
</body>
</html>
```

## Compile CLJS

To compile CLJS to JS, use the `once` subtask of `cljsbuild` as
follow:

```bash
lein cljsbuild once
Compiling ClojureScript.
WARNING: It appears your project does not contain a ClojureScript dependency. One will be provided for you by lein-cljsbuild, but it is strongly recommended that you add your own.  You can find a list of all ClojureScript releases here:
http://search.maven.org/#search|gav|1|g%3A%22org.clojure%22%20AND%20a%3A%22clojurescript%22
You're using [lein-cljsbuild "1.0.0"], which is known to work well with ClojureScript 0.0-2014 - *.

Compiling "resources/public/js/modern.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 12.146675 seconds.
```

As you can see, you received a *WARNING* saying that, even if the
`cljsbuild` plugin is able to provide you a CLJS release, it is
strongly recommended to explicitly add a specific CLJS release in the
`:dependencies` section of the `project.clj` file.

Let's add an explicit CLJS release into the project `:dependencies`
section to make `cljsbuild` happy.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2069"]]
  ...)
```

Now issue the `lein cljsbuild clean` command to clean the previous
compilation and issue the `lein cljsbuild once` command again.

```clj
lein cljsbuild clean
Deleting files generated by lein-cljsbuild.
```

```clj
lein cljsbuild once
Compiling ClojureScript.
Retrieving org/clojure/clojurescript/0.0-2069/clojurescript-0.0-2069.pom from central
Retrieving org/clojure/clojurescript/0.0-2069/clojurescript-0.0-2069.jar from central
Compiling "resources/public/js/modern.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 10.109162 seconds.
```

## Visit simple.html

Open a browser and visit the local file `simple.html`. If everything
went ok, you should see "Hello, ClojureScript!".

![Hello ClojureScript][5]

## Next Step - [Tutorial 2: Browser CLJS REPL (bREPL)][6]

In the next [tutorial][6] we're introduce the so called *brepl*, a browser
connected CLJS REPL.

# License

Copyright © Mimmo Cosenza, 2012-2014. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/clojure/clojurescript.git
[2]: https://github.com/technomancy/leiningen
[3]: https://github.com/emezeske/lein-cljsbuild.git
[4]: http://docs.oracle.com/javase/specs/jls/se7/html/jls-6.html
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/hellocljs.png
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[7]: https://github.com/swannodette
[8]: https://groups.google.com/forum/#!topic/clojurescript/CT0aDLgLxW8
[9]: https://github.com/cemerick
