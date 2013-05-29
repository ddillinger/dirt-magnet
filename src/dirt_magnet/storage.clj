(ns dirt-magnet.storage
  (:require [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :refer [where]]
            [clojure.java.io :as io]))


(defn db-map []
  (let [[_ user password host port database] (re-matches #"postgres://(?:(.+):(.*)@)?([^:]+)(?::(\d+))?/(.+)" (System/getenv "DATABASE_URL"))]
    {:subprotocol "postgresql"
     :subname     (str "//" host ":" port "/" database)
     :classname   "org.postgresql.Driver"
     :user        user
     :password    password}))

(defn db [] (assoc (db-map) :connection (j/get-connection (db-map))))

(def db-schema [:links
                [:id          :serial]
                [:title       :text]
                [:source      :text]
                [:url         :text]
                [:is_image    :boolean]
                [:created_at  "timestamp with time zone"]])

(defn apply-schema []
  (try
    (j/db-do-commands (db) false (apply j/create-table-ddl db-schema))
    (j/db-do-commands (db) false "CREATE UNIQUE INDEX link_id ON links (id);")
    (catch Exception e
      (println e))))

(defn insert-into-table [table data]
  (j/insert! (db) table data))

(defn update-table [table data where-data]
  (j/update! (db) table data (where where-data)))

(defn query [q]
  (j/query (db) [q]))

