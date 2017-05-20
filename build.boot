(def project 'clj_mnist)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.7.0"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [org.clojure/math.numeric-tower "0.0.4"]
                            [net.mikera/core.matrix "0.59.0"]])

(task-options!
 aot {:namespace   #{'clj.mnist.core }}
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/clj_mnist"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 jar {:main        'clj-mnist.core
      :file        (str "clj_mnist-" version "-standalone.jar")})

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot)
          (pom)
          (uber)
          (jar)
          (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[clj.mnist.core :as app])
  (apply (resolve 'app/-main) args))

(require '[adzerk.boot-test :refer [test]])
