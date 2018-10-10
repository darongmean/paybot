(ns template.core
  (:require
    [cljs.nodejs]

    [macchiato.env :as config]
    [macchiato.middleware.defaults :as defaults]
    [macchiato.server :as http]
    [macchiato.util.response :as res]

    [mount.core :as mount :refer [defstate]]

    [taoensso.timbre :refer-macros [info]]))


(cljs.nodejs/enable-util-print!)


(defonce counter (atom 1))


(defn routes [request response raise]
  (-> (str "Hello " @counter " !!!")
      (res/ok)
      (res/content-type "text/plain")
      (response)))


(defstate environment
          :start (-> (config/env)
                     (update-in [:host] (fnil identity "localhost"))
                     (update-in [:port] (fnil identity 3000))))


(defstate http-server
          :start (let [{:keys [host port]} @environment
                       server (http/start
                                {:handler    (defaults/wrap-defaults routes defaults/site-defaults)
                                 :host       host
                                 :port       port
                                 :on-success #(info "server started on" host ":" port)})
                       *socket-coll (atom [])
                       _ (.on server "connection" #(swap! *socket-coll conj %1))]
                   [server *socket-coll])
          :stop (let [[server *socket-coll] @http-server]
                  (doseq [socket (or @*socket-coll [])]
                    (.destroy socket))
                  (.close server #(info "server stopped."))))


(defn main [& cli-args]
  (mount/start))


(defn hot-reload []
  (info "hot-reload starting...")
  (info "hot-reload completed."))
