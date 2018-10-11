(ns fbbot.core
  (:require
    [cljs.nodejs]

    [fbbot.system.environment :as env]
    [fbbot.system.server :as http]

    [macchiato.middleware.defaults :as defaults]
    [macchiato.util.response :as res]

    [mount.core :as mount :refer [defstate]]

    [reitit.ring :as ring]

    [taoensso.timbre :refer-macros [info]]))


(cljs.nodejs/enable-util-print!)


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


(defstate environment :start (env/load-environment))


(defstate http-server
          :start (http/start-server paybot @environment)
          :stop (http/stop-server @http-server))


(defn main [& cli-args]
  (mount/start))


(defn hot-reload []
  (info "hot-reload starting...")
  (info "hot-reload completed."))
