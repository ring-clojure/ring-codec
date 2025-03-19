(ns ring.util.codec
  "Functions for encoding and decoding data."
  (:require [clojure.string :as str])
  (:import java.util.Map
           clojure.lang.MapEntry
           java.nio.charset.Charset
           [java.net URLEncoder URLDecoder]
           [java.util Base64 StringTokenizer]))

(defn assoc-conj
  "Associate a key with a value in a map. If the key already exists in the map,
  a vector of values is associated with the key."
  [map key val]
  (assoc map key
    (if-let [cur (get map key)]
      (if (vector? cur)
        (conj cur val)
        [cur val])
      val)))

(defn- double-escape [^String x]
  (.replace (.replace x "\\" "\\\\") "$" "\\$"))

(def ^:private string-replace-bug?
  (= "x" (str/replace "x" #"." (fn [x] "$0"))))

(defmacro ^:no-doc fix-string-replace-bug [x]
  (if string-replace-bug?
    `(double-escape ~x)
    x))

(def ^:private ^Charset utf-8 (Charset/forName "UTF-8"))

(defn- ^Charset to-charset [s]
  (if (nil? s) utf-8 (if (string? s) (Charset/forName s) s)))

(defn percent-encode
  "Percent-encode every character in the given string using either the specified
  encoding, or UTF-8 by default."
  ([unencoded]
   (percent-encode unencoded utf-8))
  ([^String unencoded encoding]
   (->> (.getBytes unencoded (to-charset encoding))
        (map (partial format "%%%02X"))
        (str/join))))

(defn- parse-bytes ^bytes [encoded-bytes]
  (let [encoded-len (count encoded-bytes)
        bs (byte-array (/ encoded-len 3))]
    (loop [encoded-index 1, byte-index 0]
      (if (< encoded-index encoded-len)
        (let [encoded-byte (subs encoded-bytes encoded-index (+ encoded-index 2))
              b (.byteValue (Integer/valueOf encoded-byte 16))]
          (aset bs byte-index b)
          (recur (+ encoded-index 3) (inc byte-index)))
        bs))))

(defn percent-decode
  "Decode every percent-encoded character in the given string using the
  specified encoding, or UTF-8 by default."
  ([encoded]
   (percent-decode encoded utf-8))
  ([^String encoded encoding]
   (let [encoding (to-charset encoding)]
     (str/replace encoded
                  #"(?:%[A-Fa-f0-9]{2})+"
                  (fn [chars]
                    (-> (parse-bytes chars)
                        (String. encoding)
                        (fix-string-replace-bug)))))))

(defn url-encode
  "Returns the url-encoded version of the given string, using either a specified
  encoding or UTF-8 by default."
  ([unencoded]
   (url-encode unencoded utf-8))
  ([unencoded encoding]
   (let [encoding (to-charset encoding)]
     (str/replace
      unencoded
      #"[^A-Za-z0-9_~.+-]+"
      #(double-escape (percent-encode % encoding))))))

(defn ^String url-decode
  "Returns the url-decoded version of the given string, using either a specified
  encoding or UTF-8 by default. If the encoding is invalid, nil is returned."
  ([encoded]
   (url-decode encoded utf-8))
  ([encoded encoding]
   (percent-decode encoded (to-charset encoding))))

(defn base64-encode
  "Encode an array of bytes into a base64 encoded string."
  [^bytes unencoded]
  (String. (.encode (Base64/getEncoder) unencoded)))

(defn base64-decode
  "Decode a base64 encoded string into an array of bytes."
  [^String encoded]
  (.decode (Base64/getDecoder) encoded))

(defprotocol ^:no-doc FormEncodeable
  (form-encode* [x encoding]))

(extend-protocol FormEncodeable
  String
  (form-encode* [^String unencoded ^String encoding]
    (URLEncoder/encode unencoded (to-charset encoding)))
  Map
  (form-encode* [params encoding]
    (letfn [(encode [x] (form-encode* x encoding))
            (encode-param [k v] (str (encode (name k)) "=" (encode v)))]
      (->> params
           (mapcat
            (fn [[k v]]
              (cond
                (sequential? v) (map #(encode-param k %) v)
                (set? v)        (sort (map #(encode-param k %) v))
                :else           (list (encode-param k v)))))
           (str/join "&"))))
  Object
  (form-encode* [x encoding]
    (form-encode* (str x) encoding))
  nil
  (form-encode* [x encoding] ""))

(defn form-encode
  "Encode the supplied value into www-form-urlencoded format, often used in
  URL query strings and POST request bodies, using the specified encoding.
  If the encoding is not specified, it defaults to UTF-8"
  ([x]
   (form-encode x utf-8))
  ([x encoding]
   (form-encode* x encoding)))

(defn- form-encoded-chars? [^String s]
  (or (.contains s "+") (.contains s "%")))

(defn form-decode-str
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default."
  ([encoded]
   (form-decode-str encoded utf-8))
  ([^String encoded encoding]
   (if (form-encoded-chars? encoded)
     (try
       (URLDecoder/decode encoded (to-charset encoding))
       (catch Exception _ nil))
     encoded)))

(defn- tokenized [s delim]
  (reify clojure.lang.IReduceInit
    (reduce [_ f init]
      (let [tokenizer (StringTokenizer. s delim)]
        (loop [result init]
          (if (.hasMoreTokens tokenizer)
            (recur (f result (.nextToken tokenizer)))
            result))))))

(def ^:private ^:const kv-separator (int \=))

(defn- split-key-value-pair [^String s]
  (let [i (.indexOf s kv-separator)]
    (cond
      (pos? i)  (MapEntry. (.substring s 0 i) (.substring s (inc i)))
      (zero? i) (MapEntry. "" (.substring s (inc i)))
      :else     (MapEntry. s ""))))

(defn form-decode-map
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default. Expects an encoded map of key/value pairs as defined by:
  https://url.spec.whatwg.org/#urlencoded-parsing"
  ([encoded]
   (form-decode-map encoded utf-8))
  ([^String encoded encoding]
   (reduce
    (fn [m param]
      (let [kv (split-key-value-pair param)
            k  (form-decode-str (key kv) encoding)
            v  (form-decode-str (val kv) encoding)]
        (if (and k v)
          (assoc-conj m k v)
          m)))
    {}
    (tokenized encoded "&"))))

(defn form-decode
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default. If the encoded value is a string, a string is returned.
  If the encoded value is a map of parameters, a map is returned."
  ([encoded]
   (form-decode encoded utf-8))
  ([^String encoded encoding]
   (if-not (.contains encoded "=")
     (form-decode-str encoded encoding)
     (form-decode-map encoded encoding))))
