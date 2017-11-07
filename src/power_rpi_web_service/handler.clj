(ns power-rpi-web-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :refer [upper-case]]
            [clojure.core.match :refer [match]]
            [power.core :refer [power with-device] :rename {with-device with-power-device}]
            [power.py-relay :refer [make-relay-device]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [task.core :refer [run]])
  (:gen-class)
  (:import (java.time LocalDateTime)))

;-------------------------------------------------------
; VARS
;-------------------------------------------------------
(def ^:private not-found-route (route/not-found (response {:message "Not found"})))
(def ^:private time-power-on (or (System/getenv "TIME_POWER_ON") "20:00"))
(def ^:private time-power-off (or (System/getenv "TIME_POWER_OFF") ""))
(def ^:private power-device-pin (or (System/getenv "POWER_DEVICE_PIN") 6))

(def ^:private services [[:power
                          {:configuration {:time-power-on time-power-on
                                           :hours-to-wait time-power-off}}]])

(def ^:private devices {:power (make-relay-device power-device-pin)})

;-------------------------------------------------------
; HANDLER OPTIONS
;-------------------------------------------------------

(defn- current-time []
  (let [now (LocalDateTime/now)
        hour (.getHour now)
        minute (.getMinute now)]
    [now [hour minute]]))

(defn- map-values
  [m keys f & args]
  (reduce #(apply update-in %1 [%2] f args) m keys))


(defn- upper-case-response
  [response]
  (map-values response [:command :option :status] upper-case))


(defn- service-status
  [service]
  (try
    (if (the-ns (symbol (str (name service) ".core"))) "ok")
    (catch Exception e (.getMessage e))))


(defn- power-devices
  [option]
  (try
    (with-power-device (:power devices) (power option))
    "ok"
    (catch Exception e (str "error:" (.getMessage e)))))

(defn- power-device-task
  [time-to-power option]
  (let [time (current-time)
        hour-minute (second time)
        str-time (str (first hour-minute) ":" (second hour-minute))]
    (if (= time-to-power str-time)
      (with-power-device (:power devices) (power option)))))

(defn- power-devices-tasks
  [time-power-on time-power-off]
  (do
    (run {:pause 900000} (power-device-task time-power-on :on))
    (if (seq time-power-off)
      (run {:pause 900000} (power-device-task time-power-off :off)))))

;-------------------------------------------------------
; WEB HANDLERS
;-------------------------------------------------------
(defn- check-availability-handler [] "RPI Device")


(defn- check-services-status-handler
  [services]
  (let [service-map (map #(hash-map
                            (keyword (str (first %1) "-service"))
                            (hash-map :status (service-status (first %1))
                                      :configuration (-> %1 second :configuration)))
                         services)]
    (response service-map)))


(defn- power-handler
  [option]
  (let [response-body {:command "power"
                       :option  option
                       :status  (power-devices (keyword option))}]
    (-> response-body upper-case-response response)))


(defroutes app-routes
           (GET "/" [] (check-availability-handler))
           (GET "/status" [] (check-services-status-handler services))
           (PUT "/power/:option" [option] (power-handler option))
           not-found-route)


(def app (-> app-routes wrap-json-response wrap-json-body))

(defn -main [& args]
  (let [port (Integer. (first args))]
    (do
      (power-devices-tasks time-power-on time-power-off)
      (run-jetty app {:port port :join? false}))))