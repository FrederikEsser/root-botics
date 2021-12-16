(ns root-botics.util)

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
