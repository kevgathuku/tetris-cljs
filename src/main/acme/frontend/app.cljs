(ns acme.frontend.app
  (:require [reagent.core :as r] [reagent.dom.client :as rdom]
            [acme.frontend.tetris.block :as block]))

(def messages
  ["Hello, world!"
   "Atoms are simple and powerful."
   "ClojureScript runs in the browser."
   "State changes drive rendering."
   "Shadow-cljs makes this easy."])

(defonce app-state
  (r/atom {:current-index nil :current-block nil}))

(defonce tick-interval (atom nil))

(defn- random-index []
  (rand-int (count messages)))

(defn- assign-initial-state! []
  (swap! app-state assoc :current-block (block/create {}))
  (swap! app-state assoc :current-index (random-index)))

(defn- shuffle-message! []
  (swap! app-state
         (fn [{:keys [current-index] :as state}]
           (let [new-index (random-index)]
             ;; no change if it's different
             (if (= new-index current-index)
               state
               (assoc state :current-index new-index))))))

(defn- tick-game! []
  (let [current-block (:current-block @app-state)
        at-bottom? (>= (get-in current-block [:location :y]) 20)]
    (println "At bottom?" at-bottom?)

    (if at-bottom?
      (swap! app-state assoc :current-block (block/create {}))
      (swap! app-state update :current-block block/move-down))))

(defn- stop-tick! []
  (when @tick-interval
    (js/clearInterval @tick-interval)
    (reset! tick-interval nil)))

(defn- start-tick! []
  (stop-tick!)  ; Reuse stop logic for cleanup
  (reset! tick-interval
          (js/setInterval tick-game! 800)))

(defn- board [current-block]
  [:svg {:width 200 :height 400}
   [:rect {:width 200 :height 400 :fill "black"}]
   (let [{:keys [x y]} (:location current-block)]
     [:rect {:x (* x 20) ; Scale to grid
             :y (* (dec y) 20) ; start at 0 - increment by 20 downwards each tick
             :width 20
             :height 20
             :fill "red"}])])

(defn tetris []
  (let [{:keys [current-block]} @app-state
        points (when current-block (block/points current-block))]
    [:div.hero
     [:h3 "Tetris"]
     (if current-block
       [:div
        [:p (str "Shape: " (:shape current-block))]
        [:p (str "Rotation: " (:rotation current-block) "°")]
        [:p (str "Location: x=" (get-in current-block [:location :x])
                 " y=" (get-in current-block [:location :y]))]
        [:p "Points: " (pr-str points)]]
       [:p "No block created yet"])
     [board current-block]]))

(defn app []
  (let [{:keys [current-index]} @app-state
        message (when current-index
                  (nth messages current-index))]
    [:<>
     [:div {:style {:font-family "sans-serif"}}
      [:h2 (str "Random Message: " current-index)]
      [:p {:style {:font-size "1.2em"}}
       message]
      [:button {:on-click shuffle-message!}
       "Shuffle"]]
     [tetris]]))

(defonce root (rdom/create-root (.getElementById js/document "app")))

(defn mount! []
  (rdom/render root [app]))

(defn ^:dev/before-load stop []
  (stop-tick!))

(defn ^:export ^:dev/after-load init []
  (assign-initial-state!)
  (start-tick!)
  (mount!))

