(ns fbbot.core-dev
  (:require
    [cljs.test :refer (deftest is)]

    [pjstadig.humane-test-output]))


;; activate `pjstadig.humane-test-output` by require in the name space


(deftest core-dev-namespace-loaded
  (is (= 1 1)))