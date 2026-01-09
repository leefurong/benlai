(ns benlai.core
  (:require [org.httpkit.server :as http]
            [hiccup.core :as hiccup]
            [hiccup.util :as hiccup-util]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.util UUID]))

;; Simple tree diffing - compare two hiccup trees
(defn tree-diff [old-tree new-tree]
  (if (= old-tree new-tree)
    []
    (cond
      (not= (type old-tree) (type new-tree))
      [:replace new-tree]

      (vector? old-tree)
      (let [[old-tag old-attrs & old-children] old-tree
            [new-tag new-attrs & new-children] new-tree]
        (if (not= old-tag new-tag)
          [:replace new-tree]
          (let [attrs-diff (when (not= old-attrs new-attrs)
                             [:attrs new-attrs])
                children-diff (when (not= old-children new-children)
                                [:children (mapv vector
                                                 (range (max (count old-children) (count new-children)))
                                                 (concat old-children (repeat nil))
                                                 (concat new-children (repeat nil)))])]
            (filter some? [attrs-diff children-diff]))))

      :else
      [:replace new-tree])))

;; Store for component views and state
(defonce views (atom {}))
(defonce states (atom {}))
(defonce last-rendered (atom {}))

(defmacro defview
  "Define a view component that renders hiccup from state"
  [name params & body]
  `(do
     (defn ~name ~params
       ~@body)
     (swap! views assoc '~name ~name)))

(defn normalize-attrs
  "Normalize attributes: convert data-handler vectors to JSON strings"
  [attrs]
  (if (map? attrs)
    (reduce-kv (fn [m k v]
                 (if (and (= k :data-handler) (vector? v))
                   (assoc m k (json/write-str v))
                   (assoc m k v)))
               {} attrs)
    attrs))

(defn normalize-hiccup
  "Recursively normalize Hiccup tree: convert data-handler vectors to JSON"
  [hiccup]
  (cond
    (vector? hiccup)
    (let [[tag & rest] hiccup]
      (if (and (seq rest) (map? (first rest)))
        ;; Has attributes map
        (let [attrs (first rest)
              children (next rest)]
          (into [tag (normalize-attrs attrs)] (map normalize-hiccup children)))
        ;; No attributes, just children
        (into [tag] (map normalize-hiccup rest))))
    :else
    hiccup))

(defn render-view [view-fn state]
  (let [hiccup-tree (view-fn state)]
    (normalize-hiccup hiccup-tree)))

(defn get-patch [session-id new-tree]
  (let [old-tree (get @last-rendered session-id)]
    (if (nil? old-tree)
      [:full new-tree]
      (let [diff (tree-diff old-tree new-tree)]
        (if (empty? diff)
          []
          diff)))))

(defn handle-event
  "Handle event, update state, render new UI and return Hiccup (as JSON)
   event-data: map containing event parameters from client"
  [session-id event-handler state-atom view-fn event-data]
  (let [current-state @state-atom
        new-state (event-handler current-state event-data)]
    (swap! state-atom (constantly new-state))
    (swap! states assoc session-id new-state)
    (let [new-tree (render-view view-fn new-state)]
      (swap! last-rendered assoc session-id new-tree)
      ;; Return Hiccup tree as JSON - browser will convert to HTML
      new-tree)))

(defn start-server! [{:keys [port view state]}]
  (let [session-id (str (UUID/randomUUID))
        _ (swap! states assoc session-id @state)
        _ (swap! last-rendered assoc session-id (render-view view @state))]
    (http/run-server
     (fn [req]
       (let [uri (:uri req)
             method (:request-method req)]
         (cond
           (= uri "/")
           (let [html-template (slurp (io/resource "public/index.html"))
                 initial-hiccup (render-view view @state)
                 hiccup-json (json/write-str initial-hiccup)
                 escaped-json (hiccup-util/escape-html hiccup-json)
                 final-html (str/replace html-template
                                         "<div id=\"app\">Loading...</div>"
                                         (str "<div id=\"app\" data-hiccup=\"" escaped-json "\"></div>"))]
             {:status 200
              :headers {"Content-Type" "text/html"}
              :body final-html})

           (and (= uri "/api/event") (= method :post))
           (let [body (slurp (:body req))
                 request-data (json/read-str body :key-fn keyword)
                 handler-name (get request-data :handler)
                 params (get request-data :params [])
                 ;; Convert params array to event map
                 ;; If params is [val1, val2], event becomes {:0 val1, :1 val2}
                 ;; If params is [{:key val}], event becomes that map
                 ;; If single param, use :value key
                 event-data (cond
                              (empty? params) {}
                              (and (= (count params) 1) (map? (first params)))
                              (first params) ; Single map param becomes the event
                              (= (count params) 1)
                              {:value (first params)} ; Single value param
                              :else
                              (into {} (map-indexed (fn [i v] [(keyword (str i)) v]) params)))
                 handler-symbol (symbol handler-name)
                 handler (resolve handler-symbol)]
             (if handler
               (let [hiccup-tree (handle-event session-id handler state view event-data)]
                 {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body (json/write-str hiccup-tree)})
               {:status 404
                :body (str "Handler not found: " handler-name)}))

           :else
           {:status 404
            :body "Not found"})))
     {:port (or port 8080)})))

