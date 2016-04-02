### Requirements

* JDK 8
* Leiningen
* Light Table with the Clojure plugin version 0.3.2


### Create a new Guestbook project

    lein new luminus myapp +cljs +sqlite +swagger

### Setup the database

Create the migration files:

    lein migratus create guestbook-table
    
Add a migration script to create the guestbook table for storing messages:

    create table guestbook
    (message varchar(30),
     guest varchar(500))

    
Create queries to add and select messages:

    -- :name add-message :! :n
    insert into guestbook
    (guest, message)
    values (:guest, :message)

    -- :name get-messages :? :*
    select * from guestbook

### Test the queries

Navigate to the `user` namespace and run `(start)` to initialize the app.

Navigate to `guestbook.db.core` and start the database component with `(mount.core/start #'*db*)`.

Run `bind-connecion` and test the queries:

    (add-message {:guest "Bob" :message "Hello"})
    (get-messages)

### Create services

Navigate to `guestbook.routes.services` and add a reference to `[guestbook.db.core :as db]`.

Add routes for listing and storing the messages:

```
(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}
  (context "/api" []
    :tags ["guestbook"]

    (GET "/messages" []
         :return [{:guest String :message String}]
         :summary "list available messages"
         (ok (db/get-messages)))

    (POST "/message" []
          :body-params [guest :- String message :- String]
          :return {:status s/Keyword}
          :summary "add a new message"
          (try
            (db/add-message {:guest guest :message message})
            (ok {:status :ok})
            (catch Exception e
              (internal-server-error {:status :error}))))))
```

### Making the UI 

Load the messages from the server when the app starts:

```
(defn init! []
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components)
  (GET "/api/messages"
       {:handler #(session/put! :messages %)}))
```

Render the messages in the session:

```
(defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "Welcome to Guestbook"]
     [:p (str (session/get :messages))]]]])
```

Create a new namespace called `guesbook.messages`:


```clojure
(ns guestbook.messages
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [ajax.core :refer [POST]]))
```

Create a `message-list` component:

```
(defn message-list [messages]
  [:ul
     (for [{:keys [guest message] :as id} messages]
       ^{:key id}
       [:li  message " -" guest])])
```

Reference it in the `guestbook.core`:

```
(ns guestbook.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            ...
            [guestbook.messages :refer [message-list]])
  (:import goog.History))
  ```
  
  Update the `home-page` to list the messages:
  
  ```
  (defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "Welcome to Guestbook"]     
     [message-list (session/get :messages)]]]])
```

Write a component for adding a new message:

```
(defn add-message []
  (let [form (r/atom {})]
    (fn []
      [:div.form-group
       [:label "name"]
       [:input.form-control
        {:value (:guest @form)
         :on-change #(swap! form assoc :guest (-> % .-target .-value))}]])))
```

Reference the component in the `guestbook.core` namespace:


```
(ns guestbook.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            ...
            [guestbook.messages
             :refer [message-list add-message]])
  (:import goog.History))
  ```
  
  Update the `home-page` with the new component:
  
  ```
(defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "Welcome to Guestbook"]
     [message-list (session/get :messages)]]]
   [:div.row
    [:div.col-md-12
     [:hr]
     [add-message]]]])
```

Factor out the input component:

```
(defn input [element id label form]
  [:div.form-group
   [:label label]
   [element
    {:value (id @form)
     :on-change #(swap! form assoc id (-> % .-target .-value))}]])

(defn add-message []
  (let [form (r/atom {})]
    (fn []
      [:div
       [input :input.form-control :guest "name" form]
       [input :textarea.form-control :message "message" form]])))
```      

Add the submit button: 

```
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
```        


