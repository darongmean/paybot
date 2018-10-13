(ns fbbot.preload
  (:require
    [cljs.spec.alpha :as s]
    [cljs.spec.test.alpha :as st]

    [expound.alpha :as expound]

    [pjstadig.humane-test-output]))


;; activate `pjstadig.humane-test-output` by require in the name space


(set! s/*explain-out* expound/printer)

(s/check-asserts true)

(st/instrument)
