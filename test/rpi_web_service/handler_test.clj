(ns rpi-web-service.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [rpi-web-service.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "RPI Device"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
