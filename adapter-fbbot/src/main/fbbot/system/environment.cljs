(ns fbbot.system.environment
  (:require
    [macchiato.env :as config]))


(defn load-environment []
  (-> (config/env)
      (update-in [:host] (fnil identity "localhost"))
      (update-in [:port] (fnil identity 3000))))
