(ns ring.util.codec
  "Functions for encoding and decoding data."
  (:require [clojure.string :as str])
  (:import java.util.Map
           [java.net URLEncoder URLDecoder]
           [java.util Base64]))

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

(defn percent-encode
  "Percent-encode every character in the given string using either the specified
  encoding, or UTF-8 by default."
  ([unencoded]
   (percent-encode unencoded "UTF-8"))
  ([^String unencoded ^String encoding]
   (->> (.getBytes unencoded encoding)
        (map (partial format "%%%02X"))
        (str/join))))

(defn- parse-bytes [encoded-bytes]
  (->> (re-seq #"%[A-Za-z0-9]{2}" encoded-bytes)
       (map #(subs % 1))
       (map #(.byteValue (Integer/valueOf % 16)))
       (byte-array)))

(defn percent-decode
  "Decode every percent-encoded character in the given string using the
  specified encoding, or UTF-8 by default."
  ([encoded]
   (percent-decode encoded "UTF-8"))
  ([^String encoded ^String encoding]
   (str/replace encoded
                #"(?:%[A-Za-z0-9]{2})+"
                (fn [chars]
                  (-> ^bytes (parse-bytes chars)
                      (String. encoding)
                      (fix-string-replace-bug))))))

(defn url-encode
  "Returns the url-encoded version of the given string, using either a specified
  encoding or UTF-8 by default."
  ([unencoded]
   (url-encode unencoded "UTF-8"))
  ([unencoded encoding]
   (str/replace
    unencoded
    #"[^A-Za-z0-9_~.+-]+"
    #(double-escape (percent-encode % encoding)))))

(defn ^String url-decode
  "Returns the url-decoded version of the given string, using either a specified
  encoding or UTF-8 by default. If the encoding is invalid, nil is returned."
  ([encoded]
   (url-decode encoded "UTF-8"))
  ([encoded encoding]
   (percent-decode encoded encoding)))

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
    (URLEncoder/encode unencoded encoding))
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
   (form-encode x "UTF-8"))
  ([x encoding]
   (form-encode* x encoding)))

(defn form-decode-str
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default."
  ([encoded]
   (form-decode-str encoded "UTF-8"))
  ([^String encoded ^String encoding]
   (try
     (URLDecoder/decode encoded encoding)
     (catch Exception _ nil))))

(defn form-decode
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default. If the encoded value is a string, a string is returned.
  If the encoded value is a map of parameters, a map is returned."
  ([encoded]
   (form-decode encoded "UTF-8"))
  ([^String encoded encoding]
   (if-not (.contains encoded "=")
     (form-decode-str encoded encoding)
     (reduce
      (fn [m param]
        (let [[k v] (str/split param #"=" 2)
              k     (form-decode-str k encoding)
              v     (form-decode-str (or v "") encoding)]
          (if (and k v)
            (assoc-conj m k v)
            m)))
      {}
      (str/split encoded #"&")))))
