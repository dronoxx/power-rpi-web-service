(ns power-rpi-web-service.api
  (:require [power-rpi-web-service.service :refer :all]
            [compojure.core :refer [defroutes GET PUT]]
            [compojure.route :refer [not-found]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

;-------------------------------------------------------
; BEGIN VARS
;-------------------------------------------------------
(def ^:private not-found-route (not-found (response {:message "Not found"})))
;-------------------------------------------------------
; END VARS
;-------------------------------------------------------


(defroutes app-routes

           (GET "/"
                []
             (response (check-availability)))

           (GET "/status"
                []
             (response (check-devices-status)))

           (PUT "/power/:option"
                [option]
             (response (power option)))

           not-found-route)


(def app (-> app-routes wrap-json-response wrap-json-body))