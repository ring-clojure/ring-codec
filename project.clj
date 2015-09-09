(defproject ring/ring-codec "1.0.0"
  :description "Library for encoding and decoding data"
  :url "https://github.com/ring-clojure/ring-codec"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [commons-codec "1.6"]]
  :plugins [[codox "0.8.0"]]
  :codox {:src-dir-uri "http://github.com/ring-clojure/ring-codec/blob/1.0.0/"
          :src-linenum-anchor-prefix "L"}
  :profiles
  {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}})
