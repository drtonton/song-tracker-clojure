(ns song-tracker-clojure.core
  (:require [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [ring.middleware.params :as p]
            [ring.util.response :as r]
            [hiccup.core :as h])
  (:gen-class))

(defonce songs (atom []))
(defonce server (atom nil))

(add-watch songs :save-to-disk
  (fn [_ _ _ _]
    (spit "songs.edn" (pr-str @songs))))

(c/defroutes app
  (c/GET "/" request
    (h/html [:html
             [:body
              [:form {:action "/add-song" :method "post"}
               [:input {:type "text" :placeholder "Enter song" :name "song"}]
;               [:input {:type "text" :placeholder "Artist" :name "artist"}]
               [:button {:type "submit"} "Add Song"]]
              [:ol
               (map (fn [song]
                      [:li song])
                 @songs)]]]))
    (c/POST "/add-song" request
        (let [song (get (:params request) "song")]
          (swap! songs conj song)
          (r/redirect "/"))))

(defn -main []
  (try
    (let [songs-str (slurp "songs.edn")
          songs-vec (read-string songs-str)]
      (reset! songs songs-vec))
    (catch Exception _))
  (when @server
    (.stop @server))
  (reset! server (j/run-jetty (p/wrap-params app) {:port 3000 :join? false})))