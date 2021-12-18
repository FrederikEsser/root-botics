(ns root-botics.game
  (:require [root-botics.util :as ut]
            [medley.core :as medley]))

(def fall {:clearings {:fox-1     {:suit          :fox
                                   :priority      1
                                   :paths         #{:rabbit-5 :mouse-9 :rabbit-10}
                                   :slots         1
                                   :opposite-from :rabbit-3}
                       :mouse-2   {:suit          :mouse
                                   :priority      2
                                   :paths         #{:rabbit-5 :fox-6 :rabbit-10}
                                   :slots         2
                                   :opposite-from :rabbit-4}
                       :rabbit-3  {:suit          :rabbit
                                   :priority      3
                                   :paths         #{:fox-6 :mouse-7 :mouse-11}
                                   :slots         1
                                   :opposite-from :fox-1}
                       :rabbit-4  {:suit          :rabbit
                                   :priority      4
                                   :paths         #{:fox-8 :mouse-9 :fox-12}
                                   :slots         1
                                   :opposite-from :mouse-2}
                       :rabbit-5  {:suit     :rabbit
                                   :priority 5
                                   :paths    #{:fox-1 :mouse-2}
                                   :slots    2}
                       :fox-6     {:suit     :fox
                                   :priority 6
                                   :paths    #{:mouse-2 :rabbit-3 :mouse-11}
                                   :slots    2
                                   :pieces   [{:name :ruin
                                               :type :building}]}
                       :mouse-7   {:suit     :mouse
                                   :priority 7
                                   :paths    #{:rabbit-3 :fox-8 :fox-12}
                                   :slots    2}
                       :fox-8     {:suit     :fox
                                   :priority 8
                                   :paths    #{:rabbit-4 :mouse-7}
                                   :slots    2}
                       :mouse-9   {:suit     :mouse
                                   :priority 9
                                   :paths    #{:fox-1 :rabbit-4 :fox-12}
                                   :slots    2}
                       :rabbit-10 {:suit     :rabbit
                                   :priority 10
                                   :paths    #{:fox-1 :mouse-2 :fox-12}
                                   :slots    2
                                   :pieces   [{:name :ruin
                                               :type :building}]}
                       :mouse-11  {:suit     :mouse
                                   :priority 11
                                   :paths    #{:rabbit-3 :fox-6 :fox-12}
                                   :slots    3
                                   :pieces   [{:name :ruin
                                               :type :building}]}
                       :fox-12    {:suit     :fox
                                   :priority 12
                                   :paths    #{:rabbit-4 :mouse-7 :mouse-9 :rabbit-10 :mouse-11}
                                   :slots    2
                                   :pieces   [{:name :ruin
                                               :type :building}]}}})

(def marquise-de-cat {:name      :marquise-de-cat
                      :warriors  25
                      :tokens    {:keep 1
                                  :wood 8}
                      :buildings {:sawmill   6
                                  :workshop  6
                                  :recruiter 6}})

(def eyrie-dynasties {:name      :eyrie-dynasties
                      :warriors  20
                      :buildings {:roost 7}})

(defn put-piece [game {:keys [piece clearing]}]
  (let [quantity (:quantity piece)
        {:keys [idx]} (ut/get-piece-idx game [:map :clearings clearing :pieces] (select-keys piece [:player :type :name]))]
    (if (and quantity idx)
      (update-in game [:map :clearings clearing :pieces idx :quantity] + quantity)
      (update-in game [:map :clearings clearing :pieces] (comp vec conj) piece))))

(defn take-piece [game {:keys [piece clearing]}]
  (let [quantity (:quantity piece)
        {existing-piece :piece
         idx            :idx} (ut/get-piece-idx game [:map :clearings clearing :pieces] (select-keys piece [:player :type :name]))]
    (assert existing-piece (str "Take error: " piece " is not found in " clearing "."))
    (when quantity
      (assert (:quantity existing-piece) (str "Take error: " existing-piece " has no quantity."))
      (assert (<= quantity (:quantity existing-piece)) (str "Take error: " existing-piece " quantity is < " quantity)))
    (if (and quantity
             (< quantity (:quantity existing-piece)))
      (update-in game [:map :clearings clearing :pieces idx :quantity] - quantity)
      (-> game
          (update-in [:map :clearings clearing :pieces] ut/vec-remove idx)
          (update-in [:map :clearings clearing] ut/dissoc-if-empty :pieces)))))

(defn remove-piece [game {:keys [piece clearing]}]
  (let [{:keys [player type name quantity]} piece]
    (-> game
        (take-piece {:piece    piece
                     :clearing clearing})
        (as-> game
              (case type
                :warrior (update-in game [:players player :warriors] ut/plus quantity)
                :building (update-in game [:players player :buildings name] ut/plus 1)
                :token (update-in game [:players player :tokens name] ut/plus 1))))))

(defn recruit [game {:keys [player quantity clearing]}]
  (let [{:keys [warriors]} (get-in game [:players player])
        piece {:player   player
               :type     :warrior
               :quantity quantity}]
    (assert (>= warriors quantity)
            (str "Recruit error: " player " has too few warriors in supply (" warriors "<" quantity ")."))
    (-> game
        (update-in [:players player :warriors] - quantity)
        (put-piece {:piece    piece
                    :clearing clearing}))))

(defn get-ruler [game {:keys [clearing]}]
  (let [player-pieces (->> (get-in game [:map :clearings clearing :pieces])
                           (filter :player)
                           (filter (comp #{:warrior :building} :type))
                           (group-by :player)
                           (medley/map-vals ut/count-pieces))
        most-pieces   (->> player-pieces
                           vals
                           (apply max 1))
        candidates    (->> player-pieces
                           (medley/filter-vals #(= % most-pieces))
                           keys)]
    (when (= 1 (count candidates))
      (first candidates))))

(defn rules? [game {:keys [player clearing]}]
  (= player (get-ruler game {:clearing clearing})))

(defn move [game {:keys [player quantity from to]}]
  (assert (pos? quantity) (str "Move error: " player " must move at least 1 warrior."))
  (assert (or (= player (get-ruler game {:clearing from}))
              (= player (get-ruler game {:clearing to})))
          (str "Move error: " player " rules neither " from " nor " to "."))
  (assert (-> (get-in game [:map :clearings from :paths])
              (contains? to))
          (str "Move error: " from " doesn't have a path to " to "."))
  (let [piece {:player   player
               :type     :warrior
               :quantity quantity}]
    (-> game
        (take-piece {:piece    piece
                     :clearing from})
        (put-piece {:piece    piece
                    :clearing to}))))

(defn build [game {:keys [player building clearing]}]
  (let [{:keys [buildings]} (get-in game [:players player])
        supply      (get buildings building)
        {:keys [slots pieces]} (get-in game [:map :clearings clearing])
        empty-slots (->> pieces
                         (filter (comp #{:building} :type))
                         count
                         (- slots))
        piece       {:player player
                     :name   building
                     :type   :building}]
    (assert (pos? supply) (str "Build error: " player " has no " building " in supply."))
    (assert (pos? empty-slots) (str "Build error: " clearing " has no empty slots."))
    (-> game
        (update-in [:players player :buildings building] dec)
        (put-piece {:piece    piece
                    :clearing clearing}))))

(defn roll-die []
  (rand-int 4))

(defn take-hits [game {:keys [player hits clearing]}]
  (let [{:keys [pieces]} (get-in game [:map :clearings clearing])
        warriors     (ut/count-warriors pieces player)
        other-pieces (->> pieces
                          (filter (comp #{player} :player))
                          (remove (comp #{:warrior} :type))
                          shuffle                           ; randomize buildings
                          (sort-by :type)
                          reverse                           ; put tokens before buildings
                          (take (- hits warriors))
                          (map (fn [piece]                  ; prepare params for reduce remove-piece
                                 {:piece    piece
                                  :clearing clearing})))]
    (-> game
        (cond-> (pos? warriors) (remove-piece {:piece    {:player   player
                                  :type     :warrior
                                  :quantity (min hits warriors)}
                                               :clearing clearing}))
        (as-> game (reduce remove-piece game other-pieces)))))

(defn battle [game {:keys [attacker defender clearing extra-hits rolls]
                    :or   {extra-hits 0}}]
  (let [{:keys [pieces]} (get-in game [:map :clearings clearing])
        attacker-warriors (ut/count-warriors pieces attacker)
        defender-warriors (ut/count-warriors pieces defender)
        defenseless?      (zero? defender-warriors)
        extra-hits        (cond-> extra-hits
                                  defenseless? inc)
        [attacker-roll
         defender-roll] (->> (or rolls
                                 [(roll-die)
                                  (roll-die)])
                             (sort >))
        attacker-hits     (+ (min attacker-roll attacker-warriors)
                             extra-hits)
        defender-hits     (min defender-roll defender-warriors)]
    (assert (pos? attacker-warriors) (str "Battle error: " attacker " has no warriors in " clearing "."))
    (assert (some (comp #{defender} :player) pieces) (str "Battle error: " defender " has no pieces in " clearing "."))
    (-> game
        (take-hits {:player   defender
                    :hits     attacker-hits
                    :clearing clearing})
        (take-hits {:player   attacker
                    :hits     defender-hits
                    :clearing clearing}))))
