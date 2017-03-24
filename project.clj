(defproject projector-rpi-web-service "0.1.0-SNAPSHOT"
  :description "Projector web service on raspberry pi"
  :url "https://github.com/ieer/rpi-web-service"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/core.match "0.2.2"]
                 [projector "0.8.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler projector-rpi-web-service.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
