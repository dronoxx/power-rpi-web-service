(ns power-rpi-web-service.service
  (:require [power-rpi-web-service.core :refer :all]
            [power-rpi-web-service.device :refer [power-device device-status]]))

;-------------------------------------------------------
; BEGIN VARS
;-------------------------------------------------------
(def ^:private services (:services (read-edn-file :config)))
;-------------------------------------------------------
; END VARS
;-------------------------------------------------------


(defn check-availability [] "RPI Device")

(defn check-devices-status
  "Return the devices status"
  []
  (map #(let [{service-name  :type
               configuration :configuration} %]
          (hash-map
            (keyword (str service-name "-service"))
            (hash-map :status (device-status service-name)
                      :configuration configuration)))
       services))


(defn power
  "Power a device"
  [option]
  (let [status (power-device (keyword option))
        response-body {:command "power"
                       :option  option
                       :status  status}]
    (upper-case-map response-body)))
