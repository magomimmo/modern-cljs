# Tutorial 21 - A minimalist interface to React with Reagent (Part I)

In the [previous tutorial][1] we enhanced the `build.boot`
file to be able to install a CLJ/CLJS library to the local `maven`
repository and finally publish it to
[`clojars`](https://clojars.org/).

As it has been said in previous tutorials aimed at creating a kind of
confidence with the ClojureStript programming language, by adopting
the [domina](https://github.com/levand/domina) library for DOM
manipulation we have been almost prehistoric from the point of view of
building User Interfaces with CLJS.

In this tutorial of the series we're going to fill that gap by
introducing [Reagent](http://reagent-project.github.io/), a very well
known minimalist ClojureScript interface to
[React.js](https://facebook.github.io/react/index.html).  React is a JavaScript
library for building User Interfaces created by Facebook, that
recently got a lot of attention all over the places.

## Preamble

To start working, assuming you've `git` installed, do as follows:

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-18
git checkout -b reagent-tutorial
```

## Introduction

Obviously, this is not a tutorial on
[React](https://facebook.github.io/react/docs/why-react.html) and I'm
not going to explain its details. Still, I think that by porting to
[Reagent](http://reagent-project.github.io/) the
[official React introductory tutorial](https://facebook.github.io/react/docs/tutorial.html)
step by step, we could better appreciated the Reagent minimalism and
eventually understand its different approach from React itself.

## Install React Tutorial

To get an idea of the final web application created step by step in
the React Tutorial you need [Node.js](https://nodejs.org/en/) to be
installed on your computer.

### Install Node Version Manager (NVM)

I personally prefer to be able to install `node` by using
[nvm](https://github.com/creationix/nvm), a simple script to install
and manage different `node` versions.

Open your terminal and do the following:

```bash
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.0/install.sh | bash
```

> NOTE 1: On OSX, if you get `nvm: command not found` after running
> the install script, your system may not have a `.bash_profile` file
> where the command is set up. Simple create one with `touch
> ~/.bash_profile` and run the install script again.

You can now install `node` by issuing the following command at the
terminal:

```bash
nvm install 5.0
```

Verify that both `node.js` and `npm` have been installed:

```bash
node -v
v5.10.0
```

```bash
npm -v
3.8.3
```

### Clone and run the React Tutorial

Now that you have `node` and `npm` installed on your computer, clone
and run the React Tutorial final web application as follows:

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

> NOTE 2: I set the port of the node http server to `3001` to not
> collide its [express server](http://expressjs.com/) with the default
> `3000` port number of the clojure web server we're going to later
> launch.

### Play with the React web application

Now visit the [localhost:3001](http://localhost:3001/) URL in your
browser and you should receive something like this:

![React Tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-01.png)

As you see, this is a very basic web application, but it contains
important concepts to be grasped for better understanding `React` and
the way `Reagent` interfaces with it.

Post a couple of new comments by using its comment form. You'll note
the list of the comments will be updated without a full page refresh.

![React Tutorial new comments](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-02.png)

Nothing new under the sun. What is new, aside from the performance
that we can't appreciate with such a simple sample, is well hidden
under the wood.

> NOTE 3: In the above image you'll note that the first newly added
> comment contains a link and the second comment contains a word in
> bold. This is because the `<input>` element for the comment is able
> to parse `markdown` text.

## Your first React Component

Now that you have an idea about the final behavior of the web
application implemented in the React Tutorial, let's get started by
following it step by step to progressively port it to Reagent.

Stop the running `node` server and issue the following commands at
the terminal to start from scratch:

```bash
git reset --hard
HEAD is now at 2be1a2d Use 15.0.1
```

```bash
git checkout -b reagent-tutorial
Switched to a new branch 'reagent-tutorial'
```

```bash
rm public/scripts/example.js
touch public/scripts/example.js
```

Open the `example.js` source file with your preferred editor paste
into it the following code extracted from the beginning of the
official React Tutorial:

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

Rerun the tutorial web app:

```bash
PORT=3001 node server.js
Server started: http://localhost:3001/
```

and then reload the [localhost:3001](http://localhost:3001/) page. You
should see your very first and simple React Component saying `Hello,
world! I am a CommentBox.`

The above lines mix into JS code a kind of HTML code,
[JSX](https://facebook.github.io/react/docs/jsx-in-depth.html) code in
React parlance. It first defines a new class, named `CommentBox`,
which is an UI component containing one method only: `render()`. This
method uses the JSX syntax to declare the structure of the component
itself. In this very simple case, it is just a `div` component and the
text node `Hello, world! I am a CommentBox`.

This newly defined `ComponentBox` component class is then
instantiated by the `ReactDOM.render()` method, which also attaches it
to the `content` element `id` of the HTML page.

Let's take a look at the `index.html` from the `public`
directory of the `react-tutorial` project folder.

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

There are a few things to be noted here:

1. the `<div id="content"></div>` represent the `root` HTML
   element to which the `CommentBox` component instance is to be
   attached;
1. the inclusion of the [marked JS library][2] for rendering mardown text;
1. the `<script type="text/babel" src="scripts/example.js"></script>`
   `script` tag loading the `example.js` file we just coded

The JSX code contained in the `example.js` file can't be
interpreted as is in a browser. It first needs to pass through the
[`babel transpiler`](https://babeljs.io/) to be transformed into JS
source code, which will be compatible with almost any browser. That's why the `type`
attribute of the latest two `script` tags are set to `text/babel`.

## Prepare the field

Before starting the CLJS development environment, we first want to add
the latest available Reagent library to the `build.boot` of the
`modern-cljs` project.

```clj
(set-env!
 ...
 :dependencies '[
                 ...
                 [reagent "0.6.0-alpha"]
                 [cljsjs/marked "0.3.5-0"]
                 ])
...
```

Note that we also added the `cljsjs/marked "0.3.5-0"` JS external
library. This is the same JS library used in the React Tutorial, and
it is packaged to be used in a CLJS project. We'll see its use later.

Now create the `reagent.html` file in the `html` directory of the `modern-cljs`

```bash
cd /path/to/modern-cljs
touch html/reagent.html
```

and copy into it the following very simple `html` code:

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
CLJS compilation as soon as we issue the `boot dev` command to
launch the CLJS development environment.

The only element of the body, aside from the cited `script` tag, is a
`div`. This is very typical of any Single Page Application (SPA) and
it resembles the `index.html` file from the React Tutorial.
   
Also copy the `base.css` file from the React Tutorial to the
`html/css` directory of the `modern-cljs` project to obtain the same
basic page style.

```bash
cp /path/to/react-tutorial/public/css/base.css html/css/
```

We are almost done. Before launching the `boot dev`
command, create the `reagent.cljs` file and require both the
`reagent.core` and the `cljsjs.marked` namespaces into it.

```bash
touch src/cljs/modern_cljs/reagent.cljs
```

```clj
(ns modern-cljs.reagent
  (:require [reagent.core :as r]
            [cljsjs.marked]))
```

> NOTE 4: As we saw in previous tutorials, to be able to use in
> the bREPL a library never used before by other namespaces of the
> project, we first need to require its namespace in a CLJS file,
> otherwise the bREPL is not able to access it.

## Your first Reagent Component

Launch the CLJS development environment as usual:

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

> NOTE 5: we could have launched `boot tdd` instead. But at the moment
> we're not interested in executing any test. We only want to learn
> about Reagent by interacting with it at the bREPL.

As usual, open a new terminal and launch the `boot` client and the
`bREPL` on top of it. Finally visit the
[localhost:3000/reagent.html](http://localhost:3000/reagent.html).

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

> NOTE 6: as soon as you visit the
> [localhost:3000/reagent.html](http://localhost:3000/reagent.html)
> URL the bREPL connects with the JS engine of the browser and it is
> ready to evaluate CLJS expressions.

Now require the `reagent.core` namespace at the bREPL and create your
very first Reagent Component, which, under the wood, will soon become
a React Component.

```clj
cljs.user> (require '[reagent.core :as r :refer [render]])
nil
```

```clj
cljs.user> (defn comment-box []
             [:div "Hello, world! I'm a comment-box"])
#'cljs.user/component-box
```

Believe it or not, such a simple function returning a vector is enough
to create a Reagent Component corresponding to the `CommentBox`
Component we previously created with the following `JSX` code:

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

Don't you believe it? Evaluate the following form at the bREPL:

```clj
cljs.user> (render [comment-box] (.getElementById js/document "content"))
#object[Object [object Object]]
```

Do you see the `Hello, world! I'm a comment-box` text in the page?
The above `render` call corresponds to the `ReactDOM.render` function
call used with React.

> NOTE 7: being clojurean, we used `kebab-case` names
> (i.e. `comment-box`) instead of `CamelCase` names
> (i.e. `CommentBox`).

Does the `[:div "Hello, world! I'm a comment-box"]` vector remind you of something? It uses
the same
[hiccup syntax][5]
we're already accustomed with from the tutorials involving
[hiccups][3] and
[enlive][4] libraries as you can verify
by yourself:

```clj
cljs.user> (require-macros '[hiccups.core :refer [html]])
nil
```

```clj
cljs.user> (require '[hiccups.runtime])
nil
```

```clj
cljs.user> (html [:div "Hello, world! I'm a comment-box"])
"<div>Hello, world! I'm a comment-box</div>"
```

Obviously, if you evaluate the newly defined `comment-box` function at
the bREPL, it just returns a vector adhering to the `hiccup` syntax.

```clj
cljs.user> (comment-box)
[:div "Hello, world! I'm a comment-box"]
```

While the `html` macro from the `hiccups` library parses the hiccup
vector to generate a corresponding html text, the `render` function
from the `reagent` library parses the same hiccup vector to generate a
corresponding React component to be mounted in a DOM node of an html
page:

```clj
cljs.user> (doc html)
-------------------------
hiccups.core/html
([options & content])
Macro
  Render Clojure data structures to a string of HTML.
nil
```

```clj
cljs.user> (doc render)
-------------------------
reagent.core/render
([comp container] [comp container callback])
  Render a Reagent component into the DOM. The first argument may be 
either a vector (using Reagent's Hiccup syntax), or a React element. The second argument should be a DOM node.

Optionally takes a callback that is called when the component is in place.

Returns the mounted component instance.
nil
```

Considering that we still have the `domina` library available in our
project, we can simplify the `render` call as follows:

```clj
cljs.user> (require '[domina.core :as dom :refer [by-id]])
nil
```

```clj
cljs.user> (render [comment-box] (by-id "content"))
#object[Object [object Object]]
```

Let's move on.

## Composing components

The next step in the React Tutorial is to define two new skeleton
components:

* the `CommentList` component, to list comments;
* the `CommentForm` component, to create new comments.

Here is the corresponding code you have to add to the `example.js` JSX
file.

```js
var CommentList = React.createClass({
  render: function() {
    return (
      <div className="commentList">
        Hello, world! I am a CommentList.
      </div>
    );
  }
});

var CommentForm = React.createClass({
  render: function() {
    return (
      <div className="commentForm">
        Hello, world! I am a CommentForm.
      </div>
    );
  }
});
```

At the moment they have exactly the same structure as the previous
`CommentBox` component that we're are going to redefine to include
them inside it as follows:

```js
var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        <h1>Comments</h1>
        <CommentList />
        <CommentForm />
      </div>
    );
  }
});
```

Pretty logical. The `div` component now includes an `h1`, a
`CommentList` and a `CommentForm` component.

If you now reload the [localhost:3001](http://localhost:3001/) page
you should see the following content:

![Composing Component](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tutorial-03.png)

Let's do the same thing with Reagent.

```clj
cljs.user> (defn comment-list []
             [:div "Hello, world! I'm a comment-list"])
#'cljs.user/comment-list
```

```clj
cljs.user> (defn comment-form []
             [:div "Hello, world! I'm a comment-form"])
```

We can now redefine the `comment-box` component by incorporating into
it an `h1`, a `comment-list` and a `comment-form` component as we just
did with the `CommentBox` React component.

```clj
cljs.user> (defn comment-box []
             [:div
              [:h1 "Comments"]
              [comment-list]
              [comment-form]])
#'cljs.user/comment-box
```

Let's re-render the `comment-box` root component of our newly created
component hierarchy:

```clj
cljs.user> (render [comment-box] (by-id "content"))
#object[Object [object Object]]
```

The `reagent.html` page is immediately updated and you should see the following content

![Reagent Composing Components](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-tut-01.png)

> NOTE 8: remember that you always have to define a component
> hierarchy with one `root` component only (e.g. the above `:div`).

So far, so good. We replicated in Reagent the same components
composition with much less incidental complexity (i.e. plumbing code).

Before going on with the next step, let's evaluate again the
`(comment-box)` function at the bREPL

```clj
cljs.user> (comment-box)
[:div
  [:h1 "Comments"]
  [#object[...]]   ;; comment-list function object
  [#object[...]]]  ;; comment-form function object
```

> NOTE 9: the output has been manually simplified to make it more readable

The concept should be evident. This is just a standard application of
the Clojure(Script) evaluation rules for vectors: each item in a
vector is evaluated and when it is a symbol, it evaluates to the value
of the symbol. It just happens that `comment-list` and `comment-box`
are functions, so they evaluate to the corresponding function objects.

I strongly suggest to take your time to read
[one of the best pieces of documentation](https://github.com/Day8/re-frame/wiki/Using-%5B%5D-instead-of-%28%29)
available on Reagent, because it explains very well why you should
never use round parentheses when composing components, even if you
eventually could:

```clj
cljs.user> (defn comment-box []
             [:div
              [:h1 "Comments"]
              (comment-list)   ;; don't do this
              (comment-form)]) ;; don't do this
#'cljs.user/comment-box
```

```clj
cljs.user> (comment-box)
[:div
  [:h1 "Comments"]
  [:div "Hello, world! I'm a comment-list"]
  [:div "Hello, world! I'm a comment-form"]]
```

```clj
cljs.user> (render [comment-box] (by-id "content"))
#object[Object [object Object]]
```

The two scenario becomes clearer if you add, and I suggest you to do
it, the
[React Developer Tools extension](https://github.com/facebook/react-devtools)
to your Google Chrome Browser.

By composing the components with the round parentheses, the Reagent
`render` function produces the following component hierarchy

![Round Parentheses](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-round.png)

while, by composing them with the square brackets, the Reagent
`render` function produces the following component hierarchy:

![Square Brackets](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-square.png)

When you compose components by using the round parentheses instead of
the square brackets, the `render` function will not componentize
them as React component and you'll loose the performance improvement
implemented by the the VDOM differ algorithm.

Let's move on.

## Passing data to components

Let's now create a `Comment` component, which will depend on
the data passed in from its parent (i.e. `ComponentList`).

```js
var Comment = React.createClass({
  render: function() {
    return (
      <div className="comment">
        <h2 className="commentAuthor">
          {this.props.author}
        </h2>
        {this.props.children}
      </div>
    );
  }
});
```

Even without going into details with the JSX syntax, you can see that
the `Comment` component will accept two arguments from its parent:

1. the author of the comment (i.e. `this.props.author`), rendered as
   an `h2` component;
1. the comment itself (i.e. `this.props.children`), rendered as
   regular text node.

We can now redefine the `CommentList` component (i.e. the parent of
the `Comment` component) as follows:

```js
var CommentList = React.createClass({
  render: function() {
    return (
      <div className="commentList">
        <Comment author="Pete Hunt">This is one comment</Comment>
        <Comment author="Jordan Walke">This is *another* comment</Comment>
      </div>
    );
  }
});
```

Note that we have we passed the author property as an attribute and
the text as a child node of the `Comment` component.

If you reload the [localhost:3001](http://localhost:3001/) page you
should see the following content:

![Pass data to components](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-props.png)

Let's replicate the same thing with Reagent. We just need to define a
function with two parameters, `author` and `comment`, which returns a
hiccup vector:

```clj
cljs.user> (defn comment-component [author text]
             [:div
              [:h2 author]
              text])
#'cljs.user/comment-component
```

> NOTE 10: the `comment` symbol is already taken by the `cljs.core`
> namespace. This is why we preferred to name the new component as
> `comment-component`.

```clj
cljs.user> (comment-component "Pete Hunt" "This is a comment")
[:div [:h2 "Pete Hunt"] "This is a comment"]
```

Again, the `comment-component` function can be used as component
inside an hiccup vector. Redefine the `comment-list` component as
follows:

```clj
cljs.user> (defn comment-list []
             [:div
              [comment-component "Paul Hunt" "This is a comment"]
              [comment-component "Jordan Walke" "This *another* component"]])
#'cljs.user/comment-list
```

Let's see the result by re-rendering the `comment-box` root component

```clj
cljs.user> (render [comment-box] (by-id "content"))
#object[Object [object Object]]
```

As soon as you evaluate the `render` function, the DOM of the
`reagent.html` page gets updated and you should see the same content
you previously saw in react.

![Reagent props](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-tut-props.png)

## Adding markdown

While playing with the final web application implemented in the React
Official Tutorial, we previously discovered that it supports
`markdown` markup in the text of a comment. Here is the solution
adopted in the React Tutorial for parsing a `markdown` marked text and
then generating the corresponding HTML markup to be rendered by the
browser.

```js
var Comment = React.createClass({
  rawMarkup: function() {
    var rawMarkup = marked(this.props.children.toString(), {sanitize: true});
    return { __html: rawMarkup };
  },

  render: function() {
    return (
      <div className="comment">
        <h2 className="commentAuthor">
          {this.props.author}
        </h2>
        <span dangerouslySetInnerHTML={this.rawMarkup()} />
      </div>
    );
  }
});
```

As you see, the React tutorial redefines the `Comment` component by
adding to it a `rawMarkup()` function. This function returns a JS
object with one property only: `__html`. Its value is set by the
`marked()` function from the [marked](https://github.com/chjj/marked)
JS library.

The result of the `rawMarkup()` call is set as the value of the
`dangerouslySetInnerHTML` attribute inside a `span` component.

If you now reload the [localhost:3001](http://localhost:3001/) you
should see the word *another* displayed in *italics*.

Let's try to port this solution to Reagent. As you probably remember
from the lessons involving [hiccups][3] and [enlive][4], the
[hiccup syntax][5] allows us to set element attributes in a map passed as second item
of a hiccup vector, like so:

```clj
cljs.user> (html [:div [:label {:for "price"} "Price"]])
"<div><label for=\"price\">Price</label></div>"
```

Here we set `"price"` as the value of the `:for` attribute for the
`:label` element. The same thing is true in Reagent as well. Let's see
it at work.

Now require the `cljsjs.marked` namespace defined in the
`cljsjs/marked` JS library that we added to the `:dependencies`
section of the `build.boot` at the beginning of the tutorial.

```clj
cljs.user> (require '[cljsjs.marked])
nil
```

> NOTE 11: when you require an external JS library prepackaged for
> being used by CLJS you can refer its symbols by using the `js`
> fictitious namespace.

Verify that the `marked` JS library works as expected

```clj
cljs.user> (js/marked "This is *another* comment.")
"<p>This is <em>another</em> comment.</p>\n"
```

```clj
cljs.user> (js/marked "This is <em>another</em> comment." #js {:sanitize true})
"<p>This is &lt;em&gt;another&lt;/em&gt; comment.</p>\n"
```

> NOTE 12: the `js/marked` function expects a JS object as a second
> optional argument. `#js` tagged literal transforms a CLJS structure
> (a CLJS map in this case) into a JS corresponding structure (a JS
> object in this case). #js is not recursive. If you need to transform
> a nested CLJS data structure into a corresponding JS data structure
> use the `clj->js` function.

In the first call, we only passed to the `js/marked` function a string
containing a `markdown` markup. In the second call we also passed a JS
object as a second argument to sanitize a string containing an HTML
markup.

Just for curiosity, test the `js/marked` function inside a call from
the `hiccups` library using a map to set the `dangerouslySetInnerHTML`
attribute for a `span` element as follows:

```clj
cljs.user> (html [:span {:dangerouslySetInnerHTML (js/marked "This is *another* comment" #js {:sanitize true})}])
"<span dangerouslySetInnerHTML=\"&lt;p&gt;This is &lt;em&gt;another&lt;/em&gt; comment&lt;/p&gt;\n\"></span>"
```

OK, it worked. We're now ready to redefine the `comment-component`
component to replicate the same behavior from the React Official
Tutorial.

```clj
cljs.user> (defn comment-component [author comment]
             [:div 
              [:h2 author]
              [:span {:dangerouslySetInnerHTML 
                      #js {:__html (js/marked comment #js {:sanitize true})}}]])
#'cljs.user/comment-component
```

> NOTE 13: the `dangerouslySetInnerHTML` attribute expects a JS object
> as a value. See above for the use of the `#js` tagged literal.

Re-render the `comment-box` as usual

```clj
cljs.user> (render [comment-box] (by-id "content"))
#object[Object [object Object]]
```

Again, you immediately see the *another* word shown in *italics*

![Italics](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-marked.png)

Not so bad.

## Hook up the data model in React

This step requires some code refactoring. Instead of inserting the
comments in the source code, we want to get them from a data
structure. Eventually the data could come from a server.

Here is the new React code from the official tutorial:

```js
var data = [
  {id: 1, author: "Pete Hunt", text: "This is one comment"},
  {Id: 2, author: "Jordan Walke", text: "This is *another* comment"}
];
```

> NOTE 14: note the newly added `id` attribute.

Now, instead of manually instantiating the `Comment` components in the
`CommentList` component, the `CommentBox` component has to get the
comments from the `data` variable and pass them to `CommentList` as follows:

```js
var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        <h1>Comments</h1>
        <CommentList data={this.props.data} />
        <CommentForm />
      </div>
    );
  }
});
```

The code refactoring is not finished yet. First we need to redefine
the `CommentList` in such a way that it receives the comments data
from the `CommentBox` component as follows:

```js
var CommentList = React.createClass({
  render: function() {
    var commentNodes = this.props.data.map(function(comment) {
      return (
        <Comment author={comment.author} key={comment.id}>
          {comment.text}
        </Comment>
      );
    });
    return (
      <div className="commentList">
        {commentNodes}
      </div>
    );
  }
});
```

The updated `render` function creates a new `Comment` node for each
available comment contained in the `data` variable and finally returns
the accumulated `Comment` components it created.

> NOTE 15: note the newly defined `key` attribute getting the value
> from the comment `id`.

Finally we have to refactor the `ReactDOM.render` function as well,
because the `CommentBox` instance now has to get the comments from the
`data` variable as well to be able to pass them to the `CommentList`
component.

```js
ReactDOM.render(
  <CommentBox data={data} />,
  document.getElementById('content')
);
```

Reload the [locahost:3001](http://localhost:3001/). Even if the result
is the same as before, you could potentially get the data from a web
service. 

## Hook up the data model in Reagent

The very first step to hook up the data model in Reagent is as easy as
defining a vector of maps resembling the array of objects from the JS
`data` variable:

```clj
cljs.user> (def data [{:id 1
                       :author "Pete Hunt"
                       :text "This is one comment"}
                      {:id 2
                       :author "Jordan Walke"
                       :text "This is *another* comment"}])
#'cljs.user/data
```

Then we need to refactor the `comment-list` function definition to
receive a list of comments as argument and then to call the
`comment-component` function for each comment of the list.

```clj
cljs.user> (defn comment-list [comments]
             [:div
               (for [{:keys [id author text]} comments] 
                 ^{:key id} [comment-component author text])])
#'cljs.user/comment-list                 
```

The above very succinct code uses a powerful
[destructing ClojureScript idiom](https://clojurefun.wordpress.com/2012/08/13/keyword-arguments-in-clojure/)
within a `for` macro call, known as
[list comprehension](https://clojuredocs.org/clojure.core/for).

From each comment in `comments`, the `{:keys [id author text]}`
destructuring form extracts the values of the `:id`, `:author` and
`:text` keys and assigns them to the `id`, `author` and `text`
symbols. Those values are then passed to the `comment-component`
function to create a new comment. Note that we also used the `^` macro
character to associate the `{:key id}` metadata map to each hiccup
`[comment-component author text]` vector. This is required by the
underlying React lib when you deal with
[dynamic children](http://facebook.github.io/react/docs/multiple-components.html#dynamic-children).

Next we have to consequently update the `comment-box` component to
pass the comments data to the newly defined `comment-list` component.

```clj
cljs.user> (defn comment-box [comments]
             [:div 
              [:h1 "Comments"]
              [comment-list comments]
              [comment-form]])
#'cljs.user/comment-box
```

We are now ready to `render` the `comment-box` component into the
`"content"` `div` of the `reagent.html` page. This time we have to
pass the `data` vector to the `comment-box` so that `comment-list` can
dynamically generate each comment in the `data` vector.


```clj
cljs.user> (render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

Hopefully, even if you do not see any difference in the rendered page,
you should appreciate its improved dynamism.

I suggest you to now stop the `node server.js`, the `boot -c` and the
`boot dev` processes and then freeze into the `example.js` JSX file
and into the `reagent.cljs` file the components you created in this
tutorial.

Here is the content of the `example.js` file

```js
var data = [
  {id: 1, author: "Pete Hunt", text: "This is one comment"},
  {Id: 2, author: "Jordan Walke", text: "This is *another* comment"}
];

var Comment = React.createClass({
  rawMarkup: function() {
    var rawMarkup = marked(this.props.children.toString(), {sanitize: true});
    return { __html: rawMarkup };
  },

  render: function() {
    return (
      <div className="comment">
        <h2 className="commentAuthor">
          {this.props.author}
        </h2>
        <span dangerouslySetInnerHTML={this.rawMarkup()} />
      </div>
    );
  }
});

var CommentList = React.createClass({
  render: function() {
    var commentNodes = this.props.data.map(function(comment) {
      return (
        <Comment author={comment.author} key={comment.id}>
          {comment.text}
        </Comment>
      );
    });
    return (
      <div className="commentList">
        {commentNodes}
      </div>
    );
  }
});

var CommentForm = React.createClass({
  render: function() {
    return (
      <div className="commentForm">
        Hello, world! I am a CommentForm.
      </div>
    );
  }
});

var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        <h1>Comments</h1>
        <CommentList data={this.props.data} />
        <CommentForm />
      </div>
    );
  }
});

ReactDOM.render(
  <CommentBox data={data} />,
  document.getElementById('content')
);
```

An here is the complete content of the `reagent.cljs` file.

```clj
(ns modern-cljs.reagent
  (:require [reagent.core :as r]
            [cljsjs.marked]))

(def data [{:id 1
            :author "Pete Hunt"
            :text "This is one comment"}
           {:id 2
            :author "Jordan Walke"
            :text "This is *another* comment"}])

(defn comment-component [author comment]
  [:div 
   [:h2 author]
   [:span {:dangerouslySetInnerHTML 
           #js {:__html (js/marked comment #js {:sanitize true})}}]])

(defn comment-list [comments]
  [:div
   (for [{:keys [id author text]} comments] 
     ^{:key id} [comment-component author text])])

(defn comment-form []
  [:div "Hello, world! I'm a comment-form"])

(defn comment-box [comments]
  [:div 
   [:h1 "Comments"]
   [comment-list comments]
   [comment-form]])
```

Next, to be prepared for the next part of the tutorial on Reagent, I
suggest you to commit your work on both repositories:

```bash
cd /path/to/react-tutorial
git commit -am "Part I"
```

```bash
cd /path/to/modern-cljs
git add --all .
git commit -m "Part I"
```

## [Next Step - Tutorial 21 - A minimalist interface to React with Reagent (Part II)][6]

In the [next Part II][6] of the tutorial on Reagent we'll first
comment the official React Tutorial on state management and the the
way you could port it to Reagent.

# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-20.md
[2]: https://github.com/cljsjs/packages
[3]: https://github.com/teropa/hiccups
[4]: https://github.com/cgrand/enlive
[5]: https://github.com/weavejester/hiccup/wiki/Syntax
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-22.md
