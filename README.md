# modern-cljs

A tutorial to guide you in creating and setting up ClojureScript (CLJS) project.

# Introduction

This tutorial will guide you in creating, setting up and run a simple
CLJS project and will follow a progressive enhancement of the project
itself.

# Tutorial One - The basic

In the first tutorial you are going to create and configure a very basic
CLJS project.

## Create a Clojure (CLJ) project

Install, if not already done, leiningen and create a new project named
`mondern-cljs`

```
$ lein new modern-cljs
```

## Modify the project structure

Create the directory to host both CLJ and CLJS source files and rename
the generated `src/modern_cljs` directory to reflect the new directory
structure.

```
$ cd modern-cljs
$ mkdir -p src/{clj,cljs/modern_cljs}
$ mv src/modern_cljs/ src/clj/
```

Create a directory structure to host static resources of the project
(i.e. html pages, js script files, css stylesheet files, etc.)

```
$ mkdir -p resources/public/{js,css}
```

## Edit project.clj

Now you need to edit `project.clj` to:

* update source-paths of CLJ source code of the project;
* add and configure `lein-cljsbuild` plugin to `project.clj`.

Here is the leiningen generated `project.clj`

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]])
```

And here is the new version to reflect the above changes.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[lein-cljsbuild "0.2.9"]]
  :cljsbuild {:builds
              [{:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/modern.js"
                           :optimizations :whitespace
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

Create `simple.html` file with a `script` tag pointing to the value
of `:output-to` keyword of `project.clj`. Save the file in
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
    <!-- sample.html -->
    <script src="js/modern.js"></script>
</body>
</html>
```

## Compile CLJS

To compile CLJS to JS, use lein-cljsbuild command as follow:

```
$ lein cljsbuild once
Compiling ClojureScript.
Compiling "resources/public/js/modern.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern.js" in 7.860731 seconds.
mimmo$
```
## Visit simple.html

Open a browser and visit the local file `simple.html`. If everything
went ok, you should see "Hello, ClojureScript!".

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.
