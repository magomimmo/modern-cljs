;; +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
;; An Introduction to ClojureScript for Emacs/Eclipse-CCW users 
;; +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

;; NOTE: Mutatis matandis, any editor/IDE supporting nREPL should do
;; work as well. At the moment only Eclipse/CCW has been tested, but
;; requires the Browser Connected REPL scenario.

;; Basics
;; ============================================================================

;; Clone the repo
;; ----------------------------------------------------------------------------

;; To be able to run the tutorial you need to clone the repo

;; git clone https://github.com/magomimmo/modern-cljs.git 

;; You find this file under the
;; modern-cljs/cljs-tutorial/src/cljs/cljs_tutorial directory

;; Emacs
;; ----------------------------------------------------------------------------

;; Open this file in your emacs, type `C-c M-j` and evaluate
;; `(cemerick.austin.repls/exec)` in the Clojure REPL.  Do not care
;; about the WARNINGs. Now you can evaluate all the forms in this file
;; by placing the cursor after the form and typing `C-c C-e`.

;; Eclipse/CCW
;; ----------------------------------------------------------------------------

;; 1. Create a new general project: New -> Project -> General -> Project

;; 2. Type the project name: e.s. `ClojureScript Tutorial` 

;; 3. Uncheck `Use  default location` checkbox 

;; 4. Click `Browser` to select the `cljs-tutorial` directory from the
;; `modern-cljs` main directory and then `Finish`

;; 5. Convert the project: Configure -> Convert to Leiningen Project from
;; the contextual menu of the newly created project.

;; 6. Run the REPL: Leiningen -> Launch Headless REPL for the Project
;; from the contextual menu of the project and wait for the
;; ClojureScript compilation completion. You're now in the REPL.

;; 7. Run the included web server: evaluate `(run)` in the REPL

;; 8. Create the bREPL connection: evalute `(broswer-repl)` in the REPL

;; 9. Activate the bREPL connection: visit http://localhost:3000

;; 10. Open this file: double click it in the Package Explorer

;; 11. Evaluate a ClojureScript form: position the cursor after a form
;; and Cmd/Ctrl Enter to evaluate the form.

;; IMPORTANT: You must evaluate the very first form, the namespace
;; definition.

;; Declaring a namespaces
;; ----------------------------------------------------------------------------

;; ClojureScript supports modularity via namespaces. They allow you to group
;; logical definitions together.

(ns cljs-tutorial.core
  (:require [clojure.string :as string]))

;; :require is how you can import functionality from a different namespace into
;; the current one. Here we are requiring `clojure.string` and giving it an
;; alias. We could write the following:

(clojure.string/blank? "")

;; But that's really verbose compared to:

(string/blank? "")


;; Comments
;; ----------------------------------------------------------------------------

;; There are three ways to create comments in ClojureScript. The first way is
;; by preceding a line with a semi-colon, just like the lines you are reading
;; now.

;; The second way is by preceding a form with `#_`. This causes ClojureScript
;; to skip the evaluation of only the form immediately following, without
;; affecting the evaluation of the surrounding forms.

;; Try to reveal the secret message below:

(str "The secret word is " #_(string/reverse "tpircSerujolC"))

;; Finally, you can also create a comment using the `comment` macro. One common
;; technique is to use the `comment` macro to include code to be evaluated in a
;; REPL, but which you do not normally want to be included in the compiled
;; source.

;; For example, try placing your cursor after the last `)` below and type
;; `C-c C-e`:

(comment

  (string/upper-case "This is only a test...")

  )

;; The `comment` macro makes the whole form return `nil`. Now go up
;; one line (i.e. after the closing parens of the `'upper-case` form)
;; and type `C-c C-e`. In this way you can include code samples or
;; quick tests in-line with the rest of your code.


;; Definitions
;; ----------------------------------------------------------------------------

;; Once you have a namespace you can start creating top level definitions in
;; that namespace.

;; You can define a top level with `def`.

(def x 1)

x

;; You can also refer to top level definitions by fully qualifying them.

cljs-tutorial.core/x

;; This means top levels can never be shadowed by locals and function
;; parameters.

(let [x 2]
  cljs-tutorial.core/x)

;; One way to define a function is like this.

(def y (fn [] 1))

(y)

;; Defining functions in ClojureScript is common enough that `defn` sugar is
;; provided and idiomatic.

(defn z [] 1)

(z)

;; Literal data types
;; ----------------------------------------------------------------------------

;; ClojureScript comes out of the box with the usual useful data literals.

;; Booleans

(def a-boolean true)

;; Strings

(def a-string "Hello!")

;; Regular Expressions

(def a-regexp #"\d{3}-?\d{3}-?\d{4}")

;; Numbers

(def a-number 1)


;; Function literals
;; ----------------------------------------------------------------------------

;; ClojureScript also supports a short hand function literal which is useful
;; You can use the % and %N placeholders to represent function arguments.

;; You should not abuse the function literal notation as it degrades readability
;; outside of simple cases. It is nice for simple functional cases such as
;; the following. 

(map (fn [n] (* n 2)) [1 2 3 4 5])

;; more on vector literals and map function later

(map #(* % 2) [1 2 3 4 5])

;; Did you note that we passed an anonymous function as first argument
;; of the `map` function? Nothing new for a JavaScript programmer
;; JavaScript got some inspiration from the Lisp land.

;; JavaScript data type literals
;; ----------------------------------------------------------------------------

;; You can construct a JavaScript array with the `array` function.

(def an-array (array 1 2 3))

;; But ClojureScript also supports JavaScript data literals via the `#js`
;; reader literal.

(def another-array #js [1 2 3])

;; Similarly you can create simple JavaScript objects with `js-obj`.

(def an-object (js-obj "foo" "bar"))

;; But again you can save a few characters with `#js`.

(def another-object #js {"foo" "bar"})

;; It's important to note that `#js` is shallow, the contents of `#js` will be
;; ClojureScript data unless preceded by `#js`.

;; This is a mutable JavaScript object with an immutable ClojureScript vector
;; inside.

(def shallow #js {"foo" [1 2 3]})


;; Constructing a type
;; ----------------------------------------------------------------------------

;; Of course some JavaScript data types you will want to create with
;; constructor.

;; (js/Date.) is equivalent to new Date().

(def a-date (js/Date.))

(def another-date #inst "2014-01-15")

;; Note the above returns an `#inst` data literal.

(def another-regexp (js/RegExp. "\\d{3}-?\\d{3}-?\\d{4}"))

;; Handy

;; NOTE: js/Foo is how you refer to global JavaScript entities of any kind.

js/Date

js/RegExp

js/requestAnimationFrame ; this only works with a Browser Connected REPL

;; If you're curious about other JavaScrip interop jump to the bottom of this
;; tutorial.


;; ClojureScript data types
;; ============================================================================

;; Unless there is a good reason you should generally write your ClojureScript
;; programs with ClojureScript data types. They have many advantages over
;; JavaScript data types - they present a uniform API and they are immutable.

;; Vectors
;; ----------------------------------------------------------------------------

;; Instead of arrays ClojureScript programmers use persistent vectors, they are
;; like arrays - they support efficient random access, efficient update
;; and efficient addition to the end.

(def a-vector [1 2 3 4 5])

;; We can get the length of a vector in constant time via `count`.

(count a-vector)

;; We can add an element to the end.

(def another-vector (conj a-vector 6))

;; Note this does not mutate the array! `a-vector` will be left
;; unchanged.

a-vector

another-vector

;; Hallelujah! Here is where some ClojureScript magic
;; happens. `another-vector` appears to be a completely new vector
;; compared to `a-vector`. But is not really so. Internally, the new
;; vector efficientely shares the `a-vector` structure. In this way you
;; get the benefits of immutability without paying in performance.

;; We can access any element in a vector with `nth`. The followings
;; will return the second element.

(nth a-vector 1)

(nth ["foo" "bar" "baz"] 1)

;; or with `get`

(get a-vector 0)

;; which allows you to return an alternate value when the index is
;; out-of bounds.

(get a-vector -1 :out-of-bounds)
(get a-vector (count a-vector) :out-of-bounds)

;; Surprisingly vectors can be treated as functions. This is actually
;; a very useful property for associative data structures to have as
;; we'll see below with sets.

(a-vector 1)

(["foo" "bar" "baz"] 1)

;; Maps
;; ----------------------------------------------------------------------------

;; Along with vectors maps are the most common data type in ClojureScript.
;; Map usage is analogous to the usage of Object in JavaScript, but
;; ClojureScript maps are immutable and considerably more flexible.

;; Let's define a simple map. Note `:foo` is a ClojureScript keyword.
;; ClojureScript programmers prefers to use keywords for keys instead
;; of strings. They are more distinguishable from the rest of the
;; code, more efficient than plain strings and they can be used in
;; function position (i.e. first position after the open parens), as
;; we'll see in a moment.

(def a-map {:foo "bar" :baz "woz"})

;; We can get the number of key-value pairs in constant time.

(count a-map)

;; We can access a particular value for a key with `get`.

(get a-map :foo)

;; and return an alternative value when the key is not present

(get a-map :bar :not-found)

;; We can add a new key-value pair with `assoc`.

(def another-map (assoc a-map :noz "goz"))

;; Again a-map is unchanged! Same magic as before for vectors

a-map

another-map

;; We can remove a key value pair with `dissoc`.

(dissoc a-map :foo)

;; Again a-map is unchanged!

a-map

;; Like vectors maps can act like functions.

(a-map :foo)

;; However ClojureScript keywords themselves can act like functions and the
;; following is more idiomatic.

(:foo a-map)

;; We can check if a map contains a key, with `contains?`.

(contains? a-map :foo)

;; We can get all the keys in a map with `keys`.

(keys a-map)

;; And all of the values with `vals`.

(vals a-map)

;; We can put a lot of things in a map, even other maps

(def a-nested-map {:customer-id 1e6 
                   :preferences {:nickname "Bob"
                                 :avatar "http://en.gravatar.com/userimage/0/0.jpg"}
                   :services {:alerts {:daily true}}})

;; and navigate its keys to get the nested value you're interested in

(get-in a-nested-map [:preferences :nickname])
(get-in a-nested-map [:services :alerts :daily])

;; or just find a top level key-value pair (i.e. MapEntry) by key

(find a-nested-map :customer-id)
(find a-nested-map :services)

;; There are many cool ways to create maps.

(zipmap [:foo :bar :baz] [1 2 3])

(hash-map :foo 1 :bar 2 :baz 3)

(apply hash-map [:foo 1 :bar 2 :baz 3])

(into {} [[:foo 1] [:bar 2] [:baz 3]])

;; Unlike JavaScript objects ClojureScript maps support complex keys.

(def complex-map {[1 2] :one-two [3 4] :three-four})

(get complex-map [3 4])


;; Keyword digression
;; ----------------------------------------------------------------------------

;; Let's take a moment to digress about keywords as they are so ubiquitous
;; in ClojureScript code.

(identity :foo)

;; If you add an additional preceding colon you'll get namespaced keyword.

(identity ::foo)

;; What good is this for? It allows you to put data into collections without
;; fear of namespace clashes without the tedium of manual namespacing them
;; in your source.

(identity {:user/foo ::foo})

;; Sets
;; ----------------------------------------------------------------------------

;; ClojureScript also supports sets.

(def a-set #{:cat :dog :bird})

;; `:cat` is already in `a-set`, so it will be unchanged.

(conj a-set :cat)

;; But `:zebra` isn't.

(conj a-set :zebra)

;; If you haven't guessed already, `conj` is a "polymorphic" function that adds
;; an item to a collection. This is some of the uniformity we alluded to
;; earlier.

;; `contains?` works on sets just like they do on maps.

(contains? a-set :cat)

;; Like vectors and maps, sets can also act as functions. If the argument
;; exists in the set it will be returned, otherwise the set will return nil.

(#{:cat :dog :bird} :cat)

;; This is powerful when combined with conditionals.

(defn check [x]
  (if (#{:cat :dog :bird} x)
    :valid
    :invalid))

(check :cat)
(check :zebra)

;; Lists
;; ----------------------------------------------------------------------------

;; A less common ClojureScript data structure is lists. This may be
;; surprising as ClojureScript is a Lisp, but maps, vectors and sets
;; are the goto for most applications. Still lists are sometimes
;; useful, expecially when dealing with code (i.e. code is data).

(def a-list '(:foo :bar :baz))

;; `conj` is "polymorphic" on lists as well and it's smart enough to
;; add the new item in the most efficient way on the basis of the
;; collection type.

(conj a-list :front)  

;; and lists are immutable as well

a-list

;; You can get the first element of a list

(first a-list)

;; or the tail of a list

(rest a-list)

;; which allows you to easly verify how ClojureScript shares data
;; structure instead of inefficiently copying data for supporting
;; immutability.

(def another-list (conj a-list :front))

another-list

a-list

(identical? (rest another-list) a-list)

;; `identical?` checks whether two things are represented by the same
;; thing in memory with.

;; Equality
;; ============================================================================

;; ClojureScript has a much simpler notion of equality than what is present
;; in JavaScript. In ClojureScript equality is always deep equality.

(= {:foo "bar" :baz "woz"} {:foo "bar" :baz "woz"})

;; Maps are not ordered.

(= {:foo "bar" :baz "woz"} {:baz "woz" :foo "bar"})

;; For sequential collections equality just works.

(= [1 2 3] '(1 2 3))

;; Again, it is possible to check whether two things are represented
;; by the same thing in memory with `identical?`.

(def my-vec [1 2 3])
(def your-vec [1 2 3])

(identical? my-vec your-vec)

;; Control
;; ============================================================================

;; In order to write useful programs we need to be able to express
;; control. ClojureScript provides the usual control constructs,
;; however truth-y and false-y values are not the same as in
;; JavaScript so it's worth reviewing.

;; if
;; ----------------------------------------------------------------------------

;; 0 is not a false-y value.

(if 0
  "Zero is not false-y"
  "Yuck")

;; Nor is the empty string.

(if ""
  "An empty string is not false-y"
  "Yuck")

;; the empty vector

(if []
  "An empty vector is not false-y"
  "Yuck")

;; the empty list

(if ()
  "An empty list is not false-y"
  "Yuck")

;; the empty map

(if {}
  "An empty map is not false-y"
  "Yuck")

;; the empty set

(if #{}
  "An empty set is not false-y"
  "Yuck")

;; and even the empty regexp

(if #""
  "An empty regexp is not false-y"
  "Yuck")

;; The only false-y values in ClojureScript are `nil` and `false`. `undefined`
;; is not really a valid ClojureScript value and is generally coerced to `nil`.


;; cond
;; ----------------------------------------------------------------------------

;; Nesting `if` tends to be noisy and hard to read so ClojureScript
;; provides a `cond` macro to deal with this.

(cond
  nil "Not going to return this"
  false "Nope not going to return this either"
  :else "Default case")

;; loop/recur
;; ----------------------------------------------------------------------------

;; The most primitive looping construct in ClojureScript is loop/recur. Most
;; of the iteration constructs are defined in terms of it. Using loop/recur is
;; usually considered bad style if a reasonable functional solution via
;; map/filter/reduce or a list comprehension is possible.

(loop [i 0 ret []]
  (if (< i 10)
    (recur (inc i) (conj ret i))
    ret))


;; Moar functions
;; ============================================================================

;; Functions are the essence of any significant ClojureScript program so
;; we will dive into features that are unique to ClojureScript functions that
;; might be unfamiliar.

;; Here is a simple function that takes two arguments and adds them.

(defn foo1 [a b]
  (+ a b))

(foo1 1 2)

;; Functions can have multiple arities.

(defn foo2
  ([a b] (+ a b))
  ([a b c] (* a b c)))

(foo2 3 4)
(foo2 3 4 5)

;; Multiple arities can be used to supply default values.

(defn defaults
  ([x] (defaults x :default))
  ([x y] [x y]))

(defaults :explicit)
(defaults :explicit1 :explicit2)

;; Functions support rest arguments.

(defn foo3 [a b & d]
  [a b d])

(foo3 1 2)
(foo3 1 2 3 4)

;; You can apply functions.

(apply + [1 2 3 4 5])


;; multimethods
;; ----------------------------------------------------------------------------

;; Often when you need some polymorphism and performance isn't an issue
;; multimethods will suffice. Multimethods are functions that allow open
;; extension, but instead of limiting dispatch to type, dispatch is controlled
;; by whatever value the dispatch fn originally supplied to defmulti returns.

;; Here is a function that takes a list. It dispatches on the first element
;; of the list!

(defmulti parse (fn [[f & r :as form]] f))

(defmethod parse 'if
  [form] {:op :if})

(defmethod parse 'let
  [form] {:op :let})

(parse '(if a b c))
(parse '(let [x 1] x))


;; Scoping
;; ============================================================================

;; Unlike JavaScript there is no hoisting in
;; ClojureScript. ClojureScript has lexical scoping.


(def some-x 1)

(let [some-x 2]
  some-x)

some-x

;; Closures
;; ----------------------------------------------------------------------------

;; Could a language with such a name miss closures? Sure it can't. You
;; may already familiar with them in JavaScript, even if it's a
;; variable scoped language.

(let [a 1e3]
  (defn foo []
    (* a a))
  (defn bar []
    (+ (foo) a)))

;; Above we defined `foo` and `bar` functions inside the scope of a
;; `let` form and they both know about `a` (i.e. they close over `a`)

(foo)
(bar)

;; And Nobody else. 

(comment 
  (defn baz []
    (type a))
  (baz)
  )

;; That's why some people say closures are the poor's man objects.
;; They encapsulate the information as well. 

;; But in ClojureScript functions parameters and let bindings locals
;; are not mutable! And loop locals too!

(let [fns (loop [i 0 ret []]
            (if (< i 10)
              (recur (inc i) (conj ret (fn [] i)))
              ret))]
  (map #(%) fns))

;; In JavaScript you would see a list of ten 9's. In ClojureScript we
;; see the expected numbers from 0 to 9.


;; Destructuring
;; ============================================================================

;; In any serious ClojureScript program there will be significant amounts of
;; data manipulation. Again we will see that ClojureScript's uniformity
;; pays off.

;; In ClojureScript anywhere bindings are allowed like `let` or function
;; parameters destructuring is allowed. This is similar to the destructuring
;; proposed for ES6, but the system provided in ClojureScript benefits from
;; all the collections supporting uniform access.


;; Sequence destructuring
;; ----------------------------------------------------------------------------

;; Destructuring sequential types is particularly useful.

(let [[f & r] '(1 2 3)]
  f)

(let [[f & r] '(1 2 3)]
  r)

(let [[r g b] [255 255 150]]
  g)

;; _ is just a convention for saying that you are not interested at the
;; item in the corresponding position. it has no other special meaning.
;; Here we're only interested at the third local variable named `b`.

(let [[_ _ b] [255 255 150]]
  b)

;; destructuring function arguments works just as well. Here we are
;; only intersted at the second argument `g`.

(defn green [[_ g _]] g)

(green [255 255 150])


;; Map destructuring
;; ----------------------------------------------------------------------------

;; Map destructuring is also useful. Here we destructure the value for the
;; `:foo` key and bind it to a local `f`, and the value for `:baz` key
;; and bind it to a local `b`.

(let [{f :foo b :baz} {:foo "bar" :baz "woz"}]
  [f b])

;; If we don't want to rename we can just use `:keys`.

(let [{:keys [first last]} {:first "Bob" :last "Smith"}]
  [first last])

;; the above map destructuring form is very useful when you need to
;; define a function with optional, non positional and defaulted
;; arguments.

(defn magic [& {:keys [k g h]
                :or {k 1
                     g 2
                     h 3}}]
  (hash-map :k k 
            :g g
            :h h))

(magic)
(magic :k 10)
(magic :g 100)
(magic :h 1000)
(magic :k 10 :g 100 :h 1000)
(magic :h 1000 :k 10 :g 100)
 

;; Sequences
;; ============================================================================

;; We said that ClojureScript data structures are preferred as they
;; provide a uniform interface. All ClojureScript collections satisfy
;; the ISeqable protocol, which means iteration is uniform
;; (i.e. polymorphic) for all collection types.


;; Map / Filter / Reduce
;; ----------------------------------------------------------------------------

;; ClojureScript supports the same bells and whistles out of the box you may
;; be familiar with from other functional programming languages or JavaScript
;; libraries such as Underscore.js

(map inc [0 1 2 3 4 5 6 7 8 9])

(filter even? (range 10))

(remove odd? (range 10))

;; ClojureScript's map and filter operations are lazy. You can stack up
;; operations without getting too concerned about multiple traversals.

(map #(* % %) (filter even? (range 20)))

(reduce + (range 100))


;; List comprehensions
;; ----------------------------------------------------------------------------

;; ClojureScript supports list comprehensions you might know from various
;; languages. List comprehensions are sometimes more natural / readable
;; than a chain of map and filter operations.

(for [x (range 1 10)
      y (range 1 10)]
  [x y])

(for [x (range 1 10)
      y (range 1 10)
      :when (and (zero? (rem x y))
                 (even? (quot x y)))]
  [x y])

(for [x (range 1 10)
      y (range 1 10)
      :let [prod (* x y)]]
  [x y prod])


;; Seqable collections
;; ----------------------------------------------------------------------------

;; Most ClojureScript collections can be coerced into sequences.

(seq {:foo "bar" :baz "woz"})
(seq #{:cat :dog :bird})
(seq [1 2 3 4 5])
(seq '(1 2 3 4 5))

;; Many ClojureScript functions will call `seq` on their arguments in order to
;; provide the expected behavior. The following demonstrates that you can
;; uniformly iterate over all the ClojureScript collections!

(first {:foo "bar" :baz "woz"})
(rest {:foo "bar" :baz "woz"})

(first #{:cat :dog :bird})
(rest #{:cat :dog :bird})

(first [1 2 3 4 5])
(rest [1 2 3 4 5])

(first '(1 2 3 4 5))
(rest '(1 2 3 4 5))


;; Metadata
;; ============================================================================

;; All of the ClojureScript standard collections support metadata. Metadata
;; is a useful way to annotate data without affecting equality. The
;; ClojureScript compiler uses this language feature to great effect.

;; You can add meta data to a ClojureScript collection with `with-meta`. The
;; metadata must be a map.

(def plain-data [0 1 2 3 4 5 6 7 8 9])

(def decorated-data (with-meta plain-data {:url "http://lighttable.com"}))

;; Metadata has no effect on equality.

(= plain-data decorated-data)

;; You can access metadata with `meta`.

(meta decorated-data)

;; Error Handling
;; ============================================================================

;; Error handling in ClojureScript is relatively straightforward and more or
;; less similar to what is offered in JavaScript.

;; You can construct an error like this.

(js/Error. "Oops")

;; You can throw an error like this.

(throw (js/Error. "Oops"))

;; You can catch an error like this.

(try
  (throw (js/Error. "Oops"))
  (catch js/Error e
    e))

;; JavaScript unfortunately allows you to throw anything. You can handle
;; this in ClojureScript with the following.

(try
  (throw (js/Error. "Oops"))
  (catch :default e
    e))


;; Mutation
;; ============================================================================

;; Atoms
;; ----------------------------------------------------------------------------

;; A little bit of mutability goes a long way. ClojureScript does not offer
;; any traditional mutable data structures, however it does support identities
;; that can evolve over time via atom.

(def x (atom 1))

;; You can dereference the value of an atom with `@`.

@x

;; This is equivalent to calling `deref`.

(deref x)

;; If you want to change the value of an atom you can use `reset!` which returns
;; the new value. It's idiomatic to add the bang char `!` at the end of function
;; names mutating objects.

(reset! x 2)

x

@x

;; swap!
;; ------------------------------------------------------------------------------

;; if you want to change the value of an atom on the basis of its current value
;; you can use `swap!`. In its simplest form `swap!` accept as first argument
;; the atom itself and as a second argument an updating function of one argument
;; which will be instantiated with the current value of the atom. `swap!` returns
;; the new value of the atom.

(swap! x inc)

x

@x

;; if your updating function needs extra arguments to calculate the new value, you
;; have to pass them as extra arguments to `swap!` after the updating function
;; itself.

(swap! x (fn [old extra-arg]
           (+ old extra-arg)) 39)

x

@x

;; as usual when anonymous functions are simple enough it's idiomatic the usage
;; of the condensed form.

(swap! x #(- %1 %2) 42)

x

@x

;; Note that the updating function has to be free of side-effects because a
;; waiting writer could call it more than once in a spin loop.

;; set!
;; ----------------------------------------------------------------------------

;; Sometimes you need to mutate existing JavaScript objects. For this you
;; have `set!`.

(def c (.createElement js/document "canvas"))
(def ctxt (.getContext c "2d"))

;; We can use property access with `set!` to change the fill color of a
;; a canvas rendering context.

(set! (.-fillColor ctxt) "#ffffff")


;; The ClojureScript Standard Library
;; ============================================================================

;; The ClojureScript standard library largely mirrors the Clojure standard
;; library with the exception of functionality that assumes a multithreaded
;; environment, first class namespaces, and Java numerics.

;; Here are some highlights and patterns that newcomers to ClojureScript might
;; find useful. 

(apply str (interpose ", " ["Bob" "Mary" "George"]))

((juxt :first :last) {:first "Bob" :last "Smith"})

(def people [{:first "John" :last "McCarthy"}
             {:first "Alan" :last "Kay"}
             {:first "Joseph" :last "Licklider"}
             {:first "Robin" :last "Milner"}])

(map :first people)

(take 5 (repeat "red"))

(take 5 (repeat "blue"))

(take 5 (interleave (repeat "red") (repeat "blue")))

(take 10 (cycle ["red" "white" "blue"]))

(partition 2 [:a 1 :b 2 :c 3 :d 4 :e 5])

(partition 2 1 [:a 1 :b 2 :c 3 :d 4 :e 5])

(take-while #(< % 5) (range 10))

(drop-while #(< % 5) (range 10))


;; Protocols
;; ============================================================================

;; The ClojureScript language is constructed on a rich set of protocols. The
;; same uniformity provided by ClojureScript collections can be extended to
;; your own types or even types that you do not control!

;; A lot of the uniform power we saw early was because the ClojureScript
;; collections are implemented in terms of protocols. Collections can be
;; coerced in sequences because they implement ISeqable. You can use `get`
;; on vectors and maps because they implement ILookup.

(get {:foo "bar"} :foo)
(get [:cat :bird :dog] 1)

;; Map destructing actually desugars into `get` calls. That means if you extend
;; your type to ILookup it will also support map destructuring!


;; extend-type
;; ----------------------------------------------------------------------------

;; ClojureScript supports custom extension to types that avoid many of the
;; pitfalls that you encounter in other languages. For example imagine we have
;; some awesome polymorphic functionality in mind.

(defprotocol MyProtocol (awesome [this]))

;; It's idiomatic to name the first argument of a protocol's functions
;; as `this` which reminds you that it is the argument used by
;; ClojureScript to dispatch the right function implementation on the
;; basis of the type of the value of `this`

;; Now imagine we want JavaScript strings to participate. We can do this
;; simply.

(extend-type string
  MyProtocol
  (awesome [_] "Totally awesome!"))

;; As said while learning about `let` special form, when we're not
;; interested in the value of an argument it's idiomatic to use the
;; underscore as a placeholder like above.

(awesome "Is this awesome?")


;; extend-protocol
;; ----------------------------------------------------------------------------

;; Sometimes you want to extend several types to a protocol at once.

(extend-protocol MyProtocol
  js/Date
  (awesome [_] "Having an awesome time!")
  number
  (awesome [_] "I'm an awesome number!"))

(awesome #inst "2014")
(awesome 5)


;; reify
;; ----------------------------------------------------------------------------

;; Sometimes it's useful to make an anonymous type which implements some
;; various protocols.

;; For example say we want a JavaScript object to support ILookup. Now we don't
;; want to blindly `extend-type object`, that would pollute the behavior of plain
;; JavaScript objects for everyone.

;; Instead we can provide a helper function that takes an object and returns
;; something that provides this functionality.

(defn ->lookup [obj]
  (reify
    ILookup
    (-lookup [this k]
      (-lookup this k nil))
    (-lookup [this k not-found]
      (let [k (name k)]
        (if (.hasOwnProperty obj k)
          (aget obj k)
          not-found)))))

;; We can then selectively make JavaScript objects work with `get`.

(get (->lookup #js {"foo" "bar"}) :foo)

;; But this also means we get destructuring on JavaScript objects.

(def some-object #js {"foo" "bar" "baz" "woz"})

(let [{:keys [foo baz]} (->lookup some-object)]
  [foo baz])


;; specify
;; ----------------------------------------------------------------------------

;; Light Table ships with a older version of ClojureScript and does not yet
;; support specify


;; Macros
;; ============================================================================


;; Types & Records
;; ============================================================================

;; deftype
;; ----------------------------------------------------------------------------

;; Sometimes a map will simply not suffice, in these cases you will want to
;; make your own custom type.

(deftype Foo [a b])

;; It's idiomatic to use CamelCase to name a deftype. You can instantiate a
;; deftype instance using the same constructor pattern we've already discussed.

(Foo. 1 2)

;; You can access properties of a deftype instance using property access
;; syntax.

(.-a (Foo. 1 2))

;; You can implement protocol methods on a deftype. Note that the first
;; argument to any deftype or defrecord method is the instance itself.
;; The dash in `-count` has no special meaning. It's just a convention for
;; the core ClojureScript protocols. You need not adopt it.

(deftype Foo [a b]
  ICounted
  (-count [this] 2))

(count (Foo. 1 2))

;; Sometimes it's useful to implement methods directly on the deftype.

(deftype Foo [a b]
  Object
  (toString [this] (str a ", " b)))

(.toString (Foo. 1 2))

;; deftype field are immutable unless specified. The following will not compile.
;; (To prove it to yourself, highlight and evaluate the `deftype` form below.)

(comment

  (deftype Foo [a ^:mutable b]
    Object
    (setA [this val] (set! a val)))

  )

;; The following will compile.

(deftype Foo [a ^:mutable b]
  Object
  (setB [this val] (set! b val)))

;; defrecord
;; ----------------------------------------------------------------------------

;; deftype doesn't provide much out of the box. Often what you want to do is
;; have a domain object that acts more or less like a map. This is what
;; defrecord is for.

;; Like for deftype, it's idiomatic to use CamelCase to name a defrecord.

(defrecord Person [first last])

;; You can construct an instance in the usual way.

(Person. "Bob" "Smith")

;; Or you can use the provided constructors.

(->Person "Bob" "Smith")

(map->Person {:first "Bob" :last "Smith"})

;; It's considered idiomatic and even recommended to define a factory function
;; which returns the created instance of a defrecord/deftype. It's idiomatic to use
;; dash-case for factories names.

(defn person [first last]
  (->Person first last))

;; records work like maps

(seq (person "Bob" "Smith"))

(:first (person "Bob" "Smith"))

(keys (person "Bob" "Smith"))

(vals (person "Bob" "Smith"))

;; both deftype and defrecord are open to dynamic extensions (i.e. open class)

(keys (assoc (person "Bob" "Smith") :age 18))


;; Records & Protocols
;; ----------------------------------------------------------------------------

;; You can extend a defrecord to satisfy a protocol as you do with deftype.

(extend-type Person
  MyProtocol
  (awesome [this]
    (str (:last this) ", " (:first this))))

(awesome (person "Bob" "Smith"))

(satisfies? MyProtocol (person "Bob" "Smith"))

;; Or you can extend a protocol on a defrecord.

(extend-protocol MyProtocol
  Person
  (awesome [this]
    (str (:last this) ", " (:first this))))

(awesome (person "Bob" "Smith"))

(satisfies? MyProtocol (person "Bob" "Smith"))

;; If you need a more sophisticated form of polymorphism consider multimethods.

;; If you mix types/records with protocols you are modeling your problem with an
;; object oriented approach, which is sometimes useful.

;; Note ClojureScript does not offer a direct form of inheritance. Instead,
;; reuse/extension by composition is encouraged. It's best to avoid
;; deftype/defrecord and model your problem with plain maps. You can easily
;; switch to records later down the line.

(defrecord Contact [person email])

;; Even if it's not required, remember to define a factory function to create
;; instances of the new Contact record type by internally calling the factory
;; function for the Person record type.

(defn contact [first last email]
  (->Contact (person first last) email))

(contact "Bob" "Smith" "bob.smith@acme.com")

;; And extend the protocol on defrecord as well.

(extend-protocol MyProtocol
  Contact
  (awesome [this]
    (str (awesome (:person this)) ", " (:email this))))

(awesome (contact "Bob" "Smith" "bob.smith@acme.com"))

;; To change the value of a nested key you use 'assoc-in', like with maps.

(assoc-in (contact "Bob" "Smith" "bob.smith@acme.com")
          [:person :first] "Robert")

;; It you need to use the previous value of a nested field for calculating the
;; new one, you can use 'update-in', like with maps.

(update-in (contact "Bob" "Smith" "bob.smith@acme.com")
           [:person :first] #(string/replace %1 #"Bob" %2) "Robert")

;; As said, the main difference with the majority of OO languages is that your
;; instances of deftypes/defrecords are immutable.

(def bob (contact "Bob" "Smith" "bob.smith@acme.com"))

(update-in bob [:person :first] #(string/replace %1 #"Bob" %2) "Robert")

(get-in bob [:person :first])


;; JavaScript Interop
;; ============================================================================

;; Property Access
;; ----------------------------------------------------------------------------

(def a-date (js/Date.))

;; You can access properties with the `.-` property access syntax.

(.-getSeconds a-date)

;; Method Calls
;; ----------------------------------------------------------------------------

;; Methods can be invoke with the `.` syntax.

(.getSeconds a-date)

;; The above desugars into the following.

(. a-date (getSeconds))

;; For example you can write a `console.log` call like so.

(. js/console (log "Interop!"))


;; Primitive Array Operations
;; ----------------------------------------------------------------------------

;; When writing performance sensitive code sometimes dealing with mutable
;; arrays is unavoidable. ClojureScript provides a variety of functions for
;; creating and manipulating JavaScript arrays.

;; You can make an array of specific size with `make-array`

(make-array 32)

;; You can access an element of a array with `aget`.

(aget #js ["one" "two" "three"] 1)

;; You can access nested arrays with `aget`.

(aget #js [#js ["one" "two" "three"]] 0 1)

;; You can set the contents of an array with aset.

(def yucky-stuff #js [1 2 3])

(aset yucky-stuff 1 4)

yucky-stuff
