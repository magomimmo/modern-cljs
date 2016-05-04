# Tutorial 22 - A minimalist interface to React with Reagent (Part II)

In the [previous tutorial][1] we ported to [Reagent][2] the first
steps of the official [React Tutorial][3].

In this Part II of the tutorial on Reagent we're going to complete the
porting of the official [React Tutorial][3] to [Reagent][2], by
introducing component state management.

## Preamble

To start working, assuming you've `git` installed, do as follows to
restart from the end of the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md#save-your-interactive-experience)
for both React Tutorial and its porting on Reagent.

## React Tutorial

If you did not save the state of the React Tutorial at the end of the
[previous tutorial]((https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md#save-your-interactive-experience)),
open a terminal and submit the following commands

```bash
git clone https://github.com/magomimmo/react-tutorial.git
cd react-tutorial
git checkout reagent-tutorial
```

Then from the same terminal install the required `npm` modules and run
the React Tutorial server:

```bash
npm install
PORT=3001 node server.js
```

Finally visit the URL [localhost:3001](http://localhost:3001/).

## Reagent port of React Tutorial

Open a new terminal and enter the following commands

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-21
git checkout -b reagent-tutorial-2
```

Then launch the development environment as usual

```bash
boot dev
```

Next visit the URL
[localhost:3000/reagent.html](http://localhost:3000/reagent.html).
Even if you don't see anything, because we still have to attach the
`comment-box` root component to the `"content"` `div` of the
`reagent.html` page, a websocket connection will be established
between your development environment and your browser's JS engine.

Now open a new terminal and launch the nREPL client followed by the
bREPL client on top of it as usual:

```bash
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> 
```

```clj
boot.user> (start-repl)
...
cljs.user> 
```

We're almost done. To complete the setup for continuing the porting of
the React Tutorial to Reagent, we need to require few namespaces from
the bREPL:

```clj
cljs.user> (require '[reagent.core :as r :refer [render]])
nil
```

```clj
cljs.user> (require '[domina.core :as dom :refer [by-id]])
nil
```

```clj
cljs.user> (require '[modern-cljs.reagent :as tut :refer [comment-box data]])
nil
```

> NOTE 1: the latest requirement is needed to make the `comment-box` and
> `data` symbols visible in the bREPL, because they are now defined in
> the `modern-cljs.reagent` namespace. In the previous bREPL session
> they were defined at the bREPL in the `cljs.user` namespace.

Finally, to attach and render the components we need to call the
Reagent `render`function:

```clj
cljs.user> (render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

You should now see the content of the `reagent.html` page.

## Reactive state

The next steps in the React Tutorial are
[Fetching from the server](https://facebook.github.io/react/docs/tutorial.html#fetching-from-the-server)
and
[Reactive State](https://facebook.github.io/react/docs/tutorial.html#reactive-state). In
this port of the React Tutorial to Reagent we're going to restrict our
scope to the state management only, because it is where Reagent
stands out the most from React.

Let's start by reading the paragraph of the React Tutorial on state
management:

> So far, based on its props, each component has rendered itself
> once. props are immutable: they are passed from the parent and are
> "owned" by the parent. To implement interactions, we introduce mutable
> state to the component. this.state is private to the component and can
> be changed by calling this.setState(). When the state updates, the
> component re-renders itself.

In the subsequent couple of steps the React Tutorial substitutes the
hard-coded `data` with some dynamic from the server. Considering I'm not
going to port to Reagent this part of the React Tutorial, I'm just
copying the corresponding code to facilitate its pasting into the
`example.js` source file. I'll also limit my code comments to a few
things.

```js
// delete the `data` variable definition.
// substitute the previous CommentBox component definition with
// the following one
var CommentBox = React.createClass({
  loadCommentsFromServer: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    this.loadCommentsFromServer();
    setInterval(this.loadCommentsFromServer, this.props.pollInterval);
  },
  render: function() {
    return (
      <div className="commentBox">
        <h1>Comments</h1>
        <CommentList data={this.state.data} />
        <CommentForm />
      </div>
    );
  }
});

// substitute the previous ReactDOM.render call with the following one
ReactDOM.render(
  <CommentBox url="/api/comments" pollInterval={2000} />,
  document.getElementById('content')
);
```

The most important variation from the previous version of `CommentBox`
React component is the substitution of the `this.props.data`
*property* with the `this.state.data` *state* in its `render`
function, initially set to an empty array of comments, to make the
component stateful.

Apparently, the only way to change the state of the component is via
the `setState()` function.

The `setState()` function is indirectly called the very first time,
via the `loadCommentsFromServer()` function, in the body of
`componentDidMount()`. The `componentDidMount()` function is called only
once, immediately after the component has been mounted in the DOM by
the `ReactDOM.render` function. Then, `setState()` is indirectly
called every 2 seconds (i.e. pollInterval), by passing to it the array
of comments obtained by polling the server via ajax.

If you heard that React implements *one-way data flow* communication
model between data and User Interfaces, this is what they mean:

```
data -> CommentBox -> CommentList -> Comment
```

The value of `this.state.data` obtained from the server is passed down
to the `CommentList` component. Each comment contained in
`this.state.data` is then passed to the `Comment` component of the
list. Only new or updated `Comment` components will be redrawn.

This last observation is very important, because it illustrates that a
React component gets updated not only when its private state changes,
but even when they receive new `props` from its owner
(e.g. `CommentBox -> CommentList -> Comment`).

As you are going to see in a moment, Reagent exploits the above React
behavior to not use the component state management offered by React
and, instead, take care of it by itself.

Let's now verify if the above code refactoring works as
expected. First reload the [localhost:3001](http://localhost:3001/)
page. Considering that we still have to implement the `CommentForm`
component to create new comments, to see the React state management at
work you need to manually change the `comment.json` array of comments
which is read every two second by the the `CommentBox` component.

Open the `comments.json` file, which is located in the
`react-tutorial` main folder, and add a new comment to it:

```json
[
  {
    "id": 1388534400000,
    "author": "Pete Hunt",
    "text": "Hey there!"
  },
  {
    "id": 1420070400000,
    "author": "Paul O’Shannessy",
    "text": "React is *great*!"
  },
  {
    "id": 3,
    "author": "Dan Holmsand",
    "text": "Reagent 0.6.0-alpha is here. Check it out! http://reagent-project.github.io/news/news060-alpha.html"
  }
]
```

When you save the file, after a moment you should see that the page has been
updated with the new comment by the
[author of Reagent](https://twitter.com/holmsand).

![Dan Holmsand Comment](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/newcomment.png)

You could even modify one of the comment in the `comments.json` file
to see a reaction in the page as you save the file:

```json
[
  // ...,
  ,
  {
    "id": 3,
    "author": "Dan Holmsand",
    "text": "Reagent 0.4.3 is now, finally, a thing."
  }
]
```

![update a comment](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/updatecomment1.png)

Before porting the component state management from React to Reagent,
we have to digress a little bit about Clojure(Script)
[`atom`](http://clojure.org/reference/atoms).

## On being mundane

Immutability is divine, but sometime you still need to modify the
world around you. ClojureScript has immutability as the default
behavior of its data structures, but it also offers you a more mundane
`atom` function when you really want to manipulate reality.

Let's get some help from the bREPL to learn about `atom` usage. Say
you want to maintain the number of times a button gets clicked

```clj
cljs.user> (def clicks (atom 0))
#'cljs.user/clicks
```

By evaluating `clicks`, you get the `Atom` object itself.

```clj
cljs.user> clicks
#object [cljs.core.Atom {:val 0}]
```

To get the protected value living inside the `clicks` symbol, you need
to dereference it by using the `deref` function:

```clj
cljs.user> (deref clicks)
0
```

The `deref` function is used so frequently that it deserves a
[reader macro](https://yobriefca.se/blog/2014/05/19/the-weird-and-wonderful-characters-of-clojure/)
to shorten its call:

```clj
cljs.user> @clicks
0
```

Every time the button gets clicked, you want to increment its internal
value by using a function (i.e. `inc`):

```clj
cljs.user> (swap! clicks inc)
1
```

```clj
cljs.user> @clicks
1
```

Sometimes you want to reset the number of clicks to a specific
value:

```clj
cljs.user> (reset! clicks 0)
0
```

```clj
cljs.user> @clicks
0
```

Even if changes to atoms are always non blocking and free of race
conditions, this is not the main reason we are talking about them in
the Reagent context.

Say you want to observe the state of `clicks`. You can easily add a
watcher to log its state in time at the `js/console`.

To add a `watcher` you use `add-watch`:

```clj
cljs.user> (doc add-watch)
-------------------------
cljs.core/add-watch
([iref key f])
  Adds a watch function to an atom reference. The watch fn must be a
  fn of 4 args: a key, the reference, its old-state, its
  new-state. Whenever the reference's state might have been changed,
  any registered watches will have their functions called. The watch
  fn will be called synchronously. Note that an atom's state
  may have changed again prior to the fn call, so use old/new-state
  rather than derefing the reference. Keys must be unique per
  reference, and can be used to remove the watch with remove-watch,
  but are otherwise considered opaque by the watch mechanism.  Bear in
  mind that regardless of the result or action of the watch fns the
  atom's value will change.  Example:

      (def a (atom 0))
      (add-watch a :inc (fn [k r o n] (assert (== 0 n))))
      (swap! a inc)
      ;; Assertion Error
      (deref a)
      ;=> 1
nil
```

In this context, we're only interested in logging the new value
(i.e. the fourth argument passed to the watcher function).

```clj
(add-watch clicks :log #(-> %4 clj->js js/console.log))
```

Be sure to have your browser console open on the
[localhost:3000/reagent.html](http://localhost:3000/reagent.html) page
and observe it while updating the state of the `clicks` atom:

```clj
cljs.user> (reset! clicks 0)
0
```

```clj
cljs.user> (swap! clicks + 5)
5
```

```clj
cljs.user> (swap! clicks dec)
4
```

```clj
cljs.user> (swap! clicks dec)
3
```

You should see the state of the `clicks` atom printed at the
browser console every time you change its internal value at the
bREPL. Here, the fundamental word is *time*.

![Watch clicks atom at console](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/atom-console.png)

You can think about the `data` containing comments as an `atom` and
the `comment-box` component as an observer that executes a reaction,
redrawing itself, anytime it observes a change in the state of the
comments.

> NOTE 2:  in reality, as we'll see in a moment, this statement is not
> true.

## From atom to ratom

The
[Reagent tutorials on the re-frame wiki](https://github.com/Day8/re-frame/wiki#reagent-tutorials)
does a terrific job explaining Reagent and I strongly reccomend you
read it several times, until you really grasp the entire content.

That said, Reagent offers its own version of the standard CLJS `atom`
definition, AKA `ratom`, supporting the same `atom` protocol
(e.g. `swap!`, `reset!`, etc.) plus something new, as you can read
from its documentation at the REPL:

```clj
cljs.user> (doc r/atom)
-------------------------
reagent.core/atom
([x] [x & rest])
  Like clojure.core/atom, except that it keeps track of derefs.
Reagent components that derefs one of these are automatically
re-rendered.
nil
```

This is very interesting: whenever a component derefs a `ratom`, it
automagically re-renders itself. This statement is not totally true,
because a Reagent component re-renders itself when it derefs a `ratom`
only if the internal value of the `ratom` is not
[identical](https://github.com/reagent-project/reagent/pull/143) to
the previous one.

## Ratom at work

We previously saw in the React Tutorial that by modifying the array of
comments in the `comments.json` file, periodically read by ajax, we're
creating the condition for a change in the `CommentBox`
`this.state.data` state and, consequently, in the `CommentList` and
`Comment` components as well via the one-way data flow communication
model.

Even without porting the `ajax` stuff from the React Tutorial to
Reagent, by now knowing that a Reagent component could eventually
re-render itself by derefing a `ratom`, we should be able to obtain
the same behavior.

First, open the `reagent.cljs` file and modify the `data` symbol by
wrapping its definition in a `ratom`:

```clj
(def data (r/atom [{:id 1
                    :author "Pete Hunt"
                    :text "This is one comment"}
                   {:id 2
                    :author "Jordan Walke"
                    :text "This is *another* comment"}]))
```

Then modify consequently the `comment-box` component definition by
simply adding the dereference of its comments parameter as follows:

```clj
(defn comment-box [comments]
  [:div 
   [:h1 "Comments"]
   [comment-list @comments]
   [comment-form]])
```

Next, re-render the `comment-box` in the `"content"` div as usual:

```clj
cljs.user> (render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

Finally, swap the content of the `ratom` by adding a new comment to
it:

```clj
cljs.user> (swap! data conj {:id 3 :author "Mimmo Cosenza" :text "Reagent is even better"})
[{:id 1, :author "Pete Hunt", :text "This is one comment"} {:id 2, :author "Jordan Walke", :text "This is *another* comment"} {:id 3, :author "Mimmo Cosenza", :text "Reagent is even better"}]
```

The `comment-box` *view* component should have immediately been updated
with the new comment just added to the `ratom`.

![view update](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/viewupdate.png)

Let's see if the `comment-box` component reacts to an updated comment as well:

```clj
cljs.user> (swap! data assoc-in [(rand-int (count @data)) :text] "This is a **randomly** assigned comment")
[{:id 1, :author "Pete Hunt", :text "This is one comment"} {:id 2, :author "Jordan Walke", :text "This is *another* comment"} {:id 3, :author "Mimmo Cosenza", :text "This is a **randomly** assigned comment"}]
```

> NOTE 3: the text of the comment to be updated is randomly chosen by
> the `(rand-int (count @data))` expression.

Again, a soon as the `swap!` form gets evaluated, you should see the
corresponding components *view* update.

![update comment](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/updatecomment.png)

We just experimented with how Reagent abstracted away, in the `ratom`
structure, the React need to use `this.state.data` instead of
`this.props.data` to manage state management in React components.

Hopefully, you should have also appreciated how the Reagent way of
managing components state made the Reagent/CLJS code much more concise
than the corresponding React/JS code.

The next and last step in porting the React Tutorial to Reagent has to
do with the `CommentForm` component.

## Adding new comments

In the lastest paragraps we were able to programmatically add new
comments to the React Tutorial by just adding them to the
`comments.json`
file. [Mutatis mutandis](https://en.wikipedia.org/wiki/Mutatis_mutandis),
while porting this step from the React Tutorial to Reagent, we did a
similar thing by just adding new comments to the `data` `ratom`.

Let's now afford the problem of adding new comments by using a form
component. The final solution implemented by the React Tutorial
follows:

```js
var CommentForm = React.createClass({
  getInitialState: function() {
    return {author: '', text: ''};
  },
  handleAuthorChange: function(e) {
    this.setState({author: e.target.value});
  },
  handleTextChange: function(e) {
    this.setState({text: e.target.value});
  },
  handleSubmit: function(e) {
    e.preventDefault();
    var author = this.state.author.trim();
    var text = this.state.text.trim();
    if (!text || !author) {
      return;
    }
    this.props.onCommentSubmit({author: author, text: text});
    this.setState({author: '', text: ''});
  },
  render: function() {
    return (
      <form className="commentForm" onSubmit={this.handleSubmit}>
        <input
          type="text"
          placeholder="Your name"
          value={this.state.author}
          onChange={this.handleAuthorChange}
        />
        <input
          type="text"
          placeholder="Say something..."
          value={this.state.text}
          onChange={this.handleTextChange}
        />
        <input type="submit" value="Post" />
      </form>
    );
  }
});
```

The easiest way to understand the code of a React component is to
start reading its `render` function.

The `CommentForm` component is composed of a `form` with three
`input`: two `input` of `text` type and one `input`of `submit` type.

The `text` `input` types get their values from a corresponding state
of the main component: `this.state.author` and
`this.state.text`. Their values are initially set to the void string
`''` by the `getInitialState()` function.

Whenever the user type into one of these `input` components, the
corresponding `onChange` handler gets called
(i.e. `handleAuthorChange` and `handleTextChange`). Those handlers
just set the value of the corresponding state to the one typed in by
the user. So far, so good.

The `handleSubmit` function is called whenever the user click the
`Post` submit button of the `form`. The `handleSubmit` function does
few things:

* it stops the default propagation of the submit event to the server;
* it trims any blank from `this.state.author` and `this.state.text`
  strings;
* it calls the `onCommentSubmit` function passing to it the `author`
  and `text` trimmed strings when they are not blanks and it finally
  clears the `author` and the `text` states by setting again their
  values to the void `''` string.

But where is the `onCommentSubmit` function defined? In the
`CommentBox` component which is the `owner` of the `CommentForm` one:

```js
var CommentBox = React.createClass({
  loadCommentsFromServer: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  handleCommentSubmit: function(comment) {
    var comments = this.state.data;
    // Optimistically set an id on the new comment. It will be replaced by an
    // id generated by the server. In a production application you would likely
    // not use Date.now() for this and would have a more robust system in place.
    comment.id = Date.now();
    var newComments = comments.concat([comment]);
    this.setState({data: newComments});
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      type: 'POST',
      data: comment,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        this.setState({data: comments});
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    this.loadCommentsFromServer();
    setInterval(this.loadCommentsFromServer, this.props.pollInterval);
  },
  render: function() {
    return (
      <div className="commentBox">
        <h1>Comments</h1>
        <CommentList data={this.state.data} />
        <CommentForm onCommentSubmit={this.handleCommentSubmit} />
      </div>
    );
  }
});
```

Again, if you start by reading the `render` function of the
`CommentBox` component, you'll see that it passes the
`handleCommentSubmit` callback function as the value of the
`onCommentSubmit` props of the `CommentForm` sub-component. 

As previously said, we are not going to port to Reagent the ajax and
the server side parts of the React Tutorial and we leave to the
careful reader the understanding of the `handleCommentSubmit`
callback.

To see the updated and final version of the React Tutorial at work,
just reload the [localhost:3001](http://localhost:3001/) URL. Add any
number of comments you want by using the newly added `CommentForm`
component.

> NOTE 4: Thanks to the provided custom backend, you can open more tabs
> in your browser and appreciate the way they get updated anytime you
> add a new comment in one of the tab. As previously said, I'm not going
> to port this feature to Reagent in this tutorial.

## Adding new comments in Reagent

Let's start the porting of the `CommentForm` to Reagent by first
defining the structure of the `comment-form` component as simple as
possible. Considering that the code already ported to Reagent is now
living in the `modern-cljs.reagent` namespace, let's first set this
namespace as the current one in the bREPL.

```clj
cljs.user> (in-ns 'modern-cljs.reagent)
nil
```

Now define the `comment-form` as follows:

```clj
modern-cljs.reagent> (defn comment-form []
                       [:form
                        [:input {:type "text"
                                 :placeholder "Your name"}]
                        [:input {:type "text"
                                 :placeholder "Say something"}]
                        [:input {:type "button"
                                 :value "Post"}]])
#'modern-cljs.reagent/comment-form
```

> NOTE 5: the third `input` of the `form` is of type `button` because
> we're not going to `POST` new comments to the server.

Before rendering the `comment-box` component, if we want to use the
`by-id` function form the `domina` library as we did before, we have
to require its `domina.core` namespace in the `modern-cljs.reagent`
current bREPL namespace as well:

```clj
modern-cljs.reagent> (require '[domina.core :as dom :refer [by-id]])
nil
```

Let's now render the `comment-box` as usual:

```clj
modern-cljs.reagent> (r/render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

You should immediately see the `comment-form` component in your
browser. The next step to replicate the same behavior of the React
Tutorial is to manage the state for the `CommentForm` component.

### Local state and new ways to create component

We already used a `ratom` to manage the state of the `data` vector of
maps recording comments. But this case is different. We need a local
state, not a global one as it is `data`.

Before to be able to create a local `ratom` to manage the local state
of a Reagent component, we need to digress about a very important
Reagent topic: the three different ways to create a Reagent component.

Until now we only used the most simple way, known as `form-1`, for
creating a Reagent component: a simple function definition returning a
hiccup vector

```clj
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
  [:form
    [:input {:type "text"
             :placeholder "Your name"}]
    [:input {:type "text"
             :placeholder "Say something"}]
    [:input {:type "button"
     :value "Post"}]])

(defn comment-box [comments]
  [:div 
   [:h1 "Comments"]
   [comment-list @comments]
   [comment-form]])
```

The second way of creating a Reagent component, known as `form-2`, is
to define a function returning a function which, in turn, returns an
hiccup vector. 

The `form-2` way of creating a Reagent component is used whenever you
need some initial setup for the component which has to be executed
only once, at its creation.

This is exactly what we need to setup a local state for the
`comment-form` component which initially set both the `author` and the
`text` props to the void string `""`. Something like the following:

```clj
modern-cljs.reagent> (defn comment-form []
                       (let [comment (r/atom {:author "" :text ""})] 
                         (fn [] 
                           [:form
                            [:input {:type "text"
                                     :placeholder "Your name"
                                     :value (:author @comment)}]
                            [:input {:type "text"
                                     :placeholder "Say something"
                                     :value (:text @comment)}]
                            [:input {:type "button"
                                     :value "Post"}]])))
```

As you see, we first create a local `ratom` comment, internally
represented as a map with its `:author` and `:text` keys initialized
to the void string `""`. Then we return an anonymous function that, in
turn, returns the usual hiccup vector to be subsequently rendered. 

Also note as we set the `input` attributes using a map, as we already
did within `comment-component` to set the `dangerouslySetInnerHTML`
attribute for its included `span` component.

By using the map in the hiccup vector we were able to set the `value`
attribute of both `author` and `text` inputs to the corresponding
protected value of the local `ratom` by derefing it. This way, anytime
the mutable `comment` ratom gets swapped or reset, the corresponding
`input` component gets the opportunity to be re-rendered.

So far, so good. But if you try to re-render the `comment-box` root
component at the bREPL

```clj
modern-cljs.reagent> (r/render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

you'll see that both the `author` and the `text` input components do
not take any of your typing.

This is because we have not defined any
handler for the `:on-change` event.

> NOTE 6: as previously said, React uses `CamelCase` names for
> component names. For events, it uses `camelCase` names. Conversely,
> Reagent uses `kebab-case` names for components and keywordized
> `:kebak-case` names for both events and component attributes.

Let's add the `:on-change` event handler to our input components:

```clj
modern-cljs.reagent> (defn comment-form []
  (let [comment (r/atom {:author "" :text ""})] 
    (fn [] 
      [:form
       [:input {:type "text"
                :placeholder "Your name"
                :value (:author @comment)
                :on-change #(swap! comment assoc :author (-> %
                                                             .-target
                                                             .-value))}]
       [:input {:type "text"
                :placeholder "Say something"
                :value (:text @comment)
                :on-change #(swap! comment assoc :text (-> %
                                                           .-target
                                                           .-value))}]
       [:input {:type "button"
                :value "Post"}]])))
#'modern-cljs.reagent/comment-form
```

As you see, each `:on-change` handler gets the value of the target of
the `:on-change` event and sets it as the value of the corresponding
key of the *ratomized* `comment` map by using the `swap!` function.

If you re-render the `comment-box` root component the `input`
component of the `comment-form` component is now taking your typing

```clj
modern-cljs.reagent> (r/render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

The very last step in porting to Reagent the final version of the
React Tutorial concerns the `:on-click` event associated with the
`Post` button.

> NOTE 7: as we decided to not port to Reagent the custom backend and
> the corresponding ajax call to set/get comments to/from it, we
> substituted the `submit` input type with the `button` input
> type. Consequently, instead of having to manage the form
> `:on-submit` event we need to manage its `:on-click` event.

The `:on-click` handler associated with the `comment-form` has to do few things:

* get the values of `:author` and `:text` keys from the *ratomized*
  comment and `trim` them;
* reset those keys' to the void string `""`;
* when neither the `author` or the `text` value is `blank?`, add the
  comment to the global `data` ratom providing each of them with a
  kind of unique identifier as React Tutorial did.

The `trim` function and the `blank?` predicate are included in the
`clojure.string` namespace. So we need to require it at the bREPL:

```clj
modern-cljs.reagent> (require '[clojure.string :as s :refer [trim blank?]])
nil
```

```clj
modern-cljs.reagent> (trim "   trim me    ")
"trim me"
```

```clj
modern-cljs.reagent> (blank? nil)
true
modern-cljs.reagent> (blank? "    ")
true
modern-cljs.reagent> (blank? "")
true
modern-cljs.reagent> (blank? "\t")
true
modern-cljs.reagent> (blank? "\n")
true
```

We can now define the `handle-comment-on-click` handler as follows:

```clj
modern-cljs.reagent> (defn handle-comment-on-click [comment]
                       (let [author (trim (:author @comment))
                             text (trim (:text @comment))]
                         (reset! comment {:author "" :text ""})
                         (when-not (or (blank? author) (blank? text))
                           (swap! data conj {:id (.getTime (js/Date.)) :author author :text text}))))
#'modern-cljs.reagent/handle-comment-on-click
```

Note as we *derefed* the *ratomized* comment to get the values from
its `:author` and `:text` keys. Also note that we created an unique
identifier for each new comment by miming the same `Date` and
`getTime` JS constructor/function. Finally, when neither of those two
value is blank, we `conj` the newly created comment to the global
`data` ratom.

The very last step is to redefine the `comment-form` to include the
`:on-click` handler:

```clj
modern-cljs.reagent> (defn comment-form []
                       (let [comment (r/atom {:author "" :text ""})] 
                         (fn [] 
                           [:form
                            [:input {:type "text"
                                     :placeholder "Your name"
                                     :value (:author @comment)
                                     :on-change #(swap! comment assoc :author (-> %
                                                                                  .-target
                                                                                  .-value))}]
                            [:input {:type "text"
                                     :placeholder "Say something"
                                     :value (:text @comment)
                                     :on-change #(swap! comment assoc :text (-> %
                                                                                .-target
                                                                                .-value))}]
                            [:input {:type "button"
                                     :value "Post"
                                     :on-click #(handle-comment-on-click comment)}]])))
#'modern-cljs.reagent/comment-form
```

Re-render the `comment-box` root component as usual:

```clj
modern-cljs.reagent> (r/render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

You should now be able to add new comments by using the `comment-form`
component as you previously did with the React Tutorial. Because we
did not port the custom backend and the corresponding ajax call to
set/get the comments from the backend.


# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md
[2]: http://reagent-project.github.io/
[3]: https://facebook.github.io/react/docs/tutorial.html

