# Tutorial 1 - The basic

In the first tutorial you are going to create and configure a very basic
[CLJS][1] project using [leiningen 2][2] and [lein-cljsbuild][3] plugin.

[Leiningen][2] is a build managment system for CLJ
projects. [lein-cljsbuild][3] is a leiningen plugin specialized in
managing CLJS projects.

## Create a Clojure (CLJ) project

If not already done, install [leiningen][2] and create a new project
named `mondern-cljs`.

```bash
$ lein new modern-cljs
```

I'm assuming that you added `lein` to your `$PATH` environment variable.

## Modify the project structure

Create the directories to host both CLJ and CLJS source files and rename
the leiningen generated `src/modern_cljs` directory to reflect the new
directory structure.

```bash
$ cd modern-cljs
$ mkdir -p src/{clj,cljs/modern_cljs}
$ mv src/modern_cljs/ src/clj/
```

> Note: due to [java difficulties][4] in managing hyphen "-" (or other
> special characters) in package names, substitute any hyphen with
> underscore in corresponding directory names.

Create a directory structure to host static resources of the project
(i.e. html pages, js script files, css stylesheet files, etc.)

```bash
$ mkdir -p resources/public/{js,css}
```

## Edit project.clj

You now need to edit `project.clj` to:

* update source-paths of CLJ source code of the project;
* add and configure [lein-cljsbuild][3] plugin in `project.clj`.

Here is the original leiningen generated `project.clj`

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]])
```

And here is the updated version where we added added the
`lein-cljsbuild` plugin, the `:cljsbuild` and the `:source-paths`
configurations

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  ;; CLJ source code path
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]]

  ;; lein-cljsbuild plugin to build a CLJS project
  :plugins [[lein-cljsbuild "0.2.10"]]

  ;; cljsbuild options configuration
  :cljsbuild {:builds
              [{;; CLJS source code path
                :source-path "src/cljs"

                ;; Google Closure (CLS) options configuration
                :compiler {;; CLS generated JS script filename
                           :output-to "resources/public/js/modern.js"

                           ;; minimal JS optimization directive
                           :optimizations :whitespace

                           ;; generated JS code prettyfication
                           :pretty-print true}}]})
```

## Create a CLJS source file

After having configured `project.clj`, create a CLJS file inside
`src/cljs/modern_cljs` directory with the following code

```clojure
(ns modern-cljs.modern)

(.write js/document "Hello, ClojureScript!")
```

Save the file as `modern.cljs`.

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

To compile CLJS to JS, use the task `once` of `cljsbuild` as follow:

```bash
$ lein cljsbuild once
Compiling ClojureScript.
Compiling "resources/public/js/modern.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern.js" in 7.860731 seconds.
$
```
## Visit simple.html

Open a browser and visit the local file `simple.html`. If everything
went ok, you should see "Hello, ClojureScript!".

![Hello ClojureScript][5]

## Next Step

In the next [tutorial][6] we're introduce the so called *brepl*, a browser
connected CLJS REPL.

# License

Copyright © Mimmo Cosenza, 2012-2013. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/clojure/clojurescript.git
[2]: https://github.com/technomancy/leiningen
[3]: https://github.com/emezeske/lein-cljsbuild.git
[4]: http://docs.oracle.com/javase/specs/jls/se7/html/jls-6.html
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/hellocljs.png
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
