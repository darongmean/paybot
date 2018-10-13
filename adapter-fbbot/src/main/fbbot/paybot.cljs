(ns fbbot.paybot
  (:require
    [com.gfredericks.test.chuck.clojure-test :refer-macros [checking]]

    [cljs.spec.alpha :as s]
    [cljs.test :refer (deftest is)]

    [clojure.test.check.generators :as gen]

    [macchiato.middleware.defaults :as middleware]
    [macchiato.middleware.restful-format :as rest-middleware]
    [macchiato.util.response :as ring-response]

    [reitit.ring :as ring]

    [taoensso.timbre :refer-macros [debug error]]))


(defn hello-world [_ respond _]
  (-> (str "Hello world!")
      (ring-response/ok)
      (ring-response/content-type "text/plain")
      (respond)))


(defn messenger-request [{:keys [params]}]
  (-> {}
      (assoc :messenger/mode (params "hub.mode"))
      (assoc :messenger/challenge (params "hub.challenge"))
      (assoc :messenger/verify-token (params "hub.verify_token"))))


(defn subscribe-response [request respond _]
  (let [{:messenger/keys [challenge]} (messenger-request request)]
    ; TODO: need to check mode and verify-token
    (-> challenge
        (ring-response/ok)
        (respond))))


(def paybot
  (ring/ring-handler
    (ring/router
      [["/" {:middleware [[middleware/wrap-defaults middleware/site-defaults]]
             :get        hello-world}]
       ["/messenger" {:middleware [[middleware/wrap-defaults middleware/api-defaults]
                                   [rest-middleware/wrap-restful-format {:keywordize? true}]]
                      :get        subscribe-response}]])
    (ring/create-default-handler)))

(deftest paybot-test
  (checking "that the same 'challenge' token returned when verify webhook" 100
    [challenge gen/string-alphanumeric
     verify-token gen/string-alphanumeric]
    (paybot
      {:request-method :get
       :headers        {"Accept" "application/json"}
       :body           {}
       :params         {"hub.mode"         "subscribe",
                        "hub.challenge"    challenge,
                        "hub.verify_token" verify-token}
       :uri            "/messenger"}
      (fn [response]
        (is (= 200 (:status response)))
        (is (= "application/json" (get-in response [:headers "Content-Type"])))
        (is (= (str "\"" challenge "\"") (:body response))))
      identity)))

(comment
  (defn success-debug [obj]
    (debug "result >> success")
    (cljs.pprint/pprint obj)
    (debug "result << success"))
  (defn fail-debug [obj]
    (error "result >> fail")
    (cljs.pprint/pprint obj)
    (error "result << fail"))
  (paybot
    {:request-method :get
     :headers        {"Accept" "application/json"}
     :body           {}
     :params         {"hub.mode"         "subscribe",
                      "hub.challenge"    "800315870",
                      "hub.verify_token" "MAGIC_TOKEN_1234"}
     :uri            "/messenger"}
    success-debug
    fail-debug)
  (s/def ::name string?)
  (s/explain ::name 1))
