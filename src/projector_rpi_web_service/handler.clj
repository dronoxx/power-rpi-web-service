(ns projector-rpi-web-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :refer [upper-case]]
            [clojure.core.match :refer [match]]
            [projector.core :refer [available-commands projector with-device] :rename {with-device with-projector-device}]
            [projector.rs232 :refer [create-a-connection]]
            [power.core :refer [power with-device] :rename {with-device with-power-device}]
            [power.py-relay :refer [make-relay-device]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

;-------------------------------------------------------
; VARS
;-------------------------------------------------------
(def ^:private not-found-route (route/not-found (response {:message "Not found"})))
(def ^:private projector-port (or (System/getenv "PROJECTOR_PORT") "/dev/ttyAMA0"))
(def ^:private power-minutes (or (System/getenv "POWER_MINUTES_TO_WAIT") 2))

(def ^:private services [[:projector
                          {:configuration {:port projector-port}}]
                         [:power
                          {:configuration {:minutes-to-wait power-minutes}}]])

(def ^:private devices {:power (make-relay-device 6)})

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


(defn commander
  [command option]
  (try
    (when-let [projector-device (create-a-connection projector-port)]
      (let [projector-fn #(with-projector-device projector-device (projector command option))
            power-fn #(with-power-device (:power devices) (power option %1 %2))]
        (match [command option]
               [:power :on] (do
                              (power-fn 1 :second)
                              (Thread/sleep 1500)
                              (projector-fn))
               [:power :off] (do
                               (projector-fn)
                               (power-fn power-minutes :minute))
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
  (let [port (Integer. (first args))]
    (do
      (run-jetty app {:port port :join? false}))))