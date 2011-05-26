
Forseti
==========

`forseti` is a library for lightweight Aspect-Oriented Programming in Clojure.

Usage
-----

Simply add this to your leiningen deps: `[forseti "0.1.0"]`

Documentation
------------

The documentation can be found here: http://eduardoejp.github.com/forseti/

Disclaimer
----------

Forseti is not a full-fledged AOP system. There is no code-weaver and the access to join-points is very limited. The usage I'm giving it is not so intensive,
so I don't have a good reason to create a source-to-source AOP compiler for the moment.

That being the case, Forseti is for the moment based only on 2 macros: `defpointcut` and `advice`.
`defpointcut` allows you to define pointcuts for the following join-points: function execution and multimethod execution.
Given a point-cut, `advice` advices the code through `:before`, `:after` and `:around` directives.

As you may have guessed by now, it all works by changing the root bindings of function variables and by changing the method implementations of multimethods.
That's the reason Forseti is only a lightweight AOP lib instead of a full AOP system. There is no direct source code manipulation.

Examples
--------

	(defn pre-fn [a b] (* a b))

	(defmulti print-type type)
	(defmethod print-type Integer [i] "It's an Integer!")
	(defmethod print-type Double [i] "It's a Double!")
	(defmethod print-type String [i] "It's a String!")
	(defmethod print-type :default [i] "It's a... um... something!")

	(pre-fn 5 4)
	(print-type 1)
	(print-type 1.0)
	(print-type "1")
	(print-type \1)

	(defpointcut print-pc
		:fns [pre-fn]
		:methods [[print-type Integer] [print-type Double]])
	(advice print-pc
		:around (do (println ":around" *symbol* "| *args* =" *args*) (apply proceed *args*))
		:before (do (println ":before" *symbol* "| *args* =" *args*) *args*)
		:after (do (print ":after" *symbol* "| *args* =" *args* "| ") (println "*result* =" *result*))
		)
	
	; The order in which the types of advice (:before, :after and :around) appear on the advice macro affects the result. In the case above, the original function
	; will be wrapped inside the :around code, the result will be wrapped inside :before and the result will be wrapped inside :after.
	; :before, :after and :around can also affect the result in other ways:
	; Since :before executes before the original fn, it has access to the arguments and it's return value will be the args to the original function.
	; :after has access to the *args* but can't change the input of the original function. However, its return value will be the return value of the new funtion.
	; Since :around can be executed both before and after the original function, only the arguments that are explicitely given to "proceed" will be taken
	; by the original function. The return value of :around will be treated like :after.
