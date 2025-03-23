(defproject ring/ring-codec "1.2.0"
  :description "Library for encoding and decoding data"
  :url "https://github.com/ring-clojure/ring-codec"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :plugins [[lein-codox "0.10.7"]]
  :codox
  {:output-path "codox"
   :source-uri "http://github.com/ring-clojure/ring-codec/blob/{version}/{filepath}#L{line}"}
  :aliases {"test-all" ["with-profile" "default:+1.10" "test"]}
  :profiles
  {:dev  {:dependencies [[criterium "0.4.6"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}})
