(ns angular-template-type-checker.typescript
  (:require [cljs.nodejs :as node]
            [clojure.string :as str]))

(def ts-node (-> (node/require "ts-node")
                 (.register (clj->js {:project "./tsconfig.json"
                                      :compilerOptions {:allowUnreachableCode true}}))))

(defn get-compiler [filename]
  (let [compile (-> (ts-node)
                    (aget "compile"))]
    (fn [code]
      (compile code (str filename ".ts") 0))))

(defn compile [code filename]
  ((get-compiler filename) code))

(defn extract-bindings [text]
  "gets a sequence of bindings (bits in curly braces) from some text, or returns nil if there aren't any"
  (let [curly-brace-regex #"\{\{\s*(.*?)\s*\}\}"]
    (->> (re-seq curly-brace-regex text)
         (mapcat rest)
         not-empty)))

(defn get-exprs-for-attr [[attr value]]
  (let [ng-repeat-regex #"\S+\s+in\s+(.*)"]
    (->> (case attr
           :ng-repeat (rest (re-find ng-repeat-regex value)) ; remove the "x in" bit from the ng-repeat
           (or (extract-bindings value) [value]))
         (mapcat (fn [ng-expr]
                   (let [[expr & filters] (str/split ng-expr "|")]
                     (cons expr
                           (mapcat #(rest (str/split % ":")) filters))))))))

(defn build-typescript [attributes metadata]
  (let [import-statements (->> (filter :import metadata)
                               (group-by :import)
                               (map (fn [[import metadata]]
                                      (str "import {" (str/join ", " (map :type metadata)) "} from '" import "';"))))
        function-args (->> metadata
                           (map #(str (:name %) ": " (:type %)))
                           (str/join ", "))
        build-function #(str "function " (gensym "func") " (" function-args "){ return " % "; }")]
    (->> (concat import-statements
                 (->> attributes
                      (mapcat get-exprs-for-attr)
                      (map build-function)))
         (apply str))))
