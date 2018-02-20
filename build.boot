(set-env! :resource-paths #{"web_resources/html" "web_resources/json" "web_resources/css"}
          :source-paths #{"src/clj"  "src/cljc"   "src/cljs"}
          :dependencies '[[org.clojure/clojure "1.8.0"]
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

(deftask train
  "Train a neural network"
  [a args ARG [str]]
  (require '[train.core :as train])
  (apply (resolve 'train/-main) args))

(deftask interactive
  "TODO: docstring"
  []
   (comp (serve :handler 'host.core/app
                :resource-root "target"
                :reload true)
          (watch)
          (reload)))

(deftask run-web
  "TODO: docstring"
  []
   (comp (interactive)
         (cljs :source-map true)
         (target :dir #{"target"})))

(deftask build-web
  "TODO: docstring"
  []
   (merge-env! :source-paths #{"dev"})
   (comp (cljs :source-map true)
         (target :dir #{"target"})))
