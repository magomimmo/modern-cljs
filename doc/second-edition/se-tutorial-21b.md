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
[Virtual DOM](http://facebook.github.io/react/docs/working-with-the-browser.html#the-virtual-dom)(VDOM)
maintaining a fast and immutable in-memory representation of the DOM,
my personal opinion is that the VDOM and its differing algorithm only
represent implementation details. To me, the most fundamental aspects
of React.js are:

* [one-way data flow, AKA one-way binding](https://youtu.be/nYkdrAPrdcw?list=PLb0IAmt7-GS188xDYE-u1ShQmFFGbrk0v);
* [composability model](https://facebook.github.io/react/docs/multiple-components.html);
* [synthetic event system](http://facebook.github.io/react/docs/interactivity-and-dynamic-uis.html#event-handling-and-synthetic-events);

Even if I appreciate a lot React.js (and React Native as well) I still
think that ClojureScript is a superior programming language when
compared with JS and with
[JSX](http://facebook.github.io/react/docs/jsx-in-depth.html) as well.

To let me explain my opinion and, at the same time, to introduce you to [Reagent](http://reagent-project.github.io/), it can be very useful to start this tutorial by porting to Reagent the [standard React.js introductory tutorial](https://facebook.github.io/react/docs/tutorial.html).

## Install React Tutorial

The best way to appreciate Reagent is to first try out the standard
React.js tutorial by downloading and locally running it on your
computer.

### Install node.js

First you need to install [node.js](https://nodejs.org/en/). I
personally prefer to be able to install it by using
[nvm](https://github.com/creationix/nvm), a simple script to install
and manage different node versions.

The easiest way to install nvm is by its install script as follow:

```bash
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.0/install.sh | bash
```

> NOTE 1: On OSX, if you get `nvm: command not found` after running
> the install script, your system may not have a `.bash_profile` file
> where the command is set up. Simple create one with `touch
> ~/.bash_profile` and run the install script again.

Then you can install node.js by issuing the following command:

```bash
nvm install 5.0
```

You can now verify that both node.js and [npm]() have been installed:

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

![React Tutorial](modern-cljs/doc/images/react-tut-01.png)

As you see this is a very basic web application, but it contains very
important concept to be grasped to better understand as `React.js`
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

![React Tutorial new comments](modern-cljs/doc/images/react-tut-02.png)

Nothing new under the sun. What is new, aside from the
performance that we can't appreciate with such a simple sample, it is hidden
under the wood.

> NOTE 3: In the above image you'll note that the first added comment
> contains a link and the second contains a word in bold. This is
> because the `<input>` element for the comment is able to parse
> `markdown` text.



## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-20.md
