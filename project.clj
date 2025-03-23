(defproject ring/ring-codec "1.3.0"
  :description "Library for encoding and decoding data"
  :url "https://github.com/ring-clojure/ring-codec"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :plugins [[lein-codox "0.10.8"]]
  :codox
  {:output-path "codox"
   :source-uri "http://github.com/ring-clojure/ring-codec/blob/{version}/{filepath}#L{line}"}
  :aliases {"test-all" ["with-profile" "default:+1.10:+1.11:+1.12" "test"]}
  :profiles
  {:dev  {:dependencies [[criterium "0.4.6"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}
   :1.11 {:dependencies [[org.clojure/clojure "1.11.4"]]}
   :1.12 {:dependencies [[org.clojure/clojure "1.12.0"]]}})
