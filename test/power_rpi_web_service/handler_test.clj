(ns power-rpi-web-service.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [power-rpi-web-service.api :refer :all]
            [cheshire.core :refer :all]))

(def response-code-expected 200)
(def content-type-expected "application/json")
(def default-status-expected "OK")

(deftest test-app
  (testing "Check availability test"
    (let [response (app (mock/request :get "/"))
          body-expected "RPI Device"]
      (is (= (:status response) response-code-expected))
      (is (= (:body response) body-expected))))

  (testing "Power on devices test"
    (let [response (app (mock/request :put "/power/on"))
          body-expected {:command "POWER" :option "ON" :status default-status-expected}]
      (is (= (:status response) response-code-expected))
      (is (.contains (get-in response [:headers "Content-Type"]) content-type-expected))
      (is (= (parse-string (:body response) true) body-expected))))

  (testing "Power off devices test"
    (let [response (app (mock/request :put "/power/off"))
          body-expected {:command "POWER" :option "OFF" :status default-status-expected}]
      (is (= (:status response) response-code-expected))
      (is (.contains (get-in response [:headers "Content-Type"]) content-type-expected))
      (is (= (parse-string (:body response) true) body-expected))))

  (testing "Service status test"
    (let [response (app (mock/request :get "/status"))]
      (is (.contains (get-in response [:headers "Content-Type"]) content-type-expected))
      (is (= (:status response) response-code-expected))))

  (testing "Command not found test"
    (let [response (app (mock/request :put "/invalid-command/on"))]
      (is (= (:status response) 404))))

  (testing "Not found route test"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
