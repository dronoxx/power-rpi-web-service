(defproject power-rpi-web-service "0.1.0-SNAPSHOT"
  :description "Web service for raspberry pi to power devices"
  :url "https://github.com/ieer/rpi-web-service"
  :license {:name "GNU General Public License v3.0"
            :url  "https://github.com/xerp/power-rpi-web-service/blob/master/LICENSE"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/core.match "0.2.2"]
                 [power "0.4.0"]
                 [ring/ring-jetty-adapter "1.5.1"]]
  :plugins [[lein-ring "0.9.7"]]
  :repl-options {:timeout 120000}
  :ring {:handler power-rpi-web-service.handler/app}
  :main power-rpi-web-service.handler
  :profiles {
             :uberjar {:aot :all}
             :dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.0"]]}})
