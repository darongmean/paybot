(ns fbbot.paybot
  (:require
    [macchiato.middleware.defaults :as defaults]
    [macchiato.util.response :as res]

    [reitit.ring :as ring]))


(defn hello-world [request response raise]
  (-> (str "Hello world!")
      (res/ok)
      (res/content-type "text/plain")
      (response)))


(def paybot
  (ring/ring-handler
    (ring/router ["/" {:get hello-world}])
    (ring/create-default-handler)
    {:middleware [[defaults/wrap-defaults defaults/site-defaults]]}))
