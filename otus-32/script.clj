#!/usr/bin/env bb -I

(defn sum-a-and-b []
  (let [[{:keys [a b]} & _] *input*]
    {:result (+ a b)}))

(prn (sum-a-and-b))
