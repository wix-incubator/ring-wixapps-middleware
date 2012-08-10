(ns ring.middleware.wixapps-middleware-test
  (:import [org.apache.commons.codec.binary Base64 Hex]
           [java.util Arrays])
  (:use clojure.test
        ring.util.test
        ring.middleware.wixapps-middleware))

(def wixapps-middleware-handler (wrap-wixapps-middleware identity {:algorithm "HmacSHA256" :header-field "AUTH-HMAC"
                                                   :secret-key "d245bbf8-57eb-49d6-aeff-beff6d82cd39"}))

(def valid-request {:uri "/"
              :request-method :get
              :server-port 80
              :server-name "dave.inadub.co.uk"
              :remote-addr "localhost"
              :scheme :http
              :query-params {"instance" "naQKltLRVJwLVN90qQYpmmyzkVqFIH0hpvETYuivA1U.eyJpbnN0YW5jZUlkIjoiOWY5YzVjMTYtNTljOC00NzA4LThjMjUtODU1NTA1ZGFhOTU0Iiwic2lnbkRhdGUiOiIyMDEyLTA4LTA4VDE5OjQ3OjMxLjYyNFoiLCJ1aWQiOm51bGwsInBlcm1pc3Npb25zIjpudWxsfQ"}
              })

(def invalid-request {:uri "/"
              :request-method :get
              :server-port 80
              :server-name "dave.inadub.co.uk"
              :remote-addr "localhost"
              :scheme :http
              :query-params {"instance" "foobar.eyJpbnN0YW5jZUlkIjoiOWY5YzVjMTYtNTljOC00NzA4LThjMjUtODU1NTA1ZGFhOTU0Iiwic2lnbkRhdGUiOiIyMDEyLTA4LTA4VDE5OjQ3OjMxLjYyNFoiLCJ1aWQiOm51bGwsInBlcm1pc3Npb25zIjpudWxsfQ"}
              })

(def valid-response {:uri "/"
              :request-method :get
              :server-port 80
              :server-name "dave.inadub.co.uk"
              :remote-addr "localhost"
              :scheme :http
              :query-params {"instance" "naQKltLRVJwLVN90qQYpmmyzkVqFIH0hpvETYuivA1U.eyJpbnN0YW5jZUlkIjoiOWY5YzVjMTYtNTljOC00NzA4LThjMjUtODU1NTA1ZGFhOTU0Iiwic2lnbkRhdGUiOiIyMDEyLTA4LTA4VDE5OjQ3OjMxLjYyNFoiLCJ1aWQiOm51bGwsInBlcm1pc3Npb25zIjpudWxsfQ"
                             "parsed-instance" {"instanceId" "9f9c5c16-59c8-4708-8c25-855505daa954", "signDate" "2012-08-08T19:47:31.624Z", "uid" nil, "permissions" nil}}
              })

(deftest hmac-sha512
  (is (= (String. (Base64/encodeBase64 (hmac "HmacSHA256" "eyJpbnN0YW5jZUlkIjoiOWY5YzVjMTYtNTljOC00NzA4LThjMjUtODU1NTA1ZGFhOTU0Iiwic2lnbkRhdGUiOiIyMDEyLTA4LTA4VDE5OjQ3OjMxLjYyNFoiLCJ1aWQiOm51bGwsInBlcm1pc3Npb25zIjpudWxsfQ" "d245bbf8-57eb-49d6-aeff-beff6d82cd39") false true)  )
                     "naQKltLRVJwLVN90qQYpmmyzkVqFIH0hpvETYuivA1U")))

(deftest base64-decode
  (is (=
        (String. (Base64/decodeBase64 (.getBytes "dGVzdA")))
        (String. (.getBytes "test" )))))

(deftest valis-request-test
  (is (= (wixapps-middleware-handler valid-request) valid-response )))

(deftest invalid-request-test
  (let [response (wixapps-middleware-handler invalid-request)]
    (is (= (:status response) 403))
    (is (= (:body response) "403 Forbidden - Incorrect HMAC"))))