(ns acme.frontend.app
  (:require [reagent.core :as r] [reagent.dom.client :as rdom]
            [acme.frontend.tetris.game :as game]
            [acme.frontend.tetris.block :as block]))

(defonce app-state
  (r/atom {:game nil}))

(defonce tick-interval (atom nil))

(defn- new-game []
  (swap! app-state assoc :game (game/new-game)))

(defn- new-tetromino []
  (swap! app-state update :game game/new-tetro))

(defn- tick-game! []
  (let [game (:game @app-state)
        points (:points game)
        max-y (apply max (map #(second (first %)) points))
        at-bottom? (>= max-y 19)]
    ; (js/console.log "Max Y:" max-y "At bottom?" at-bottom?)

    (if at-bottom?
      (swap! app-state update :game game/new-tetro)
      (swap! app-state update :game game/down))))

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

(defn- board [game]
  [:svg {:width 200 :height 400}
   [:rect {:width 200 :height 400 :fill "black"}]
   [render-points (:points game)]])

(defn tetris []
  (let [{:keys [game]} @app-state]
    [:div
     (if game
       [:div
        [:p (str "Shape: " (:shape (:tetro game)))]
        [:p (str "Rotation: " (:rotation (:tetro game)) "°")]
        [:p (str "Location: " (pr-str (:location (:tetro game))))]
        [:p "Points: " (pr-str (:points game))]]
       [:p "No block created yet"])
     [board game]]))

(defn- handle-keydown
  "Handles keyboard input for tetris controls.

  Arrow keys: Move and rotate blocks
  Space: Rotate block

  Prevents default behavior to avoid page scrolling."
  [e]
  (when (contains? #{"ArrowLeft" "ArrowRight" "ArrowDown" "ArrowUp" " "} (.-key e))
    (.preventDefault e))
  (case (.-key e)
    "ArrowLeft"  (swap! app-state update :game game/left)
    "ArrowRight" (swap! app-state update :game game/right)
    "ArrowDown"  (swap! app-state update :game game/down)
    "ArrowUp"    (swap! app-state update :game game/rotate)
    " "          (swap! app-state update :game game/rotate)
    nil))

(defn app []
  [:div {:tab-index 0
         :auto-focus true
         :on-key-down handle-keydown}
   [:h2 {:style {:font-family "sans-serif"}} "Tetris"]
   [:button {:on-click new-tetromino}
    "Shuffle"]
   [tetris]])

(defonce root (rdom/create-root (.getElementById js/document "app")))

(defn mount! []
  (rdom/render root [app]))

(defn ^:dev/before-load stop []
  (stop-tick!))

(defn ^:export ^:dev/after-load init []
  (new-game)
  (start-tick!)
  (mount!))

(comment
  (let [points [[[4 3] "red"] [[4 4] "red"] [[4 5] "red"] [[5 5] "red"]]]
    (map #(second (first %)) points)))
