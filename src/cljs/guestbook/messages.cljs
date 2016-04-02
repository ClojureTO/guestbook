(ns guestbook.messages
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [ajax.core :refer [POST]]))

(defn message-list [messages]
  [:ul
   (for [{:keys [guest message] :as id} messages]
     ^{:key id}
     [:li  message " -" guest])])

(defn input [element id label form]
  [:div.form-group
   [:label label]
   [element
    {:value (id @form)
     :on-change #(swap! form assoc id (-> % .-target .-value))}]])

(defn submit-input [form]
  (POST "/api/message"
        {:params @form
         :handler #(do
                     (session/update! :messages conj @form)
                     (swap! form empty))}))

(defn add-message []
  (let [form (r/atom {})]
    (fn []
      [:div
       [input :input.form-control :guest "name" form]
       [input :textarea.form-control :message "message" form]
       [:button.btn.btn-primary
        {:on-click #(submit-input form)}
        "Add message"]])))
