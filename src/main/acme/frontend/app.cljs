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

(defn random-index []
  (rand-int (count messages)))

(defn assign-initial-state! []
  (swap! app-state assoc :current-block (block/create {}))
  (swap! app-state assoc :current-index (random-index)))

(defn shuffle-message! []
  (swap! app-state
         (fn [{:keys [current-index] :as state}]
           (let [new-index (random-index)]
             ;; ensure it's different
             (if (= new-index current-index)
               state
               (assoc state :current-index new-index))))))

(defn start-tick! []
  (when @tick-interval
    (js/clearInterval @tick-interval))
  (reset! tick-interval
          (js/setInterval
           (fn []
             (swap! app-state update :current-block block/move-down))
           1000)))

(defn stop-tick! []
  (when @tick-interval
    (js/clearInterval @tick-interval)
    (reset! tick-interval nil)))

(defn tetris []
  (let [{:keys [current-block]} @app-state]
    [:div.hero
     [:h3 "Tetris Block"]
     (if current-block
       [:div
        [:p (str "Shape: " (:shape current-block))]
        [:p (str "Rotation: " (:rotation current-block) "°")]
        [:p (str "Location: x=" (get-in current-block [:location :x])
                 " y=" (get-in current-block [:location :y]))]]
       [:p "No block created yet"])]))

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

