(ns asset-status
  (:require
    [clojure.java.io :refer [file]]
    [clojure.data.json :refer [read-str write-str]]
    [clojure.set :as set]
    [clj-http.client :as client]
    [clojure.data.csv :as csv]
    [clojure.string :as s])
  (:import
    (com.cobblemon.mod.common.api.pokemon PokemonSpecies)
    (com.cobblemon.mod.common.client.render.models.blockbench FloatingState)
    (com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation BedrockAnimationRepository)
    (com.cobblemon.mod.common.client.render.models.blockbench.repository VaryingModelRepository)
    (java.util Collection HashSet)
    (net.minecraft.resources ResourceLocation)))

(def headers
  [:generation :dex-no :species :land-behaviour :liquid-behaviour :air-behaviour :land-speed :land-acceleration
   :land-skill :land-jump :land-stamina :liquid-speed :liquid-acceleration :liquid-skill :liquid-jump :liquid-stamina
   :air-speed :air-acceleration :air-skill :air-jump :air-stamina])

(defn csv->map
  "Takes a series of CSV rows and returns a map of the columns with the header as the key"
  [csv-data]
  (let [data (drop 3 csv-data)]
    (map zipmap (repeat headers) data)))

(defn fetch-csv
  "Fetch and parse the riding data CSV"
  []
  (let [url "https://docs.google.com/spreadsheets/u/1/d/1YhQlnH6m-tlkVbopIg5KLV8BFaaDCgZNkx3dIAy5PZ0/export?format=csv&id=1YhQlnH6m-tlkVbopIg5KLV8BFaaDCgZNkx3dIAy5PZ0&gid=50216943"]
    (->> (client/get url)
         (:body)
         (csv/read-csv)
         (csv->map))))

(defn contains-animation? [animation coll]
  (boolean (some #(s/ends-with? % animation) coll)))

(defn get-required-animations [riding-style riding-behaviour]
  (condp = riding-style
    :air #(and (contains-animation? "air_fly" %) (contains-animation? "air_idle" %))
    :land (if (= riding-behaviour "Basic")
            #(and (contains-animation? "ground_idle" %) (contains-animation? "ground_walk" %))
            #(and (contains-animation? "ground_idle" %) (contains-animation? "ground_walk" %) (contains-animation? "ground_run" %)))
    :liquid #(or (and (contains-animation? "water_idle" %) (contains-animation? "water_swim" %))
                 (and (contains-animation? "surfacewater_idle" %) (contains-animation? "surfacewater_swim" %)))))

(defn get-species-form-name [species form]
  (if (or (nil? form) (= "Normal" (.getName form)))
    (.getName species)
    (str (.getName species) " [" (.getName form) "]")))

(def excluded-behaviours #{"Roll" "Climbing" "Burst" "Waterwalking" "Strider" "Airship" "Glider" "N/A" "Submarine"})

(defn has-riding-behaviour? [species form csv behaviour]
  (let [csv-data (first (filter #(= (:species %) (get-species-form-name species form)) csv))
        not-nil-or-na #(and (not (nil? %)) (not (set/subset? #{%} excluded-behaviours)))]
    (if (not csv-data)
      false
      (not-nil-or-na (get csv-data behaviour)))))

(defn has-riding-behaviours? [species form csv]
  (or
    (has-riding-behaviour? species form csv :air-behaviour)
    (has-riding-behaviour? species form csv :land-behaviour)
    (has-riding-behaviour? species form csv :liquid-behaviour)))

(defn needs-riding-animation? [species form riding-style csv]
  (let [csv-data (first (filter #(= (:species %) (get-species-form-name species form)) csv))
        behaviour (get csv-data (keyword (str (name riding-style) "-behaviour")))
        required-animation-fn (get-required-animations riding-style behaviour)]
    (if (or (nil? csv-data) (set/subset? #{behaviour} excluded-behaviours))
      false
      (let [state (FloatingState.)
            species-resource-id (.getResourceIdentifier species)]
        (when form (.setCurrentAspects state (set (.getAspects form))))
        (let [variation (get (.getVariations VaryingModelRepository/INSTANCE) species-resource-id)
              resolved-poser (when variation (.getResolvedPoser variation state))
              name (when resolved-poser (.getPath resolved-poser))]
          (if name
            (not (required-animation-fn (.getAnimations BedrockAnimationRepository/INSTANCE name)))
            false))))))

(defn has-seat? [species form]
  (let [state (FloatingState.)
        species-resource-id (.getResourceIdentifier species)]
    (when form
      (.setCurrentAspects state (set (.getAspects form))))
    (let [poser (.getPoser VaryingModelRepository/INSTANCE species-resource-id state)]
      (.setCurrentModel state poser)
      (.updateLocators poser nil state)
      (boolean (some #(s/starts-with? % "seat_") (keys (.getLocatorStates state)))))))

(defn has-variation? [species form]
  (let [aspects (.getAspects form)
        resolver (-> (.getVariations VaryingModelRepository/INSTANCE) (.get (.getResourceIdentifier species)))
        variations (.getVariations resolver)
        matching-variation (filter #(= (set aspects) (set (.getAspects %))) variations)]
    (not (empty? matching-variation))))

(defn implemented? [species form]
  (if (or (nil? form) (= (.getName form) "Default"))
    (.getImplemented species)
    (and (.getImplemented species) (has-variation? species form))))

(defn create-implemented-map [species form csv]
  (let [result-species (.getName species)
        result-form (if (nil? form) "Normal" (.getName form))
        implemented? (implemented? species form)
        has-seat? (has-seat? species form)]
    {:species result-species
     :form result-form
     :implemented? implemented?
     :has-riding-behaviours? (has-riding-behaviours? species form csv)
     :has-air-behaviour? (has-riding-behaviour? species form csv :air-behaviour)
     :has-land-behaviour? (has-riding-behaviour? species form csv :land-behaviour)
     :has-liquid-behaviour? (has-riding-behaviour? species form csv :liquid-behaviour)
     :has-seat? has-seat?
     :needs-air-riding-animations? (needs-riding-animation? species form :air csv)
     :needs-land-riding-animations? (needs-riding-animation? species form :land csv)
     :needs-liquid-riding-animations? (needs-riding-animation? species form :liquid csv)}))

(defn implemented-status [species csv]
  (if (empty? (.getForms species))
    [(create-implemented-map species nil csv)]
    (map #(create-implemented-map species % csv) (.getForms species))))

(defn get-engineering-data []
  (let [csv (fetch-csv)]
    (->> (map #(implemented-status % csv) (.getSpecies PokemonSpecies/INSTANCE))
         (reduce into []))))

(def results (get-engineering-data))
;
(defn general-stats []
  (let [results (get-engineering-data)
        total (count results)]
    {:total total
     :implemented? (count (filter :implemented? results))
     :has-seat? (count (filter :has-seat? results))
     :has-a-riding-behaviour? (count (filter :has-riding-behaviours? results))
     :needs-animations? (count (filter #(or (:needs-air-riding-animations? %)
                                            (:needs-land-riding-animations? %)
                                            (:needs-liquid-riding-animations? %)) results))}))

(def ordered-keys [:species :form :implemented? :has-seat? :has-riding-behaviours? :has-air-behaviour? :needs-air-riding-animations? :has-land-behaviour? :needs-land-riding-animations? :has-liquid-behaviour? :needs-liquid-riding-animations?])

(defn sort-compare [a b]
  (let [key->index (zipmap ordered-keys (range))]
    (compare (get key->index a) (get key->index b))))

(defn get-engineering-data-csv []
  (let [headers ["Species" "Form" "Implemented?" "Has Seat?" "Has Riding Behaviours?" "Has Air Behaviour?" "Needs Air Animations?" "Has Land Behaviour?" "Needs Land Animations?" "Has Liquid Behaviour?" "Needs Liquid Animations?"]]
    (->> (get-engineering-data)
         (map #(into (sorted-map-by sort-compare) %))
         (sort-by (juxt :species :form))
         (map #(s/join "," (vals %)))
         (s/join "\n")
         (str (s/join "," headers) "\n")
         (spit "output.csv"))))

(defn get-engineering-data-csv []
  (let [headers ["Species" "Form" "Implemented?" "Has Seat?" "Has Riding Behaviours?" "Has Air Behaviour?" "Needs Air Animations?" "Has Land Behaviour?" "Needs Land Animations?" "Has Liquid Behaviour?" "Needs Liquid Animations?"]]
    (->> (get-engineering-data)
         (map #(into (sorted-map-by sort-compare) %))
         (sort-by (juxt :species :form)))))

(defn ready-to-scrape? [data]
  (and (:implemented? data)
       (not (:has-seat? data))
       (:has-riding-behaviours? data)
       (or (not (:has-air-behaviour? data)) (not (:needs-air-riding-animations? data)))
       (or (not (:has-land-behaviour? data)) (not (:needs-land-riding-animations? data)))
       (or (not (:has-liquid-behaviour? data)) (not (:needs-liquid-riding-animations? data)))))

(let [headers ["Species" "Form" "Implemented?" "Has Seat?" "Has Riding Behaviours?" "Has Air Behaviour?" "Needs Air Animations?" "Has Land Behaviour?" "Needs Land Animations?" "Has Liquid Behaviour?" "Needs Liquid Animations?"]
      data (->> (get-engineering-data)
                (map #(into (sorted-map-by sort-compare) %))
                (sort-by (juxt :species :form))
                (filter ready-to-scrape?))]
  (map #(select-keys % [:species :form]) data))