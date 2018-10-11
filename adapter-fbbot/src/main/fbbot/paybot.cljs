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
      ["/" {:get hello-world}]
      {:middleware [[defaults/wrap-defaults defaults/site-defaults]]})
    (ring/create-default-handler)))
