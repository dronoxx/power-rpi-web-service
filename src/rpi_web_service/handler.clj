(ns rpi-web-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :refer [upper-case]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

;-------------------------------------------------------
; VARS
;-------------------------------------------------------

(def available-commands [:power :freeze])
(def not-found-route (route/not-found (response {:message "Not found"})))

;-------------------------------------------------------
; HANDLER OPTIONS
;-------------------------------------------------------

(defn- map-values
  [m keys f & args]
  (reduce #(apply update-in %1 [%2] f args) m keys))

(defn upper-case-response
  [response]
  (map-values response [:command :option :status] upper-case))

(defn commander
  [command option]
  "ok")

;-------------------------------------------------------
; WEB HANDLERS
;-------------------------------------------------------
(defn check-availability [] "RPI Device")

(defn command-handler
  [command option]
  (if (some #(= (keyword command) %1) available-commands)
    (let [response-body {:command command
                         :option  option
                         :status  (commander command option)}]
      (-> response-body upper-case-response response))
    not-found-route))

(defroutes app-routes
           (GET "/" [] (check-availability))
           (PUT "/:command/:option" [command option] (command-handler command option))
           not-found-route)

(def app (-> app-routes wrap-json-response wrap-json-body))
