(ns ragtime.repl
  "Convenience functions for running in the REPL."
  (:require [ragtime.core :as core]
            [ragtime.strategy :as strategy]))

(def migration-index
  "An atom holding a map that matches migration IDs to known migrations. This
  atom is updated automatically when the migrate or rollback functions are
  called."
  (atom {}))

(defn ^:internal ^:no-doc record-migrations [migrations]
  (swap! migration-index core/into-index migrations))

(defn ^:internal ^:no-doc wrap-verbosity [{:keys [id up down] :as migration}]
  (assoc migration
         :up   (fn [db] (println "Applying" id)     (up db))
         :down (fn [db] (println "Rolling back" id) (down db))))

(defn migrate
  "Migrate the database up to the latest migration."
  [{:keys [database migrations strategy]}]
  (let [migrations (map wrap-verbosity migrations)
        index      (record-migrations migrations)]
    (core/migrate-all database index migrations (or strategy strategy/raise-error))))

(defn rollback
  "Rollback the database one or more migrations."
  ([config]
   (rollback config 1))
  ([{:keys [database migrations]} n]
   (let [migrations (map wrap-verbosity migrations)
         index      (record-migrations migrations)]
     (core/rollback-last database index n))))
