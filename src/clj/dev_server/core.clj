(ns dev-server.core 
  (:require 
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found files resources]]
            [compojure.handler :refer [site]]
            [ring.util.response :refer [resource-response]]))

(defroutes handler
  (GET "/" [] (resource-response "index.html" {:root "target"})) 
  (files "/" {:root "target"})          
  (resources "/" {:root "target"})      
  (not-found "This is NOT the webpage you are looking for"))

(def app
  (-> handler
      (site)))

