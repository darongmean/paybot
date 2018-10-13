(ns fbbot.paybot
  (:require
    [com.gfredericks.test.chuck.clojure-test :refer-macros [checking]]

    [cljs.spec.alpha :as s]
    [cljs.spec.gen.alpha :as gen]
    [cljs.test :refer (deftest is)]

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

(s/def :http.success/status #{200})
(s/def :http/body string?)
(s/def :messenger.response/webhook-challenge
  (s/keys :req-un [:http.success/status :http/body]))

(s/def :messenger.request/verify-webhook
  (s/with-gen
    ; HACK: clojure.spec doesn't support non-keyword map entry yet :(
    (s/cat :hub.mode (s/tuple #{"hub.mode"} #{"subscribe"})
           :hub.challenge (s/tuple #{"hub.challenge"} string?)
           :hub.verify-token (s/tuple #{"hub.verify_token"} string?))
    #(gen/hash-map
       "hub.mode" (s/gen #{"subscribe"})
       "hub.challenge" (s/gen string?)
       "hub.verify_token" (s/gen string?))))

(defn hub-challenge [request]
  {:pre  [(->> request :params (s/assert :messenger.request/verify-webhook))]
   :post [(s/assert :messenger.response/webhook-challenge %)]}
  (let [{:messenger/keys [challenge]} (messenger-request request)]
    ; TODO: need to check mode and verify-token
    (-> challenge
        (ring-response/ok))))


(defn respond-with-fn [f]
  (fn
    ([request] (f request))
    ([request respond _]
     (respond (f request)))))


(def paybot
  (ring/ring-handler
    (ring/router
      [["/" {:middleware [[middleware/wrap-defaults middleware/site-defaults]]
             :get        hello-world}]
       ["/messenger" {:middleware [[middleware/wrap-defaults middleware/api-defaults]
                                   [rest-middleware/wrap-restful-format {:keywordize? true}]]
                      :get        (respond-with-fn hub-challenge)}]])
    (ring/create-default-handler)))

(deftest paybot-test
  (checking "that the same 'challenge' token returned when verify webhook" 100
    [verify-hook (s/gen :messenger.request/verify-webhook)]
    (paybot
      {:request-method :get
       :params         verify-hook
       :uri            "/messenger"}
      #(is (= (verify-hook "hub.challenge") (:body %)))
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
    fail-debug))
