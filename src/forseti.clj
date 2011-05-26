;Version: 0.1.0
;Copyright: Eduardo Emilio Julián Pereyra, 2010
;Email: eduardoejp@gmail.com
;License: EPL 1.0 -> http://www.eclipse.org/legal/epl-v10.html

(ns forseti
  "Lightweight Aspect-Oriented Programming for Clojure."
  (:use (clojure template)))

(defmacro defpointcut
"Defines a PointCut. The options are:
:fns [vector of fn-symbols]
:methods [vector of methods]

If the multimethod symbol is provided, all the implementations will be adviced.
If a [multimethod dispatch-value] pair is given, only that implementation will be adviced."
  [pc-sym & options]
  (let [{:keys [fns methods]} (apply hash-map options)]
    `(def ~pc-sym (with-meta {:fns '~fns, :methods '~methods} {:type ::PointCut}))))

;Advice code application implementation for regular fns.
(defmacro advice-fn
  [fns typ adv]
  (case typ
    :before
    `(do-template [~'f]
       (let [~'*symbol* '~'f, ~'_oldfn ~'f]
         (alter-var-root (var ~'f) (fn [_#] (fn [& ~'*args*] (let [~'*args* ~adv] (apply ~'_oldfn ~'*args*))))))
       ~@fns)
    :after
    `(do-template [~'f]
       (let [~'*symbol* '~'f, ~'_oldfn ~'f]
         (alter-var-root (var ~'f) (fn [_#] (fn [& ~'*args*] (let [~'*result* (apply ~'_oldfn ~'*args*)] ~adv)))))
       ~@fns)
    :around
    `(do-template [~'f]
       (let [~'*symbol* '~'f , ~'proceed ~'f]
         (alter-var-root (var ~'f) (fn [_#] (fn [& ~'*args*] ~adv))))
       ~@fns)
    ))

;Advice code application implementation for multimethods.
(defmacro advice-meth
  [meths typ adv]
  (case typ
    :before
    `(let [[~'f ~'dispval] ~meths
           ~'*symbol* '~'f, ~'*dispatch-val* '~'dispval, ~'_oldfn (get-method ~'f ~'dispval)]
       (remove-method ~'f ~'dispval)
       (defmethod ~'f ~'dispval [& ~'*args*]
         (let [~'*args* ~adv] (apply  ~'_oldfn ~'*args*))))
    :after
    `(let [[~'f ~'dispval] ~meths
           ~'*symbol* '~'f, ~'*dispatch-val* '~'dispval, ~'_oldfn (get-method ~'f ~'dispval)]
       (remove-method ~'f ~'dispval)
       (defmethod ~'f ~'dispval [& ~'*args*]
         (let [~'*result* (apply ~'_oldfn ~'*args*)] ~adv)))
    :around
    `(let [[~'f ~'dispval] ~meths
           ~'*symbol* '~'f, ~'*dispatch-val* '~'dispval, ~'proceed (get-method ~'f ~'dispval)]
       (remove-method ~'f ~'dispval)
       (defmethod ~'f ~'dispval [& ~'*args*] ~adv))
    ))

(defmacro advice-multi [multis typ adv]
  `(advice-meth ~(vec (for [k (-> multis eval methods keys)] [multis k])) ~typ ~adv))

(defmacro advice-poly [mmeths typ adv]
  `(do ~@(for [mm mmeths]
           (if (vector? mm)
             `(advice-meth ~mm ~typ ~adv)
             `(advice-multi ~mm ~typ ~adv)
             ))))

(defmacro advice
"Advices the given code to the given PointCut.
The options for the type of advice are :before, :around and :after.

Inside the code of each advice, there are certain variables that hold some information about the join point.
:before, :after and :around have access to the *symbol* and *args* of the code they are advicing.
:after has access to the *result*.
:around has a \"proceed\" function that holds the original one being adviced.
In the case of multimethods, there is also a *dispatch-val* variable with the dispatch value of the method implementation."
  ([pc typ adv]
   `(do
      ~(when (:fns (eval pc)) `(advice-fn ~(:fns (eval pc)) ~typ ~adv))
      ~(when (:methods (eval pc)) `(advice-poly ~(:methods (eval pc)) ~typ ~adv))
      ))
  ([pc typ adv & advices]
   `(do
      (advice ~pc ~typ ~adv)
      (advice ~pc ~@(identity advices))))
  )
