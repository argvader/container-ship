(ns container-ship.core
  (:require [clj-http.client :as client]))

(def *docker-api-url* "http://localhost:4243")

(defn connect!
  [url]
  (swap! *docker-api-url* url))

(defn- )

(defn containers
  "Lists containers as a lazy seq"
  [{:keys [all limit before after] :or {limit 10}}]
  (let [results-seq (fn results-seq [& [last-item]])]
    (lazy-seq
      (let [url (str *docker-api-url* "/containers/json")])
      )
    )
  )

