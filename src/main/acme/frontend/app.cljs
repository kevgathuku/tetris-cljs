(ns acme.frontend.app)

(def messages
  ["Hello, world!"
   "Atoms are simple and powerful."
   "ClojureScript runs in the browser."
   "State changes drive rendering."
   "Shadow-cljs makes this easy."])

(defonce app-state
  (atom {:current-index nil}))

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

(defn render! []
  (let [{:keys [current-index]} @app-state
        root (.getElementById js/document "app")
        message (when current-index
                  (nth messages current-index))]
    (set! (.-innerHTML root)
          (str
           "<div style='font-family: sans-serif;'>"
           "<h2>Random Message</h2>"
           "<p style='font-size: 1.2em;'>" message "</p>"
           "<button id='shuffle-btn'>Shuffle</button>"
           "</div>"))

    ;; attach button handler after render
    (when-let [btn (.getElementById js/document "shuffle-btn")]
      (.addEventListener btn "click" shuffle-message!))))

(add-watch app-state :rerender
           (fn [_ _ _ _]
             (render!)))


(defn ^:export init []
 (.appendChild (js/document.getElementById "root")
                (js/document.createTextNode "Hello, in the repl"))
 (choose-initial-message!)
  (render!)
  )

