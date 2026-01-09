(ns my-app.core
  (:require [benlai.core :as b]))

;; 1. State lives ONLY on the Server (JVM)
(defonce app-state (atom {:count 0
                          :name ""}))

;; 2. Event Handlers are pure Clojure functions
(defn increment [db _event]
  (update db :count inc))

(defn decrement [db _event]
  (update db :count dec))

;; Handler with parameters from user input
;; Event can be: {:name "value"} or {:value "value"} or {:0 "first", :1 "second"}
(defn set-name [db event]
  (assoc db :name (or (get event :name) 
                      (get event :value) 
                      (get event :0) 
                      "")))

;; 3. View is a projection of that state
(b/defview root-component [db]
  [:div {:style "font-family: Arial, sans-serif; max-width: 600px; margin: 50px auto; padding: 20px;"}
   [:h1 {:style "color: #333; text-align: center;"} "Server Count: " (:count db)]
   
   [:div {:style "margin-top: 30px; text-align: center;"}
    [:button {:data-handler "my-app.core/decrement"
              :style "padding: 10px 20px; font-size: 18px; margin: 0 10px; cursor: pointer; background: #f44336; color: white; border: none; border-radius: 5px;"} "-"]
    [:button {:data-handler "my-app.core/increment"
              :style "padding: 10px 20px; font-size: 18px; margin: 0 10px; cursor: pointer; background: #4CAF50; color: white; border: none; border-radius: 5px;"} "+"]]
   
   [:div {:style "margin-top: 40px; text-align: center;"}
    [:h2 {:style "color: #333;"} "Your Name: " (:name db)]
    [:div {:style "margin-top: 20px;"}
     [:input {:type "text"
              :name "name"
              :id "name-input"
              :placeholder "Enter your name"
              :style "padding: 10px; font-size: 16px; border: 1px solid #ccc; border-radius: 5px; min-width: 200px;"}]
     [:button {:data-handler ["my-app.core/set-name" "#name-input.value"]
               :style "padding: 10px 20px; font-size: 16px; margin-left: 10px; cursor: pointer; background: #2196F3; color: white; border: none; border-radius: 5px;"} "Submit"]]]])

;; 4. Start the Server
(defn -main []
  ;; Benlai handles the HTTP transport automatically
  (println "Starting Benlai server on http://localhost:8080")
  (b/start-server! {:port 8080
                    :view root-component
                    :state app-state}))

