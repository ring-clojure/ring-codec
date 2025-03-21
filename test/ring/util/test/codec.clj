(ns ring.util.test.codec
  (:use clojure.test
        ring.util.codec)
  (:import java.util.Arrays
           java.nio.charset.Charset))

(deftest test-percent-encode
  (is (= (percent-encode " ") "%20"))
  (is (= (percent-encode "+") "%2B"))
  (is (= (percent-encode "foo") "%66%6F%6F")))

(deftest test-percent-decode
  (is (= (percent-decode "%s4/") "%s4/"))
  (is (= (percent-decode "%20") " "))
  (is (= (percent-decode "foo%20bar") "foo bar"))
  (is (= (percent-decode "foo%FE%FF%00%2Fbar" "UTF-16") "foo/bar"))
  (is (= (percent-decode "%24") "$")))

(deftest test-url-encode
  (is (= (url-encode "foo/bar") "foo%2Fbar"))
  (is (= (url-encode "foo/bar" nil) "foo%2Fbar"))
  (is (= (url-encode "foo/bar" "UTF-8") "foo%2Fbar"))
  (is (= (url-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar"))
  (is (= (url-encode "foo/bar" (Charset/forName "UTF-16")) "foo%FE%FF%00%2Fbar"))
  (is (= (url-encode "foo+bar") "foo+bar"))
  (is (= (url-encode "foo bar") "foo%20bar")))

(deftest test-url-decode
  (is (= (url-decode "foo%2Fbar") "foo/bar" ))
  (is (= (url-decode "foo%FE%FF%00%2Fbar" "UTF-16") "foo/bar"))
  (is (= (url-decode "%") "%")))

(deftest test-base64-encoding
  (let [str-bytes (.getBytes "foo?/+" "UTF-8")]
    (is (Arrays/equals str-bytes (base64-decode (base64-encode str-bytes))))))

(deftest test-form-encode
  (testing "strings"
    (are [x y] (= (form-encode x) y)
      "foo bar" "foo+bar"
      "foo+bar" "foo%2Bbar"
      "foo/bar" "foo%2Fbar")
    (is (= (form-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar")))
  (testing "maps"
    (are [x y] (= (form-encode x) y)
      {"a" "b"}         "a=b"
      {:a "b"}          "a=b"
      {"a" 1}           "a=1"
      {"a" nil}         "a="
      {"a" "b" "c" "d"} "a=b&c=d"
      {"a" "b c"}       "a=b+c"
      {"a" ["b" "c"]}   "a=b&a=c"
      {"a" ["c" "b"]}   "a=c&a=b"
      {"a" (seq [1 2])} "a=1&a=2"
      {"a" #{"c" "b"}}  "a=b&a=c")
    (is (= (form-encode {"a" "foo/bar"} "UTF-16") "a=foo%FE%FF%00%2Fbar"))))

(deftest test-form-decode-str
  (is (= (form-decode-str "foo=bar+baz") "foo=bar baz"))
  (is (nil? (form-decode-str "%D")))
  (is (= (form-decode-str "foo=bar+baz" nil) "foo=bar baz"))
  (is (= (form-decode-str "foo=bar+baz" "UTF-8") "foo=bar baz")))

(deftest test-form-decode-map
  (are [x y] (= (form-decode-map x) y)
    "foo"     {"foo" ""}
    "a=b"     {"a" "b"}
    "a=b&c=d" {"a" "b" "c" "d"}
    "foo+bar" {"foo bar" ""}
    "a=b+c"   {"a" "b c"}
    "a=b%2Fc" {"a" "b/c"}
    "a=b&c"   {"a" "b" "c" ""}
    "a=&b=c"  {"a" "" "b" "c"}
    "a&b=c"   {"a" "" "b" "c"}
    "="       {"" ""}
    "a="      {"a" ""}
    "=b"      {"" "b"})
  (testing "invalid URL encoding"
    (are [x y] (= (form-decode-map x) y)
      "%=b" {}
      "a=%" {}
      "%=%" {}))
  (is (= (form-decode-map "a=foo%FE%FF%00%2Fbar" "UTF-16")
         {"a" "foo/bar"}))
  (is (= (form-decode-map "a=foo%2Fbar" nil)
         {"a" "foo/bar"})))

(deftest test-form-decode
  (are [x y] (= (form-decode x) y)
    "foo"     "foo"
    "a=b"     {"a" "b"}
    "a=b&c=d" {"a" "b" "c" "d"}
    "foo+bar" "foo bar"
    "a=b+c"   {"a" "b c"}
    "a=b%2Fc" {"a" "b/c"}
    "a=b&c"   {"a" "b" "c" ""}
    "a=&b=c"  {"a" "" "b" "c"}
    "a&b=c"   {"a" "" "b" "c"}
    "="       {"" ""}
    "a="      {"a" ""}
    "=b"      {"" "b"})
  (testing "invalid URL encoding"
    (are [x y] (= (form-decode x) y)
      "%=b" {}
      "a=%" {}
      "%=%" {}))
  (is (= (form-decode "a=foo%FE%FF%00%2Fbar" "UTF-16")
         {"a" "foo/bar"}))
  (is (= (form-decode "a=foo%2Fbar" nil)
         {"a" "foo/bar"})))
