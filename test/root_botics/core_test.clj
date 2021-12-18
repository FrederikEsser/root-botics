(ns root-botics.core-test
  (:require [clojure.test :refer :all]
            [root-botics.test-utils :refer :all]
            [root-botics.game :refer :all]))

(defn fixture [f]
  (with-rand-seed 123 (f)))

(use-fixtures :each fixture)

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
    (is (= (-> {:players {:marquise-de-cat {:warriors 1}}
                :map     {:clearings {:fox-1 {:pieces [{:player   :marquise-de-cat
                                                        :type     :warrior
                                                        :quantity 2}]}}}}
               (recruit {:player   :marquise-de-cat
                         :clearing :fox-1
                         :quantity 1}))
           {:players {:marquise-de-cat {:warriors 0}}
            :map     {:clearings {:fox-1 {:pieces [{:player   :marquise-de-cat
                                                    :type     :warrior
                                                    :quantity 3}]}}}}))
    (is (thrown-with-msg? AssertionError #"Recruit error:"
                          (-> {:players {:marquise-de-cat {:warriors 3}}
                               :map     {:clearings {:fox-1 {}}}}
                              (recruit {:player   :marquise-de-cat
                                        :clearing :fox-1
                                        :quantity 4}))))))

(deftest move-test
  (testing "Move"
    (is (= (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                       :type     :warrior
                                                       :quantity 1}]
                                             :paths  #{:rabbit-2}}
                                  :rabbit-2 {}}}}
               (move {:player   :marquise-de-cat
                      :quantity 1
                      :from     :fox-1
                      :to       :rabbit-2}))
           {:map {:clearings {:fox-1    {:paths #{:rabbit-2}}
                              :rabbit-2 {:pieces [{:player   :marquise-de-cat
                                                   :type     :warrior
                                                   :quantity 1}]}}}}))
    (is (= (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                       :type     :warrior
                                                       :quantity 3}]
                                             :paths  #{:rabbit-2}}
                                  :rabbit-2 {}}}}
               (move {:player   :marquise-de-cat
                      :quantity 2
                      :from     :fox-1
                      :to       :rabbit-2}))
           {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                   :type     :warrior
                                                   :quantity 1}]
                                         :paths  #{:rabbit-2}}
                              :rabbit-2 {:pieces [{:player   :marquise-de-cat
                                                   :type     :warrior
                                                   :quantity 2}]}}}}))
    (is (thrown-with-msg? AssertionError #"Take error:"
                          (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                                      :type     :warrior
                                                                      :quantity 1}]
                                                            :paths  #{:rabbit-2}}
                                                 :rabbit-2 {}}}}
                              (move {:player   :marquise-de-cat
                                     :quantity 2
                                     :from     :fox-1
                                     :to       :rabbit-2}))))
    (is (thrown-with-msg? AssertionError #"Move error:"
                          (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                                      :type     :warrior
                                                                      :quantity 1}]
                                                            :paths  #{:rabbit-2}}
                                                 :rabbit-2 {}}}}
                              (move {:player   :marquise-de-cat
                                     :quantity 0
                                     :from     :fox-1
                                     :to       :rabbit-2}))))
    (testing "rule"
      (is (= (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                         :type     :warrior
                                                         :quantity 2}
                                                        {:player   :eyrie-dynasties
                                                         :type     :warrior
                                                         :quantity 1}]
                                               :paths  #{:rabbit-2}}
                                    :rabbit-2 {:pieces [{:player   :marquise-de-cat
                                                         :type     :warrior
                                                         :quantity 1}
                                                        {:player   :eyrie-dynasties
                                                         :type     :warrior
                                                         :quantity 1}]}}}}
                 (move {:player   :marquise-de-cat
                        :quantity 1
                        :from     :fox-1
                        :to       :rabbit-2}))
             {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                     :type     :warrior
                                                     :quantity 1}
                                                    {:player   :eyrie-dynasties
                                                     :type     :warrior
                                                     :quantity 1}]
                                           :paths  #{:rabbit-2}}
                                :rabbit-2 {:pieces [{:player   :marquise-de-cat
                                                     :type     :warrior
                                                     :quantity 2}
                                                    {:player   :eyrie-dynasties
                                                     :type     :warrior
                                                     :quantity 1}]}}}}))
      (is (= (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                         :type     :warrior
                                                         :quantity 1}
                                                        {:player   :eyrie-dynasties
                                                         :type     :warrior
                                                         :quantity 1}]
                                               :paths  #{:rabbit-2}}
                                    :rabbit-2 {:pieces [{:player   :marquise-de-cat
                                                         :type     :warrior
                                                         :quantity 2}
                                                        {:player   :eyrie-dynasties
                                                         :type     :warrior
                                                         :quantity 1}]}}}}
                 (move {:player   :marquise-de-cat
                        :quantity 1
                        :from     :fox-1
                        :to       :rabbit-2}))
             {:map {:clearings {:fox-1    {:pieces [{:player   :eyrie-dynasties
                                                     :type     :warrior
                                                     :quantity 1}]
                                           :paths  #{:rabbit-2}}
                                :rabbit-2 {:pieces [{:player   :marquise-de-cat
                                                     :type     :warrior
                                                     :quantity 3}
                                                    {:player   :eyrie-dynasties
                                                     :type     :warrior
                                                     :quantity 1}]}}}}))
      (is (thrown-with-msg? AssertionError #"Move error:"
                            (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                                        :type     :warrior
                                                                        :quantity 1}
                                                                       {:player   :eyrie-dynasties
                                                                        :type     :warrior
                                                                        :quantity 1}]
                                                              :paths  #{:rabbit-2}}
                                                   :rabbit-2 {:pieces [{:player   :marquise-de-cat
                                                                        :type     :warrior
                                                                        :quantity 1}
                                                                       {:player   :eyrie-dynasties
                                                                        :type     :warrior
                                                                        :quantity 1}]}}}}
                                (move {:player   :marquise-de-cat
                                       :quantity 1
                                       :from     :fox-1
                                       :to       :rabbit-2}))))
      (is (thrown-with-msg? AssertionError #"Move error:"
                            (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                                        :type     :warrior
                                                                        :quantity 1}
                                                                       {:player   :eyrie-dynasties
                                                                        :type     :warrior
                                                                        :quantity 1}]
                                                              :paths  #{:rabbit-2}}
                                                   :rabbit-2 {}}}}
                                (move {:player   :marquise-de-cat
                                       :quantity 1
                                       :from     :fox-1
                                       :to       :rabbit-2})))))
    (is (thrown-with-msg? AssertionError #"Move error:"
                          (-> {:map {:clearings {:fox-1    {:pieces [{:player   :marquise-de-cat
                                                                      :type     :warrior
                                                                      :quantity 1}]
                                                            :paths  #{:rabbit-3}}
                                                 :rabbit-2 {}}}}
                              (move {:player   :marquise-de-cat
                                     :quantity 1
                                     :from     :fox-1
                                     :to       :rabbit-2}))))))

(deftest build-test
  (testing "Build"
    (is (= (-> {:players {:marquise-de-cat {:buildings {:sawmill 6}}}
                :map     {:clearings {:fox-1 {:slots 1}}}}
               (build {:player   :marquise-de-cat
                       :building :sawmill
                       :clearing :fox-1}))
           {:players {:marquise-de-cat {:buildings {:sawmill 5}}}
            :map     {:clearings {:fox-1 {:slots  1
                                          :pieces [{:player :marquise-de-cat
                                                    :name   :sawmill
                                                    :type   :building}]}}}}))
    (is (= (-> {:players {:marquise-de-cat {:buildings {:sawmill 1}}}
                :map     {:clearings {:fox-1 {:slots  2
                                              :pieces [{:player :marquise-de-cat
                                                        :name   :sawmill
                                                        :type   :building}]}}}}
               (build {:player   :marquise-de-cat
                       :building :sawmill
                       :clearing :fox-1}))
           {:players {:marquise-de-cat {:buildings {:sawmill 0}}}
            :map     {:clearings {:fox-1 {:slots  2
                                          :pieces [{:player :marquise-de-cat
                                                    :name   :sawmill
                                                    :type   :building}
                                                   {:player :marquise-de-cat
                                                    :name   :sawmill
                                                    :type   :building}]}}}}))
    (is (thrown-with-msg? AssertionError #"Build error:"
                          (-> {:players {:marquise-de-cat {:buildings {:sawmill 0}}}
                               :map     {:clearings {:fox-1 {:slots 1}}}}
                              (build {:player   :marquise-de-cat
                                      :building :sawmill
                                      :clearing :fox-1}))))
    (is (thrown-with-msg? AssertionError #"Build error:"
                          (-> {:players {:marquise-de-cat {:buildings {:sawmill 1}}}
                               :map     {:clearings {:fox-1 {:slots  1
                                                             :pieces [{:player :marquise-de-cat
                                                                       :name   :sawmill
                                                                       :type   :building}]}}}}
                              (build {:player   :marquise-de-cat
                                      :building :sawmill
                                      :clearing :fox-1}))))))

(deftest battle-test
  (testing "Battle"
    (is (= (-> {:players {:marquise-de-cat {:warriors 5}
                          :eyrie-dynasties {:warriors 10}}
                :map     {:clearings {:fox-1 {:pieces [{:player   :marquise-de-cat
                                                        :type     :warrior
                                                        :quantity 2}
                                                       {:player   :eyrie-dynasties
                                                        :type     :warrior
                                                        :quantity 1}]}}}}
               (battle {:attacker :marquise-de-cat
                        :defender :eyrie-dynasties
                        :clearing :fox-1
                        :rolls    [1 1]}))
           {:players {:marquise-de-cat {:warriors 6}
                      :eyrie-dynasties {:warriors 11}}
            :map     {:clearings {:fox-1 {:pieces [{:player   :marquise-de-cat
                                                    :type     :warrior
                                                    :quantity 1}]}}}}))))
