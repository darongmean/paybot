(ns fbbot.paybot
  (:require
    [macchiato.middleware.defaults :as defaults]
    [macchiato.util.response :as res]

    [reitit.ring :as ring]

    [taoensso.timbre :refer-macros [info]]))


(defn hello-world [request response raise]
  (-> (str "Hello world!")
      (res/ok)
      (res/content-type "text/plain")
      (response)))


(def paybot
  (ring/ring-handler
    (ring/router
      [["/" {:middleware [[defaults/wrap-defaults defaults/site-defaults]]
             :get        hello-world}]])
    (ring/create-default-handler)))

(comment
  (do
    (paybot {:request-method :get, :uri "/"} cljs.pprint/pprint identity)))
