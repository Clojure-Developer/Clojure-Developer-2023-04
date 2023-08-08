(ns url-shortener.frontend
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [reagent.dom.client :as rdomc]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.string :as str]))

(defonce root-el
  (rdomc/create-root (js/document.getElementById "root")))

(defonce app-state (reagent/atom {:page :main
                                  :short-url ""}))

(defn header [text]
  [:h2 {:style {:margin "8px 4px"}}
   text])

(defn short-page []
  [:<>
   [header "Your short link"]
   [:div {:style {:margin "16px 4px"}}
    [:a {:target "_blank"
         :href (:short-url @app-state)}
     (:short-url @app-state)]]
   [:button.block {:on-click (fn []
                               (reset! app-state {:short-url ""
                                                  :page :main}))}
    "BACK"]])

(defn main-page []
  (let [state (reagent/atom "")]
    (fn []
      [:<>
       [header "Shorten a long link"]
       [:label {:for "url-input"
                :style {:margin-left 4}}
        "Paste a long URL"]
       [:div {:style {:display "flex"}}
        [:input.block.fixed {:id "url-input"
                             :type "url"
                             :placeholder "Example: http://super-long-link.com/shorten-it"
                             :style {:width "80%"}
                             :value @state
                             :on-change (fn [e]
                                          (reset! state (-> e .-target .-value)))}]
        [:button.block.accent
         {:on-click (fn [_e]
                      (when-not (str/blank? @state)
                        (go (let [response (<! (http/post "/shorten" {:json-params {:url @state}}))]
                              (if (:success response)
                                (let [short-url (-> response :body :url)]
                                  (js/console.log short-url)
                                  (reset! app-state {:short-url short-url
                                                     :page :short}))
                                (js/console.log "Something went wrong"))))))}
         "SHORTEN IT"]]])))

(defn app []
  (let [page (:page @app-state)]
    [:div.card.fixed.block {:style {:margin "0 auto"
                                    :font-family "Arial, serif"
                                    :width "800px"}}
     (case page
       :main  [main-page]
       :short [short-page]
       [header "Page not found"])]))

(defn ^:dev/after-load mountit []
  (rdomc/render root-el [app]))

(defn init []
  (mountit))

