(ns angular-template-type-checker.templates
  (:require [cljs.reader :as reader]
            [clojure.string :as str]
            [angular-template-type-checker.hickory :refer [flatten-hickory get-all-attrs get-all-text-content]]
            [angular-template-type-checker.typescript :refer [build-typescript]]))

(defn get-model-info [tags]
  "Gets the metadata for a template. The metadata should be stored in the first comment tag in the template, in the form of a clojure map with keys :model, :type, and :import"
  (let [comment-tag (->> tags
                         (filter #(= :comment (:type %)))
                         first)
        model-info-edn (first (:content comment-tag))]
    (when model-info-edn
      (reader/read-string model-info-edn))))

(defn template-to-typescript [hickory]
  (let [tags (flatten-hickory hickory)
        model-info (get-model-info tags)
        attrs (->> (get-all-attrs tags)
                   (filter (fn [[attr value]]
                             (str/includes? value (:model model-info)))))
        bindings (->> (get-all-text-content tags)
                      (mapcat (fn [content]
                                (-> (re-find #"\{\{(.*)\}\}" content)
                                    next)))
                      (filter identity))
        exprs (concat bindings (map second attrs))]
    (build-typescript exprs model-info)))


