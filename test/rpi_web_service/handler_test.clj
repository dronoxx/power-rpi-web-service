(ns rpi-web-service.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [rpi-web-service.handler :refer :all]
            [cheshire.core :refer :all]))

(def response-code-expected 200)
(def content-type-expected "application/json")

(deftest test-app
  (testing "Check availability test"
    (let [response (app (mock/request :get "/"))
          body-expected "RPI Device"]
      (is (= (:status response) response-code-expected))
      (is (= (:body response) body-expected))))

  (testing "Power on projector test"
    (let [response (app (mock/request :put "/power/on"))
          body-expected {:command "POWER" :option "ON" :status "OK"}]
      (is (= (:status response) response-code-expected))
      (is (.contains (get-in response [:headers "Content-Type"]) content-type-expected))
      (is (= (parse-string (:body response) true) body-expected))))

  (testing "Power off projector test"
    (let [response (app (mock/request :put "/power/off"))
          body-expected {:command "POWER" :option "OFF" :status "OK"}]
      (is (= (:status response) response-code-expected))
      (is (.contains (get-in response [:headers "Content-Type"]) content-type-expected))
      (is (= (parse-string (:body response) true) body-expected))))

  (testing "Freeze on projector test"
    (let [response (app (mock/request :put "/freeze/on"))
          body-expected {:command "FREEZE" :option "ON" :status "OK"}]
      (is (= (:status response) response-code-expected))
      (is (.contains (get-in response [:headers "Content-Type"]) content-type-expected))
      (is (= (parse-string (:body response) true) body-expected))))

  (testing "Freeze off projector test"
    (let [response (app (mock/request :put "/freeze/off"))
          body-expected {:command "FREEZE" :option "OFF" :status "OK"}]
      (is (= (:status response) response-code-expected))
      (is (.contains (get-in response [:headers "Content-Type"]) content-type-expected))
      (is (= (parse-string (:body response) true) body-expected))))

  (testing "Command not found route test"
    (let [response (app (mock/request :put "/invalid-command/on"))]
      (is (= (:status response) 404))))

  (testing "Not found route test"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
