(ns root-botics.util)

(defn plus [n m]
  (if n (+ n m) m))

(defn dissoc-if-empty [map key]
  (cond-> map
          (empty? (get map key)) (dissoc key)))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (let [vcoll (vec coll)]
    (vec (concat (subvec vcoll 0 pos) (subvec vcoll (inc pos))))))

(defn match [data-1]
  (fn [data-2]
    (->> data-1
         (every? (fn [[key val]]
                   (let [values (if (set? val) val #{val})]
                     (contains? values (get data-2 key))))))))

(defn get-piece-idx [game path criteria]
  (->> (get-in game path)
       (keep-indexed (fn [idx piece]
                       (when ((match criteria) piece) {:idx idx :piece piece})))
       first))

(defn update-in-vec [game path criteria f & args]
  (let [{:keys [idx]} (get-piece-idx game path criteria)]
    (-> game
        (update-in path vec)
        (as-> game (apply update-in game (concat path [idx]) f args)))))

(defn count-pieces [pieces]
  (->> pieces
       (map (fn [{:keys [quantity]}]
              (or quantity 1)))
       (apply + 0)))

(defn count-warriors [pieces player]
  (->> pieces
       (keep (fn [{:keys [quantity] :as piece}]
               (when (and (= player (:player piece))
                          (= :warrior (:type piece)))
                 quantity)))
       (apply + 0)))
