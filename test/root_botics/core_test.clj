(ns root-botics.core-test
  (:require [clojure.test :refer :all]
            [root-botics.game :refer :all]))

(deftest fall-test
  (testing "Fall"
    (let [clearings (:clearings fall)]
      (testing "clearing names"
        (->> clearings
             (map (fn [[clearing {:keys [suit priority]}]]
                    (is (= (str clearing)
                           (str suit "-" priority))
                        (str clearing " doesn't match suit " suit ", priority " priority))))
             doall))
      (testing "paths"
        (->> clearings
             (mapcat (fn [[c1 {:keys [paths]}]]
                       (->> paths
                            (keep (fn [c2]
                                    (is (-> clearings
                                            (get c2)
                                            :paths
                                            (contains? c1))
                                        (str c2 " doesn't have a path to " c1)))))))
             doall))
      (testing "corners"
        (is (= 4 (->> clearings
                      vals
                      (filter :opposite-from)
                      count)))
        (->> clearings
             (map (fn [[c1 {:keys [opposite-from]}]]
                    (when opposite-from
                      (let [c2 (get clearings opposite-from)]
                        (is (= c1 (:opposite-from c2))
                            (str c1 " isn't the opposite corner from " opposite-from))))))
             doall)))))

(deftest recruit-test
  (testing "Recruit"
    (is (= (-> {:players {:marquise-de-cat {:warriors 25}}
                :map     {:clearings {:fox-1 {}}}}
               (recruit {:player   :marquise-de-cat
                         :clearing :fox-1
                         :quantity 2}))
           {:players {:marquise-de-cat {:warriors 23}}
            :map     {:clearings {:fox-1 {:pieces [{:player   :marquise-de-cat
                                                    :type     :warrior
                                                    :quantity 2}]}}}}))
    (is (= (-> {:players {:marquise-de-cat {:warriors 23}}
                :map     {:clearings {:fox-1 {:pieces [{:player   :marquise-de-cat
                                                        :type     :warrior
                                                        :quantity 2}]}}}}
               (recruit {:player   :marquise-de-cat
                         :clearing :fox-1
                         :quantity 1}))
           {:players {:marquise-de-cat {:warriors 22}}
            :map     {:clearings {:fox-1 {:pieces [{:player   :marquise-de-cat
                                                    :type     :warrior
                                                    :quantity 3}]}}}}))
    (is (thrown-with-msg? AssertionError #"Recruit error:"
                          (-> {:players {:marquise-de-cat {:warriors 3}}
                               :map     {:clearings {:fox-1 {}}}}
                              (recruit {:player   :marquise-de-cat
                                        :clearing :fox-1
                                        :quantity 4}))))))
