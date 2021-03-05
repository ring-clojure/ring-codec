(defproject ring/ring-codec "1.1.3"
  :description "Library for encoding and decoding data"
  :url "https://github.com/ring-clojure/ring-codec"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[lein-codox "0.10.7"]]
  :codox
  {:output-path "codox"
   :source-uri "http://github.com/ring-clojure/ring-codec/blob/{version}/{filepath}#L{line}"}
  :aliases {"test-all" ["with-profile" "default:+1.6:+1.7:+1.8:+1.9:+1.10" "test"]}
  :profiles
  {:dev  {:dependencies [[criterium "0.4.6"]]}
   :1.6  {:dependencies [[org.clojure/clojure "1.6.0"]]}
   :1.7  {:dependencies [[org.clojure/clojure "1.7.0"]]}
   :1.8  {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9  {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.2"]]}})
