(ns macros.go-loop-let.core
  (:require 
    [cljs.core.async.macros :refer [go-loop]]))

(defmacro go-loop-let [bindings expr]
  `(go-loop [] (let ~bindings ~expr)))

(defmacro go-loop-let-recur [bindings expr]
  `(go-loop [] (let ~bindings ~expr)(recur)))

