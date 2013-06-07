(ns container-ship.core
  (:require [clj-http.client :as client]
            [clojure.string :as s]))

(def ^:dynamic *docker-api-url* "http://localhost:4243")

(defn connect!
  [url]
  (alter-var-root (var *docker-api-url*) (constantly (s/replace url #"/$" ""))))

(defn containers
  "Lists containers as a lazy seq"
  [& [{:keys [all limit before after] :or {all 0 limit 10}}]]
  (let [url (str *docker-api-url* "/containers/json")
        params {"all" all "limit" limit "before" before "after" after}
        params (into {} (filter val params))]
    (:body (client/get url {:query-params params :as :json}))))

(defn create-container
  "Creates a new container"
  [command & [image options]]
  (let [url (str *docker-api-url* "/containers/create")
        req (merge options {:Cmd command :Image (or image "base")})]
    (:body (client/post url {:form-params req :content-type :json :as :json}))))

(defn container
  "Gets information about a container"
  [id]
  (:body (client/get (str *docker-api-url* "/containers/" id "/json") {:as :json})))

(defn container-fs
  "Inspect changes on a container's filesystem"
  [id]
  (:body (client/get (str *docker-api-url* "/containers/" id "/changes") {:as :json})))

(defn container-export
  "Exports the Container"
  [id]
  (:body (client/get (str *docker-api-url* "/containers/" id "/export") {:as :stream})))

(defn- start-stop
  [id command & [options]]
  (-> (str *docker-api-url* "/containers/" id "/" command) 
      (client/post {:query-params options})
      :status
      (= 204)))

(defn start-container
  "Starts a container returns true on success"
  [id]
  (start-stop id "start"))

(defn container-stop
  "Stops a container returns true on success
  
  Accepts a map of {:t <time in seconds until restart>} "
  [id & [options]]
  (start-stop id "stop" options))

(defn container-restart
  "Restarts a container and returns true on success

  Accepts a map of {:t <time in seconds until restart>}"
  [id & [options]]
  (start-stop id "restart" options))

(defn container-kill
  "Kills a container and returns true on success"
  [id]
  (start-stop id "kill"))

(defn delete-container
  "Deletes Container and returns true on success.

  Accepts a map of {:v true/false} to delete volumes
  associated with container. Default is false."
  [id & [options]]
  (-> (str *docker-api-url* "/containers/" id) 
      (client/delete {:query-params options})
      :status
      (= 204)))

(defn wait-container
  "Blocks for container to return an exit status
  Returns the status code."
  [id]
  (-> (str *docker-api-url* "/container/" id "/wait")
      (client/post {:as :json})
      (get-in [:body :StatusCode])))

(defn images
  "Returns all images"
  []
  (:body (client/get (str *docker-api-url* "/images/json") {:as :json})))

(defn image
  [id]
  (:body (client/get (str *docker-api-url* "/images/" id "/json" {:as :json}))))
