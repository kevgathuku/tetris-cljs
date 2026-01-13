(ns acme.frontend.app
  (:require [reagent.core :as r] [reagent.dom.client :as rdom]
            [acme.frontend.tetris.block :as block]))

(defonce app-state
  (r/atom {:tetro nil}))

(defonce tick-interval (atom nil))

(defn- shuffle-block! []
  (swap! app-state assoc :tetro (block/create {})))

(defn- tick-game! []
  (let [tetro (:tetro @app-state)
        points (block/show tetro)
        max-y (apply max (map #(second (first %)) points))
        at-bottom? (>= max-y 19)]
    ; (js/console.log "Max Y:" max-y "At bottom?" at-bottom?)

    (if at-bottom?
      (swap! app-state assoc :tetro (block/create {}))
      (swap! app-state update :tetro block/move-down))))

(defn- stop-tick! []
  (when @tick-interval
    (js/clearInterval @tick-interval)
    (reset! tick-interval nil)))

(defn- start-tick! []
  (stop-tick!)  ; Reuse stop logic for cleanup
  (reset! tick-interval
          (js/setInterval tick-game! 800)))

(defn- render-points [points]
  ; (js/console.log "points:" (pr-str  points))
  (into [:g]
        (for [[[x y] color] points] [:rect {:x (* x 20)
                                            :y (* y 20)
                                            :width 20
                                            :height 20
                                            :fill color}])))

(defn- board [tetro]
  [:svg {:width 200 :height 400}
   [:rect {:width 200 :height 400 :fill "black"}]
   [render-points (block/show tetro)]])

(defn tetris []
  (let [{:keys [tetro]} @app-state]
    [:div
     (if tetro
       [:div
        [:p (str "Shape: " (:shape tetro))]
        [:p (str "Rotation: " (:rotation tetro) "°")]
        [:p (str "Location: " (pr-str (:location tetro)))]
        [:p "Points: " (pr-str (block/show tetro))]]
       [:p "No block created yet"])
     [board tetro]]))

(defn- handle-keydown
  "Handles keyboard input for tetris controls.

  Arrow keys: Move and rotate blocks
  Space: Rotate block

  Prevents default behavior to avoid page scrolling."
  [e]
  (when (contains? #{"ArrowLeft" "ArrowRight" "ArrowDown" "ArrowUp" " "} (.-key e))
    (.preventDefault e))
  (case (.-key e)
    "ArrowLeft"  (swap! app-state update :tetro block/move-left)
    "ArrowRight" (swap! app-state update :tetro block/move-right)
    "ArrowDown"  (swap! app-state update :tetro block/move-down)
    "ArrowUp"    (swap! app-state update :tetro block/rotate)
    " "          (swap! app-state update :tetro block/rotate)
    nil))

(defn app []
  [:div {:tab-index 0
         :auto-focus true
         :on-key-down handle-keydown}
   [:h2 {:style {:font-family "sans-serif"}} "Tetris"]
   [:button {:on-click shuffle-block!}
    "Shuffle"]
   [tetris]])

(defonce root (rdom/create-root (.getElementById js/document "app")))

(defn mount! []
  (rdom/render root [app]))

(defn ^:dev/before-load stop []
  (stop-tick!))

(defn ^:export ^:dev/after-load init []
  (shuffle-block!)
  (start-tick!)
  (mount!))

(comment
  (let [points [[[4 3] "red"] [[4 4] "red"] [[4 5] "red"] [[5 5] "red"]]]
    (map #(second (first %)) points)))
