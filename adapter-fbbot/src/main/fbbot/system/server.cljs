(ns fbbot.system.server
  (:require
    [macchiato.server :as http]

    [taoensso.timbre :refer-macros [info]]))


(defn start-server [handler environment]
  (let [{:keys [host port]} environment
        server (http/start
                 {:handler    handler
                  :host       host
                  :port       port
                  :on-success #(info "server started on" host ":" port)})
        *socket-coll (atom [])
        _ (.on server "connection" #(swap! *socket-coll conj %1))]
    [server *socket-coll]))


(defn stop-server [[server *socket-coll]]
  (doseq [socket (or @*socket-coll [])]
    (.destroy socket))
  (.close server #(info "server stopped.")))
