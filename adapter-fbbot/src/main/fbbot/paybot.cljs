(ns fbbot.paybot
  (:require
    [macchiato.middleware.defaults :as middleware]
    [macchiato.middleware.restful-format :as rest-middleware]
    [macchiato.util.response :as ring-response]

    [reitit.ring :as ring]

    [taoensso.timbre :refer-macros [info]]))


(defn hello-world [request respond raise]
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

(comment
  (paybot
    {:request-method :get
     :headers        {"Accept" "application/json"}
     :body           {}
     :params         {"hub.mode"         "subscribe",
                      "hub.challenge"    "800315870",
                      "hub.verify_token" "MAGIC_TOKEN_1234"}
     :uri            "/messenger"}
    cljs.pprint/pprint
    identity))
