(ns guestbook.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [guestbook.db.core :as db]))

(s/defschema Thingie {:id Long
                      :hot Boolean
                      :tag (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})

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
