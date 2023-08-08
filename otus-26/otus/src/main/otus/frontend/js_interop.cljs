(ns otus.frontend.js-interop)

;; JS interop
;;


;; js/
;;
js/window
js/document


;; Property access
;;

;; "dot special form"
;; getting property value
(. js/document -title) ; document.title

;; doesn't work
(comment
  (. js/document -location -href))

;; have to do this instead
(. (. js/document -location) -href) ; document.location.href

;; nested dots quickly become difficult to read and write
(. (. (. js/document -location) -href) -length)

;; To help alleviate nesting there’s another interface
;; called the "double-dot special form"
(.. js/document -location -href -length)
(macroexpand '(.. js/document -location -href -length))
;; => (. (. (. js/document -location) -href) -length)


;; Function invocation
;;

(. js/document hasFocus) ; document.hasFocus ()

(. js/document -hasFocus)


;; Setters
;;

(.. js/window -location -search) ; => ""

(set! (.. js/window -location -search) "foo=bar")

(.. js/window -location -search) ; => "?foo=bar"


;; Syntax sugar
;;

(.-title js/document)

(macroexpand '(.-title js/document))

(.hasFocus js/document)
(macroexpand '(.hasFocus js/document))


;; Instantiation
;; 

(def my-obj #js {"a" 1 "b" 2})

(def my-arr #js ["a" "b" 2])

;; NB!
;; Note the compiler literal doesn’t handle nesting.
;; A #js tag is required at each “depth” of the data structure.
(def my-obj1 #js {"a" 1 "b" {"c" 2 "d" 3}})

(def my-obj2 #js {"a" 1 "b" #js {"c" 2 "d" 3}})


;; Important to remember:
;; — javascript doesn’t understand clojurescript keywords or symbols;
;; — javascript object keys can only be strings.


;; js-obj and array fns
(js-obj "foo" 1 "bar" 2)

;; fairly dangerous
(js-obj :a 1 :b 2) ; => #js {":a" 1, ":b" 2}


;; Translation
;; 

;; clj->js recursively transforms ClojureScript values to
;; JavaScript. sets/vectors/lists become Arrays, keywords
;; and symbols become strings, maps become Objects.



(clj->js {:a 1 'b 2 "c" {:d 3}})
(js->clj #js {:a 1 :b 2 :c #js {:d 3}})

;; keyword-fn (cljs.core/name by default)
(clj->js {:a 1 'b 2 "c" {:d 3}}
         :keyword-fn (fn [x] (str "+" (name x))))

;; :keywordize-keys
(js->clj #js {:a 1 :b 2 :c #js {:d 3}}
         :keywordize-keys true)

