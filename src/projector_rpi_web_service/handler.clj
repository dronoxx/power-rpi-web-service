(ns projector-rpi-web-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :refer [upper-case]]
            [clojure.core.match :refer [match]]
            [projector.core :refer [available-commands projector with-device]]
            [projector.rs232 :refer [create-a-connection]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

;-------------------------------------------------------
; VARS
;-------------------------------------------------------
(def not-found-route (route/not-found (response {:message "Not found"})))

(defn projector-port [] (or (System/getenv "PROJECTOR_PORT") "/DEV/TTYAMA0"))
(defn power-minutes [] (or (System/getenv "POWER_OFF_MINUTES_TO_WAIT") 5))

(def services [[:projector
                {:configuration {:port (projector-port)}}]
               [:power
                {:configuration {:minutes-to-wait (power-minutes)}}]])


;-------------------------------------------------------
; HANDLER OPTIONS
;-------------------------------------------------------

(defn- map-values
  [m keys f & args]
  (reduce #(apply update-in %1 [%2] f args) m keys))


(defn upper-case-response
  [response]
  (map-values response [:command :option :status] upper-case))


(defn service-status
  [service]
  (try
    (if (the-ns (symbol (str (name service) ".core"))) "ok")
    (catch Exception e (.getMessage e))))

;TODO: Implement power function
(defn power-projector
  ([option]
   (power-projector option 0 :second))
  ([option time interval]
   true))

(defn commander
  [command option]
  (try
    (when-let [device (create-a-connection (projector-port))]
      (let [projector-fn #(with-device device (projector command option))]
        (match [command option]
               [:power :on] (do
                              (power-projector :on)
                              (projector-fn))
               [:power :off] (do
                               (projector-fn)
                               (power-projector :off power-minutes :minute))
               :else (projector-fn)))
      "ok")
    (catch Exception e (str "error:" (.getMessage e)))))

;-------------------------------------------------------
; WEB HANDLERS
;-------------------------------------------------------
(defn check-availability-handler [] "RPI Device")


(defn check-services-status-handler
  [services]
  (let [service-map (map #(hash-map
                            (keyword (str (first %1) "-service"))
                            (hash-map :status (service-status (first %1))
                                      :configuration (-> %1 second :configuration)))
                         services)]
    (response service-map)))


(defn command-handler
  [command option]
  (if (some #(= (keyword command) %1) (available-commands))
    (let [response-body {:command command
                         :option  option
                         :status  (commander (keyword command) (keyword option))}]
      (-> response-body upper-case-response response))
    not-found-route))


(defroutes app-routes
           (GET "/" [] (check-availability-handler))
           (GET "/status" [] (check-services-status-handler services))
           (PUT "/:command/:option" [command option] (command-handler command option))
           not-found-route)


(def app (-> app-routes wrap-json-response wrap-json-body))


(defn -main [& args]
  (let [keyworded-args (map keyword args)
        action (first keyworded-args)
        web-server (run-jetty app {:port 8080 :join? false})]
    (cond
      (= action :start) (.start web-server)
      (= action :stop) (.stop web-server))))
