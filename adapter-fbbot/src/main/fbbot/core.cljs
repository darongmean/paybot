(ns fbbot.core
  (:require
    [cljs.nodejs]

    [fbbot.system.environment :as env]
    [fbbot.system.server :as http]

    [macchiato.middleware.defaults :as defaults]
    [macchiato.util.response :as res]

    [mount.core :as mount :refer [defstate]]

    [taoensso.timbre :refer-macros [info]]))


(cljs.nodejs/enable-util-print!)


(defonce counter (atom 1))


(defn routes [request response raise]
  (-> (str "Hello " @counter " !")
      (res/ok)
      (res/content-type "text/plain")
      (response)
      (defaults/wrap-defaults defaults/site-defaults)))


(defstate environment :start (env/load-environment))


(defstate http-server
          :start (http/start-server routes @environment)
          :stop (http/stop-server @http-server))


(defn main [& cli-args]
  (mount/start))


(defn hot-reload []
  (info "hot-reload starting...")
  (info "hot-reload completed."))
