# Tutorial 21 Reagent: a minimalist interface to React.js

In the [previous tutorial][1] we enhanced the `build.boot` building
file to be able to install a CLJ/CLJS library to the local `maven`
repository and finally publish it to
[`clojars`](https://clojars.org/).

As it has been said in previous tutorials aimed at creating a kind of
confidence with the ClojureStript programming language, by adopting
the [domina](https://github.com/levand/domina) library for DOM
manipulation, we have been almost prehistoric.

In this tutorial of the series we're going to introduce
[Reagent](http://reagent-project.github.io/), a very well known
minimalist ClojureScript interface to
[React.js](https://facebook.github.io/react/index.html) which is a
JavaScript library for building user interfaces that got a lot of
attention all over the places.

## Preamble

To start working, assuming you've git installed, do as follows:

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-18
```

## Introduction

Even if the most discussed facet of
[React.js](https://facebook.github.io/react/index.html) is the idea of
adopting a
[Virtual DOM](http://facebook.github.io/react/docs/working-with-the-browser.html#the-virtual-dom)
(VDOM) maintaining a fast and immutable in-memory representation of
the DOM, my personal opinion is that the VDOM and its differing
algorithm only represent implementation details. To me, the most
fundamental aspects of React.js are:

* [one-way data flow, AKA one-way binding](https://youtu.be/nYkdrAPrdcw?list=PLb0IAmt7-GS188xDYE-u1ShQmFFGbrk0v);
* [composability model](https://facebook.github.io/react/docs/multiple-components.html);
* [synthetic event system](http://facebook.github.io/react/docs/interactivity-and-dynamic-uis.html#event-handling-and-synthetic-events);

Even if I appreciate a lot React.js (and React Native too) I still
think that ClojureScript is a superior programming language when
compared with JS and with
[JSX](http://facebook.github.io/react/docs/jsx-in-depth.html) as well.

To explain my opinion on React.JS and, at the same time, to introduce
you to [Reagent](http://reagent-project.github.io/), it can be very
useful to start this tutorial by porting to Reagent the
[standard React.js introductory tutorial](https://facebook.github.io/react/docs/tutorial.html).

## Install React Tutorial

The best way to appreciate Reagent is to try out the standard React.js
tutorial by downloading and locally running it on your computer.

### Install node.js

First you need to install [node.js](https://nodejs.org/en/). I
personally prefer to be able to install it by using
[nvm](https://github.com/creationix/nvm), a simple script to install
and manage different node versions.

The easiest way to install `nvm` is by its install script as follow:

```bash
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.0/install.sh | bash
```

> NOTE 1: On OSX, if you get `nvm: command not found` after running
> the install script, your system may not have a `.bash_profile` file
> where the command is set up. Simple create one with `touch
> ~/.bash_profile` and run the install script again.

Then you can install `node.js` by issuing the following command:

```bash
nvm install 5.0
```

You can now verify that both `node.js` and `npm` have been installed:

```bash
node -v
v5.10.0
```

```bash
npm -v
3.8.3
```

### Get and run the React Tutorial

Now that you have `node` and `npm` installed on your computer you can
clone and run the React Tutorial as follows:

```bash
git clone https://github.com/reactjs/react-tutorial.git
Cloning into 'react-tutorial'...
remote: Counting objects: 496, done.
remote: Total 496 (delta 0), reused 0 (delta 0), pack-reused 496
Receiving objects: 100% (496/496), 98.83 KiB | 0 bytes/s, done.
Resolving deltas: 100% (246/246), done.
Checking connectivity... done.
```

```bash
cd react-tutorial
npm install
react-tutorial@0.0.0 /Users/mimmo/tmp/react-tutorial
├─┬ body-parser@1.15.0
...
  └── vary@1.0.1

npm WARN react-tutorial@0.0.0 No license field.
```

```bash
PORT=3001 node server.js
Server started: http://localhost:3001/
```

> NOTE 2: we set the port of the node http server to `3001` to not
> collide with the default `3000` port number of the clojure web
> server we're going to later launch with the `boot dev` command from
> within the `modern-cljs` project folder.

### Play with the React web application

Now visit the [localhost:3001](http://localhost:3001/) URL in your
browser and you should receive something like this:

![React Tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-01.png)

As you see this is a very basic web application, but it contains very
important concepts to be grasped to better understand as `React.js`
works and, consequently, how `Reagent` eventually differs from it.

Note that the page is composed of the following elements:

1. An `<h1>` `Comments` element;
1. A list (i.e. `<div>`) of `Comments`;
1. A `<form>` element to submit new `Comment`;

Each comment is composed of:

1. An `<h2>` element for the author of the `Comment`
1. A `<span>` element for the `Comment` itself. 

Now post a couple of new comments by using the form. You'll note the
list of the comments to be updated without refreshing the full
page.

![React Tutorial new comments](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-02.png)

Nothing new under the sun. What is new, aside from the
performance that we can't appreciate with such a simple sample, it is hidden
under the wood.

> NOTE 3: In the above image you'll note that the first added comment
> contains a link and the second contains a word in bold. This is
> because the `<input>` element for the comment is even able to parse
> a `markdown` text.

## Porting to Reagent. Step 1: your first React Component

As said, my opinion is that to better appreciate `Reagent`, we should
known a little bit of `React.js` as well. So, let's get started by
following the official tutorial of `React.js` to progressively port it
to `Reagent`.

First rename the original `public/scripts/example.js` file to
`public/scripts/complete.js` and create a new
`public/scripts/example.js` file:

```bash
mv public/scripts/example.js public/scripts/old.js
```

```bash
touch public/scripts/example.js
```

Now open the `example.js` source file with your preferred editor and
copy and paste into it the code from the beginning of the official
react tutorial:

```js
var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        Hello, world! I am a CommentBox.
      </div>
    );
  }
});

ReactDOM.render(
  <CommentBox />,
  document.getElementById('content')
);
```

Reload the [localhost:3001](http://localhost:3001/) page. You should
see your very first and simple React component saying "Hello, world! I
am a CommentBox.

The above lines mix into JS code a kind of HTML code, JSX code in
React.js parlance. It create a new class, named `CommentBox`, which is
an UI Component, containing one method only: `render()`. This method
uses JSX syntax to declare the structure of the component itself
which, in this very simple case, is just a `div` tag element (it's a
React component, not an HTML element) and the text `Hello, world! I am
a CommentBox`.

Then, this newly created UI `ComponentBox` is instantiated by the
`ReactDOM.render()` method, which also attaches it to the `content`
element `id` from the HTML page.

Let's take a look at this HTML page living in the `public` directory
of the `react-tutorial` project folder.

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>React Tutorial</title>
    <!-- Not present in the tutorial. Just for basic styling. -->
    <link rel="stylesheet" href="css/base.css" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.0.1/react.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.0.1/react-dom.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/babel-core/5.6.16/browser.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.2/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/marked/0.3.5/marked.min.js"></script>
  </head>
  <body>
    <div id="content"></div>
    <script type="text/babel" src="scripts/example.js"></script>
    <script type="text/babel">
      // To get started with this tutorial running your own code, simply remove
      // the script tag loading scripts/example.js and start writing code here.
    </script>
  </body>
</html>
```

For the moment there are only two things to be noted here:

1. the `<div id="content"></div>`, which represent the `root` HTML
   element to which the `CommentBox` instance is attached;
1. the `<script type="text/babel" src="scripts/example.js"></script>`
   `script` tag loading the `example.js` file we just coded.

## Porting to Reagent. Step 2: prepare the field

Ad said, Reagent is a ClojureScript minimalist interface to React.js,
which means that it internally uses React.js from ClojureScript. We
want to understand its mechanics by first interact with it at the bREPL.

Before starting the IFDE (Immediate Feedback Development Environment)
we defined in previous tutorial, we first want add the latest
available Reagent library to the `build.boot` of the `modern-cljs`
project.

```clojure
(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"html"}

 :dependencies '[
                 ...
                 [reagent "0.6.0-alpha"]
                 ])
...
```

Now create the `reagent.html` file in the `html` directory of the `modern-cljs`:

```bash
cd /path/to/modern-cljs
touch html/reagent.html
```

and copy into it the following very simple `html` code which resemble
both the corresponding `index.html` file from the react tutorial and
the `index.html` file from the `modern-cljs` project.


```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Reagent Tutorial</title>
    <link rel="stylesheet" href="css/base.css" />
  </head>
  <body>
    <div id="content"></div>
    <script src="main.js"></script>
  </body>
</html>
```

As you see, we linked the `main.js` file that will be created by the
CLJS compilation. 

There are only two remarkable differences from the `index.html` and
`shopping.html` files from the previous tutorials:

1. in the `body` we now have a single `div` tag only. This is very
   typical of any Single Page Application (SPA) and it resembles the
   `index.html` file from the react tutorial;
1. we have not added the call of the `init()` function to be exported
   from the `modern-cljs.reagent` CLJS namespace, as we already did in
   the previous tutorials with the `modern-cljs.login` and
   `modern-cljs.shopping` namespaces. This is because in the next few
   steps we're going to work at the bREPL and we'll add that call
   later, when we'll persist our experiment into the `reagent.cljs`
   source file.

Also copy the `base.css` file from the react tutorial to the
`html/css` directory of the `modern-cljs` project to obtain the same
basic page style.

```bash
cp /path/to/react-tutorial/public/css/base.css html/css/
```

We are almost done. Before launching the IFDE with the `boot dev`
command, create the `reagent.cljs` file and require the `reagent.core`
namespace into it.

```bash
touch src/cljs/modern_cljs/reagent.cljs
```

```clj
(ns modern-cljs.reagent
  (:require [reagent.core :as r]))
```

## Porting to Reagent. Step 3: your first Reagent component

Launch the IFDE as usual:

```bash
boot dev
Starting reload server on ws://localhost:54336
...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Writing target dir(s)...
Elapsed time: 32.980 sec
```

> NOTE 4: you could launch the TDD environment as well with the `boot
> tdd` command. But at the moment we're not interested in executing any
> test. We only want to learn about Reagent by interact with it through
> the bREPL.

So far, so good.

Now open a new terminal and launch the `boot` client and the `bREPL`
on top of it as usual.

```bash
boot repl -c
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.7.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_66-b17
        Exit: Control+D or (exit) or (quit)
    Commands: (user/help)
        Docs: (doc function-name-here)
              (find-doc "part-of-name-here")
Find by Name: (find-name "part-of-name-here")
      Source: (source function-name-here)
     Javadoc: (javadoc java-object-or-class-here)
    Examples from clojuredocs.org: [clojuredocs or cdoc]
              (user/clojuredocs name-here)
              (user/clojuredocs "ns-here" "name-here")
boot.user=>
```

```clj
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:54355 >>
<< waiting for client to connect ... Connection is ws://localhost:54355
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

NOTE 5: as usual with IDFE, as soon as you visit the
[localhost:3000/reagent.html]() URL the bREPL connected with the JS
engine of the browser and it is ready to evaluate CLJS expressions.

Now require the `reagent.core` namespace at the bREPL and create your
very first Reagent Component, which, under the wood, is a React
Component.

```clj
cljs.user> (require '[reagent.core :as r])
nil
```

```clj
cljs.user> (defn component-box []
             [:div "Hello, world! I am a component-box"])
#'cljs.user/component-box
```

Believe it or not, this unbelievable simple and pure function will
create a React Component corresponding to the `ComponentBox` component
we previously created with the following `JSX` code:

```js
var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        Hello, world! I am a CommentBox.
      </div>
    );
  }
});
```

Don't you believe it. Evaluate the following form:

```clj
cljs.user> (r/render-component [component-box] (.getElementById js/document "content"))
#object[Object [object Object]]
```

Do you see the `Hello, world! I am a component-box` text in the page?
The above `render-component` call correspond to the

```js
ReactDOM.render(
  <CommentBox />,
  document.getElementById('content')
);
```

> NOTE 6: as you see, being clojurean, we used `kebab-case` names
> (i.e. `component-box`) instead of `CamelCase` names
> (i.e. `ComponentBox`).


## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-20.md
