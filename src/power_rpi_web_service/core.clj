(ns power-rpi-web-service.core
  (:require [clojure.string :refer [upper-case]]
            [clojure.java.io :refer [resource]]
            [aero.core :refer [read-config]]))


(defn read-edn-file
  "Return configuration at edn file"
  [file]
  (read-config (resource (apply str [(name file) "." "edn"]))))

(defn map-values
  "Applies the f to the keys and create a map for these"
  ^{:author "Santiago de Pedro"
    :added  "1.0"}
  [m keys f & args]
  (reduce #(apply update-in %1 [%2] f args) m keys))


(defn upper-case-map
  "Convert to upper case all map values"
  ^{:author "Santiago de Pedro"
    :added  "1.0"}
  [response]
  (map-values response (keys response) upper-case))

