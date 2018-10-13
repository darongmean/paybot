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


(defn messenger-response [request respond raise]
  (-> {:message "Hello FB Messenger!"}
      (ring-response/ok)
      (respond)))


(def paybot
  (ring/ring-handler
    (ring/router
      [["/" {:middleware [[middleware/wrap-defaults middleware/site-defaults]]
             :get        hello-world}]
       ["/messenger" {:middleware [[middleware/wrap-defaults middleware/api-defaults]
                                   [rest-middleware/wrap-restful-format]]
                      :get        messenger-response}]])
    (ring/create-default-handler)))

(comment (do
           (paybot
             {:request-method :get
              :headers        {"Accept" "application/json"}
              :body           {}
              :uri            "/messenger"}
             cljs.pprint/pprint
             identity)))
