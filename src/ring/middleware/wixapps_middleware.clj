(ns ring.middleware.wixapps-middleware
  (:import [org.apache.commons.codec.binary Base64 Hex]
           [java.util Arrays])
  (:require [clojure.string]
            [clj-json.core :as json]))

(defn hmac
  ([algorithm msg key]
    (hmac algorithm msg key "UTF8"))
  ([algorithm msg key encoding]
    (let [key (javax.crypto.spec.SecretKeySpec. (.getBytes key "UTF8") algorithm)
          mac (doto (javax.crypto.Mac/getInstance algorithm)
                (.init key))]
      (.doFinal mac (.getBytes msg encoding)))))

(defn wrap-wixapps-middleware
  "Function used to add the wixapps middleware to the Ring stack. By default this will
  check GET requests a instance parameter, check the signature against the key, and, if it is valid
  pass a decoded and parsed SignedInstance hash-map to the controller, replacing the instance parameter.
  Otherwise, it will return 403 without calling the controller.
    - algorithm should be an algorithm string, for example HmacSHA256
    - the secret-key is the key you recieved when registering the app"
  [handler {:keys [algorithm secret-key forbidden-handler pred]
            :or {forbidden-handler (fn [req]
                                     {:status 403 :body "403 Forbidden - Incorrect HMAC"})
                 pred (fn [req] (= :get (:request-method req)))}}]
  {:pre [(every? identity [algorithm secret-key])]}
  (fn [req]
      (let [[given-hmac signed-instance] (clojure.string/split  (get (:params req) "instance") #"\.")
             json-instance (json/parse-string (String. (Base64/decodeBase64 (.getBytes signed-instance))))
             our-hmac (hmac algorithm signed-instance secret-key)]
        (if (Arrays/equals (Base64/decodeBase64  (.getBytes given-hmac)) our-hmac)
          (handler (assoc req :params (assoc (:params req) "instance" json-instance)))
          (forbidden-handler req)))))
