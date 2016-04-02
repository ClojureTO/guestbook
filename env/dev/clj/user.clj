(ns user
  (:require [mount.core :as mount]
            [guestbook.figwheel :refer [start-fw stop-fw cljs]]
            guestbook.core))

(defn start []
  (mount/start-without #'guestbook.core/repl-server))

(defn stop []
  (mount/stop-except #'guestbook.core/repl-server))

(defn restart []
  (stop)
  (start))


