(ns angular-template-type-checker.typescript
  (:require [cljs.nodejs :as node]
            [clojure.string :as str]))

(def ts-node (-> (node/require "ts-node")
                 (.register (clj->js {:project "./tsconfig.json"}))))

(defn get-compiler [filename]
  (let [compile (-> (ts-node)
                    (aget "compile"))]
    (fn [code]
      (compile code (str filename ".ts") 0))))

(defn compile [code filename]
  ((get-compiler filename) code))

(defn get-exprs-for-attr [[attr value]]
  (let [ng-repeat-regex #"\S+\s+in\s+(.*)"]
    (->> (case attr
           :ng-repeat (rest (re-find ng-repeat-regex value)) ; remove the "x in" bit from the ng-repeat
           [value])
         (mapcat (fn [ng-expr]
                   (let [[expr & filters] (str/split ng-expr "|")]
                     (cons expr
                           (mapcat #(rest (str/split % ":")) filters))))))))

(defn build-typescript [attributes {:keys [model type import]}]
  (let [import-statement (when import (str "import {" type "} from '" import "';"))
        build-function #(str "function " (gensym "func") " (" model ": " type "){ " % "; }")]
    (apply str
           import-statement
           (->> attributes
                (mapcat get-exprs-for-attr)
                (map build-function)))))
