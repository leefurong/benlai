(ns my-app.todo
  (:require [benlai.core :as b]
            [clojure.string :as str])
  (:import [java.util UUID]))

;; 1. State lives ONLY on the Server (JVM)
;; Each todo item has: :id, :text, :completed
;; Note: Input box state is browser-side only (temporary user input)
(defonce app-state (atom {:todos []}))

;; Helper function to generate unique IDs
(defn generate-id []
  (str (UUID/randomUUID)))

;; 2. Event Handlers are pure Clojure functions

;; Add a new todo item
(defn add-todo [db event]
  (let [text (or (get event :text)
                 (get event :value)
                 (get event :0)
                 "")]
    (if (not (str/blank? text))
      (update db :todos conj {:id (generate-id)
                               :text text
                               :completed false})
      db)))

;; Delete a todo item by ID
(defn delete-todo [db event]
  (let [id (or (get event :id)
               (get event :value)
               (get event :0))]
    (update db :todos (fn [todos]
                        (remove #(= (:id %) id) todos)))))

;; Toggle completion status of a todo
(defn toggle-todo [db event]
  (let [id (or (get event :id)
               (get event :value)
               (get event :0))]
    (update db :todos
            (fn [todos]
              (map (fn [todo]
                     (if (= (:id todo) id)
                       (update todo :completed not)
                       todo))
                   todos)))))

;; Clear all completed todos
(defn clear-completed [db _event]
  (update db :todos (fn [todos]
                      (remove :completed todos))))

;; Toggle all todos (mark all as complete/incomplete)
(defn toggle-all [db _event]
  (let [all-completed? (every? :completed (:todos db))]
    (update db :todos
            (fn [todos]
              (map #(assoc % :completed (not all-completed?)) todos)))))

;; 3. View is a projection of that state
(b/defview root-component [db]
  (let [todos (:todos db)
        active-count (count (remove :completed todos))
        completed-count (count (filter :completed todos))
        all-count (count todos)]
    [:div {:style "font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif; max-width: 600px; margin: 50px auto; padding: 20px; background: #f5f5f5; min-height: 100vh;"}
     
     ;; Header
     [:div {:style "background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px;"}
      [:h1 {:style "color: #333; text-align: center; margin: 0 0 10px 0; font-size: 36px; font-weight: 300;"} "📝 Todo List"]
      [:p {:style "color: #666; text-align: center; margin: 0; font-size: 14px;"} 
       (str active-count " active, " completed-count " completed")]]
     
     ;; Input section
     [:div {:style "background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px;"}
      [:form {:on-submit ["my-app.todo/add-todo" "#new-todo-input.value"]
              :style "display: flex; gap: 10px; align-items: center;"}
       [:input {:type "text"
                :id "new-todo-input"
                :name "new-todo"
                :placeholder "What needs to be done?"
                :style "flex: 1; padding: 12px; font-size: 16px; border: 2px solid #e0e0e0; border-radius: 4px; outline: none; transition: border-color 0.2s;"
                :on-focus "this.style.borderColor='#4CAF50'"
                :on-blur "this.style.borderColor='#e0e0e0'"}]
       [:button {:type "submit"
                 :style "padding: 12px 24px; font-size: 16px; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: 500; transition: background 0.2s;"
                 :on-mouseover "this.style.background='#45a049'"
                 :on-mouseout "this.style.background='#4CAF50'"} "Add"]]]
     
     ;; Toggle all button (if there are todos)
     (when (pos? all-count)
       [:div {:style "background: white; padding: 15px 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 10px;"}
        [:button {:on-click "my-app.todo/toggle-all"
                  :style "padding: 8px 16px; font-size: 14px; background: #2196F3; color: white; border: none; border-radius: 4px; cursor: pointer;"
                  :on-mouseover "this.style.background='#1976D2'"
                  :on-mouseout "this.style.background='#2196F3'"}
         (if (every? :completed todos) "Mark All Incomplete" "Mark All Complete")]])
     
     ;; Todo list
     [:div {:style "background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden;"}
      (if (empty? todos)
        [:div {:style "padding: 40px; text-align: center; color: #999;"}
         [:p {:style "margin: 0; font-size: 16px;"} "No todos yet. Add one above! ✨"]]
        (into []
              (map (fn [todo]
                     [:div {:style (str "padding: 15px 20px; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; gap: 12px; transition: background 0.2s; "
                                        (when (:completed todo) "opacity: 0.6;"))
                            :on-mouseover "this.style.background='#f9f9f9'"
                            :on-mouseout "this.style.background='white'"}
                      ;; Checkbox
                      [:input {:type "checkbox"
                               :checked (:completed todo)
                               :on-change ["my-app.todo/toggle-todo" (:id todo)]
                               :style "width: 20px; height: 20px; cursor: pointer;"}]
                      ;; Todo text
                      [:span {:style (str "flex: 1; font-size: 16px; color: #333; "
                                         (when (:completed todo) "text-decoration: line-through; color: #999;"))}
                       (:text todo)]
                      ;; Delete button
                      [:button {:on-click ["my-app.todo/delete-todo" (:id todo)]
                                :style "padding: 6px 12px; font-size: 14px; background: #f44336; color: white; border: none; border-radius: 4px; cursor: pointer; transition: background 0.2s;"
                                :on-mouseover "this.style.background='#da190b'"
                                :on-mouseout "this.style.background='#f44336'"} "Delete"]]))
              todos))]
     
     ;; Footer actions (if there are completed todos)
     (when (pos? completed-count)
       [:div {:style "background: white; padding: 15px 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-top: 10px; text-align: center;"}
        [:button {:on-click "my-app.todo/clear-completed"
                  :style "padding: 10px 20px; font-size: 14px; background: #ff9800; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: 500; transition: background 0.2s;"
                  :on-mouseover "this.style.background='#f57c00'"
                  :on-mouseout "this.style.background='#ff9800'"}
         (str "Clear " completed-count " completed")]])]))

;; 4. Start the Server
(defn -main []
  ;; Benlai handles the HTTP transport automatically
  (println "Starting Todo List app on http://localhost:8080")
  (b/start-server! {:port 8080
                    :view root-component
                    :state app-state}))

