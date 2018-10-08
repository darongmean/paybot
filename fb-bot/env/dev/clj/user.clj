(ns user
  (:require [fb-bot.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [fb-bot.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'fb-bot.core/repl-server))

(defn stop []
  (mount/stop-except #'fb-bot.core/repl-server))

(defn restart []
  (stop)
  (start))


