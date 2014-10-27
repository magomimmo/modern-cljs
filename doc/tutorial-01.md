# Tutorial 1 - The Basics

In this first tutorial, you're going to create and configure a basic
[ClojureScript][1] (CLJS) project using [Leiningen 2][2] and the
[lein-cljsbuild][3] plugin.

[Leiningen][2] is a build management system for Clojure and CLJS
projects. [lein-cljsbuild][3] is a Leiningen plugin for
managing CLJS projects.

## Create a new project

If you have not already done so, install [Leiningen][2] by following the few
simple installation steps in [Leiningen's README][2]. Then create a new
project named `modern-cljs`:

```bash
lein new modern-cljs
```

I'm assuming you added `lein` to your `$PATH` environment variable per the
installation instructions.

## Modify the project's structure

The default project structure created by Leiningen only has directories for
Clojure code. Create the directories to contain both Clojure and CLJS source
files, and rename the Leiningen generated `src/modern_cljs` directory to
reflect this new directory structure:

```bash
cd modern-cljs
mkdir -p src/{clj,cljs/modern_cljs}
mv src/modern_cljs/ src/clj/
```

> NOTE: Due to [Java's difficulties][4] in having a hyphen "-" (or other
> special characters) in package names, substitute an underscore for any
> hyphens in corresponding directory names. This is why modern-cljs becomes
> modern_cljs in the file system.

We don't care about the `test` directory just yet, so
leave it as it is. We'll take care of it in a later tutorial.

Next, create a directory structure to host static resources of the project
(i.e., html pages, js script files, css stylesheet files, etc.):

```bash
mkdir -p resources/public/{js,css}
```

You should now have the following directory structure:

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

Leiningen is configured with the `project.clj` file. You need to 
edit `project.clj` to:

* update the `source-paths` of the Clojure source code of the project
* add and configure the [lein-cljsbuild][3] plugin

Here is the original `project.clj` file that was generated:

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]])
```

And here is the updated version where you add the `lein-cljsbuild`
plugin, the `:cljsbuild` option that configures `lein-cljsbuild`, and the 
`:source-paths` configurations that match the directory structure of the
project:

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

> WARNING: A careful reader might have noticed that the `src/cljs` path is
> included in both the `:source-paths` option pertaining to the entire project,
> and in the `:source-paths` option specific to the `:cljsbuild` configuration.
>
> This is because the `cljsbuild` plugin does not automatically add the CLJS
> pathnames configured in its own `:source-paths` option to the project's
> `classpath`.
>
> Missing this requirement can cause bugs that are difficult to catch.
>
> Many thanks to [David Nolen][7] and [Chas Emerick][9] for
> [making me aware][8] of this.

## Create your first CLJS source file

Now that the project is configured, create a CLJS file called `modern.cljs`
inside the `src/cljs/modern_cljs` directory with the following code:

```clojure
(ns modern-cljs.modern)

(.write js/document "Hello, ClojureScript!")
```

> NOTE: The filename extension for ClojureScript source code is *cljs*,
> not *clj*.

## Create an HTML page

Create a simple HTML file that has a `script` tag pointing to the JavaScript
that's generated when our ClojureScript is compiled. The generated JavaScript
is in the file specified by the `:output-to` keyword in the `project.clj`
file. In our case, the output file is configured to `js/modern.js`.

Save the HTML file as `simple.html` in the `resources/public/` directory:

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

## Compile the ClojureScript source

To compile CLJS to JavaScript, use the `once` subtask of the `cljsbuild`
Leiningen task as follows:

```bash
lein cljsbuild once
Compiling ClojureScript.
WARNING: It appears your project does not contain a ClojureScript dependency. One will be provided for you by lein-cljsbuild, but it is strongly recommended that you add your own.  You can find a list of all ClojureScript releases here:
http://search.maven.org/#search|gav|1|g%3A%22org.clojure%22%20AND%20a%3A%22clojurescript%22
You're using [lein-cljsbuild "1.0.0"], which is known to work well with ClojureScript 0.0-2014 - *.

Compiling "resources/public/js/modern.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 12.146675 seconds.
```

As you can see, you received a *WARNING* saying that, even though the
`cljsbuild` plugin is able to provide you with a CLJS release, it is
strongly recommended you add a specific CLJS release in the
`:dependencies` section of the `project.clj` file.

Let's add an explicit CLJS release in the `:dependencies`
section to make `cljsbuild` happy. Update the `project.clj` file as follows:

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
Compiling "resources/public/js/modern.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 10.109162 seconds.
```

## Visit simple.html

With a web browser, open the local file `resources/public/simple.html` (use
File → Open File in Chrome, or the equivalent in your browser). If everything
went OK, you should see "Hello, ClojureScript!".

![Hello ClojureScript][5]

So what exactly happened? You compiled the ClojureScript source, including the
line, `(.write js/document "Hello, ClojureScript!")`, into JavaScript
with the cljs-build Leiningen task. You included the generated JavaScript file
in the HTML page with a `script` tag. When you loaded the page it executed
the JavaScript which added the text "Hello, Clojurescript!" to the `body` of
the HTML page, and your browser dutifully displayed the text.

You can open up the `resources\public\js\modern.js` file to take a look at the
generated JavaScript. You'll see there's a lot of boilerplate JavaScript that
you aren't taking advantage of yet. The last few lines of the file should be
recognizable. They contain the direct translation of your ClojureScript code.

## Next Step - [Tutorial 2: Browser CLJS REPL (bREPL)][6]

In the next [tutorial][6] you'll setup a so called *brepl*, which is a 
ClojureScript REPL (interactive console) connected to your browser.

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