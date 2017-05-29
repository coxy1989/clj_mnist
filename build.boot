(set-env! :resource-paths #{"web_resources/html" "web_resources/json"}
          :source-paths   #{"src/clj"  "src/cljc"   "src/cljs"}
          :dependencies   '[[org.clojure/clojure "1.7.0"]
                            [org.clojure/core.async "0.3.443"]
                            [org.clojure/data.csv "0.1.4"]
                            [org.clojure/math.numeric-tower "0.0.4"]
                            [net.mikera/core.matrix "0.59.0"]
                            [compojure "1.4.0"]
                            [pandeiro/boot-http "0.7.0"]
                            [adzerk/boot-cljs "1.7.228-2"]
                            [org.clojure/data.json "0.2.6"]
                            [rum "0.10.8"]
                            [cljs-ajax "0.6.0"]
                            [adzerk/boot-reload "0.5.1"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(require '[adzerk.boot-test :refer [test]]
         '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]])

;; "src/cljc"
;;  "src/cljs"

(deftask run-tests []
 "Run the tests"
  (merge-env! :source-paths #{"test/clj"})
  (test))

(deftask auto-test []
 "Watch and run the tests when there is a change"
  (comp
    (watch)
    (run-tests)))

(deftask train [a args ARG [str]]
  "Train a neural network"
  (require '[mnist.core :as app])
  (apply (resolve 'app/-main) args))

(deftask interactive []
   (comp (serve :handler 'dev-server.core/app          	
                :resource-root "target"                     	
                :reload true)                               	
          (watch)
	      (reload)))

(deftask webx []
   (merge-env! :source-paths #{"dev"})
   (comp (interactive)
         (cljs :source-map true)
         (target :dir #{"target"})))

