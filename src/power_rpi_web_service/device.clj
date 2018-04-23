(ns power-rpi-web-service.device
  (:require [power-rpi-web-service.core :refer :all]
            [power.core :refer [power with-device]]
            [power.py-relay :refer [make-relay-device]]))


;-------------------------------------------------------
; BEGIN VARS
;-------------------------------------------------------
(def ^:private devices (:devices (read-edn-file :config)))
;-------------------------------------------------------
; END VARS
;-------------------------------------------------------

(defn device-status
  "Return the device status"
  ^{:author "Santiago de Pedro"
    :added  "1.0"}
  [device]
  (try
    (if (the-ns (symbol (str (name device) ".core"))) "ok")
    (catch Exception e (.getMessage e))))

(defn- relay-device
  "Make a relay device"
  []
  (let [device (first (filter #(= :power (:type %)) devices))]
    (make-relay-device (:device-pin device))))

(defn power-device
  "Power the device with a specific option"
  ^{:author "Santiago de Pedro"
    :added  "1.0"}
  [option]
  (try
    (with-device (relay-device)
                 (power option))
    "ok"
    (catch Exception e (str "error:" (.getMessage e)))))