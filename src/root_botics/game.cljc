(ns root-botics.game)

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
