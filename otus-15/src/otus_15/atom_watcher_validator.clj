(ns otus-15.atom-watcher-validator
  (:gen-class))

;;
;; Атомы
;;

(def oleg
  (atom {:name "Oleg"
         :age 25
         :wears-glasses? false}))
; => #'user/oleg

@oleg

(swap! oleg #(update % :age + 3))
; => {:age 28, :wears-glasses? false, :name "Oleg"}

(swap! oleg update :age + 3)
; => {:age 31, :wears-glasses? false, :name "Oleg"}

@oleg

(reset! oleg {:name "Nikita"})
; => {:name "Nikita"}

(def resources-by-user
  (atom {1005 {:cpu 35
               :store 63466734
               :memory 10442856
               :pids #{6266, 5426, 6542}}}))

(swap! resources-by-user update-in [1005 :pids] conj 9999)

@resources-by-user
(deref resources-by-user)

(comment
  ;; Альтернативные варианты записи:

  ;; 1)
  (fn [atom-value]
    (update-in atom-value [1005 :pids] conj 9999))

  ;; 2)
  (fn [atom-value]
    (let [pids (get-in atom-value [1005 :pids])
          new-pids (conj pids 9999)
          new-atom-avalue (assoc-in atom-value [1005 :pids] new-pids)]
      new-atom-avalue))
  )

;; Семантика «сравнить и изменить» и повторы

(def accumulator
  (atom 0))

(defn +slow
  [a b timeout]
  (println (format "current: %s, delta: %s, timeout: %s" a b timeout))
  (Thread/sleep timeout)
  (+ a b))

(defn retry-example
  []
  (future (swap! accumulator +slow 1 2000))
  (future (swap! accumulator +slow 2 5000)))

(retry-example)

@accumulator

(comment
  "current: 0, delta: 1, timeout: 2000"
  "current: 0, delta: 2, timeout: 5000"
  ;; -> #<Future@257a94bc: :pending>
  "current: 1, delta: 2, timeout: 5000"
  )

;;
;; Применение атомов для сохранения результата функции
;;

;; Замыкание атома внутри анонимной функции.
(defn memoize
  [f]
  (let [mem (atom {})]
    (fn [& args]
      (if-let [e (find @mem args)]
        (val e)
        (let [ret (apply f args)]
          (swap! mem assoc args ret)
          ret)))))

(def +mem (memoize +slow))

(time (+mem 1 2 2000))
;; => Elapsed time: 2001.006041 msecs
(time (+mem 1 2 2000))
;; => Elapsed time: 0.124338 msecs

;;
;; Функции-наблюдатели
;;

;; На момент вызова наблюдателя значение в ref'е уже может быть изменено.
;; Необходимо опираться на аргументы old и new.
(defn echo-watch
  [key ref old new]
  (println key ref old "=>" new))

(add-watch accumulator :echo echo-watch)

;; В наблюдателя передаётся только конечный результат вызова reset!/swap!.
;; Перезапуски будут проигноированы, т.к. их результат отбрасывается.
(retry-example)

;; Наблюдатель вызвается даже если в результате вызовов
;; reset!/swap! значение в атоме не было изменено.
(reset! accumulator @accumulator)

(defn echo-watch*
  [key ref old new]
  (when (not= old new)
    (println key ref old "=>" new)))

;; Заменяю наблюдателя, зарегистрированного под ключом :echo.
(add-watch accumulator :echo echo-watch*)

(reset! accumulator @accumulator)

;; Удаляю наблюдателя с ключом :echo.
(remove-watch accumulator :echo)

(reset! accumulator (inc @accumulator))

;; Пример логирования при помощи наблюдателей.

(defn log->file
  [filename _ref old new]
  (when (not= old new)
    (spit filename (str new \newline) :append true)))

(def nikita
  (atom {:name "Nikita", :age 23}))

;; Наблюдатель, принимающий аргумент через ключ.
(add-watch nikita "program.log" log->file)

(swap! nikita #(update % :age inc))
(swap! nikita update :age inc)
(swap! nikita identity)
(swap! nikita assoc :wears-glasses? true)
(swap! nikita update :age inc)

(def history
  (atom ()))

(defn log->atom
  [dest-atom key ref old new]
  nil)

;; Наблююатель - частично применённая функция.
(add-watch nikita :record (partial log->atom history))

(swap! nikita update :age inc)
(swap! nikita update :age inc)

@history

;;
;; Функции-валидаторы
;;

(def acc
  (atom 0 :validator nat-int?))
;; => #'user/acc

(swap! acc dec)
;; => IllegalStateException

;; Ссылка может иметь только один валидатор.
;; Установка нового валидатора подменяет исходный.
(set-validator! acc int?)
;; => nil

(swap! acc dec)
;; => -1

(set-validator! acc (fn [x]
                      (or (int? x)
                          (throw (IllegalArgumentException.)))))
;; => nil

(reset! acc "string")
;; => IllegalArgumentException

(set-validator! acc nil)
;; => nil

(reset! acc "string")
;; => "string"
