(ns fb-bot.handler
  (:require [fb-bot.middleware :as middleware]
            [fb-bot.routes.services :refer [service-routes]]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring :as ring]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [fb-bot.env :refer [defaults]]
            [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))


(mount/defstate app
  :start
  (middleware/wrap-base
    (ring/ring-handler
      (ring/router
        [["/" {:get
               {:handler (constantly {:status 301 :headers {"Location" "/swagger-ui"}})}}]
         (service-routes)])
      (ring/routes
        (swagger-ui/create-swagger-ui-handler
          {:path   "/swagger-ui"
           :url    "/api/swagger.json"
           :config {:validator-url nil}})
        (ring/create-resource-handler
          {:path "/"})
        (wrap-content-type (wrap-webjars (constantly nil)))
        (ring/create-default-handler)))))
