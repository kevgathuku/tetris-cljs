(ns acme.frontend.app
  (:require [reagent.core :as r] [reagent.dom.client :as rdom]))

(def messages
  ["Hello, world!"
   "Atoms are simple and powerful."
   "ClojureScript runs in the browser."
   "State changes drive rendering."
   "Shadow-cljs makes this easy."])

(defonce app-state
  (r/atom {:current-index nil}))

(defn random-index []
  (rand-int (count messages)))

(defn choose-initial-message! []
  (swap! app-state assoc :current-index (random-index)))

(defn shuffle-message! []
  (swap! app-state
         (fn [{:keys [current-index] :as state}]
           (let [new-index (random-index)]
             ;; ensure it's different
             (if (= new-index current-index)
               state
               (assoc state :current-index new-index))))))

(defn- hello-world []
  [:ul
   [:li "Hello"]
   [:li {:style {:color "red"}} "World!"]])

(defn greeting [name]
  [:p "Hello " name])

(defn app []
  (let [{:keys [current-index]} @app-state
        message (when current-index
                  (nth messages current-index))]
    [:<>
     [hello-world]
     [greeting "Champ"]
     [:div {:style {:font-family "sans-serif"}}
      [:h2 (str "Random Message: " current-index)]
      [:p {:style {:font-size "1.2em"}}
       message]
      [:button {:on-click shuffle-message!}
       "Shuffle"]]]))

(defonce root (rdom/create-root (.getElementById js/document "app")))

(defn mount! []
  (rdom/render root [app]))

(defn ^:export ^:dev/after-load init []
  (choose-initial-message!)
  (mount!))

