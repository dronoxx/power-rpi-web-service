(defproject power-rpi-web-service "1.1-SNAPSHOT"
  :description "Web service for raspberry pi to power devices"
  :url "https://github.com/ieer/rpi-web-service"
  :license {:name "GNU General Public License v3.0"
            :url  "https://github.com/xerp/power-rpi-web-service/blob/master/LICENSE"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [aero "1.1.3"]
                 [power "0.4.0"]
                 [compojure "1.6.1"]
                 [ring/ring-jetty-adapter "1.5.1"]
                 [ring/ring-json "0.4.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :repl-options {:timeout 120000}
  :ring {:handler power-rpi-web-service.api/app}
  :profiles {
             :uberjar {:aot :all}
             :dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.0"]]}})
