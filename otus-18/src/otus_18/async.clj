(ns otus-18.async
  (:require
   [clojure.core.async
    :as
    a
    :refer
    [<! <!! >! >!! alts! chan close! go go-loop pipeline-async pipeline thread timeout]]))

;; Всем ли хорошо видно?

;; * Каналы (Channels)

(def echo-chan (chan))
(go (println (<! echo-chan)))
(>!! echo-chan "bottle")
; => true
; => bottle

;; * Буферизация (Buffers)

(def echo-buffer (chan 2))
(>!! echo-buffer :hello)
; => true
(>!! echo-buffer :world)
; => true
(>!! echo-buffer :!)
; Третий вызов будет блокирующим, т.к. буфер переполнен

;; Note: `sliding-buffer` - при переполнении старые данные будут удаляться из буфера
;;       `dropping-buffer` - новые данные не будут попадать в переполненный буфер
;;       при их использовании `>!!` никогда не заблокирует поток выполнения

;; * Блокирование и Паркинг (Blocking and Parking)

;; `>!`, `<!` - могут быть использованы только внутри `go`-блока
;; `>!!`, `<!!` - могут быть использованы как в `go`-блоках, так и в основном коде

(def n-ch (chan))
(doseq [n (range 1000)]
  (go (>! n-ch n)))

;; `>!`, `<!` - park thread
;; `>!!`, `<!!` - block thread

;; * thread

(def ch (chan))

(time (do
        (doseq [n (range 9)]
          (go
            (Thread/sleep 1000)
            (>! ch n)))
        (doseq [_ (range 9)]
          (<!! ch))))
; => "Elapsed time: 2016.62955 msecs"


(def another-echo-chan (chan))
(thread (println (<!! another-echo-chan)))
(>!! another-echo-chan "hi")
; => true
; => hi

;; * alts!


(def ch-a (chan))
(def ch-b (chan))
(go (println (alts! [ch-a ch-b])))
(>!! ch-b "bottle")

(go (println (alts! [ch-a ch-b (timeout 1000)])))

;; * pipeline


(def capitalizer (map clojure.string/capitalize))

(def input (chan))
(def output (chan))

(go-loop []
  (when-let [x (<! output)]
    (println x)
    (recur)))

(pipeline 1 output capitalizer input)

(>!! input "hello")

(close! input)

(<!! output)

;; * pipeline-async

(def ca> (chan 1))
(def cb> (chan 1))

(defn c-af [val result] ; notice the signature is different for `pipeline-async`, it includes a channel
  (go (<! (timeout 1000))
      (>! result (str val "!!!"))
      (>! result (str val "!!!"))
      (>! result (str val "!!!"))
      (close! result)))

(pipeline-async 1 cb> c-af ca>)

(go (println (<! cb>)))
(>!! ca> "hello")
