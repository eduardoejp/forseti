(defproject forseti "0.1.0"
  :description "Lightweight Aspect-Oriented Programming for Clojure."
  :url "https://github.com/eduardoejp/forseti"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :dev-dependencies [[org.clojars.rayne/autodoc "0.8.0-SNAPSHOT"]]
  :autodoc {:name "forseti"
            :description "Lightweight Aspect-Oriented Programming for Clojure."
            :copyright "Copyright 2011 Eduardo Julian"
            :web-src-dir "http://github.com/eduardoejp/forseti/blob/"
            :web-home "http://eduardoejp.github.com/forseti/"
            :output-path "autodoc"}
	)
