(defproject ring/ring-codec "1.0.0"
  :description "Library for encoding and decoding data"
  :url "https://github.com/ring-clojure/ring-codec"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [commons-codec "1.6"]]
  :plugins [[lein-codox "0.9.5"]]
  :codox
  {:output-path "codox"
   :source-uri "http://github.com/ring-clojure/ring-codec/blob/{version}/{filepath}#L{line}"}
  :aliases {"test-all" ["with-profile" "default:+1.4:+1.5:+1.6:+1.7:+1.8" "test"]}
  :profiles
  {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
   :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
   :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}})
