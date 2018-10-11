(ns fbbot.core
  (:require
    [cljs.nodejs]

    [fbbot.paybot :as paybot]
    [fbbot.system.environment :as env]
    [fbbot.system.server :as http]

    [mount.core :as mount :refer [defstate]]

    [taoensso.timbre :refer-macros [info]]))


(cljs.nodejs/enable-util-print!)


(defstate environment :start (env/load-environment))


(defstate http-server
          :start (http/start-server paybot/paybot @environment)
          :stop (http/stop-server @http-server))


(defn main [& cli-args]
  (mount/start))


(defn hot-reload []
  (info "hot-reload starting...")
  (info "hot-reload completed."))
