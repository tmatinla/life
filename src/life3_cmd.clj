(ns life3-cmd
  (:use life3)
  (:gen-class))

; command-line UI

(defn print-help
  [_]
  (newline)
  (println "Valittavissa olevat komennot:")
  (println " h - tämä ohjeteksti")
  (println " p - tulosta ruudukko")
  (println " n - iteroi seuraava sukupolvi")
  (println " g - iteroi seuraava sukupolvi ja tulosta ruudukko")
  (println " r - palauta ruudukko alkutilanteeseen")
  (println " c - luo satunnainen uusi ruudukko")
  (println " x - luo satunnainen uusi ruudukko ja tulosta se")
  (println " q - poistu ohjelmasta"))

(defn quit
  [_]
  (println "Lopetetaan.")
  (System/exit 0))

(defn unknown-command
  [_]
  (println "Tuntematon komento!"))

(defn print-current-cells
  [current-cells]
  (print-cells @current-cells))

(defn next-gen
  [current-cells]
  (swap! current-cells transform-cells))

(defn next-gen-and-print
  [current-cells]
  (next-gen current-cells)
  (print-current-cells current-cells))

(defn restore-cells
  [current-cells]
  (reset! current-cells cells))

(defn create-random-cells
  [current-cells]
  (reset! current-cells
    (random-cells 5 5 (+ (rand-int 20) 15))))

(defn create-random-cells-and-print
  [current-cells]
  (create-random-cells current-cells)
  (print-current-cells current-cells))

(defn run-command
  [key current-cells]
  ((get {\h print-help
         \p print-current-cells
         \n next-gen
         \g next-gen-and-print
         \r restore-cells
         \c create-random-cells
         \x create-random-cells-and-print
         \q quit}
      key unknown-command)
    current-cells))

(defn get-key
  []
  (.charAt (.next (java.util.Scanner. System/in)) 0))

(defn -main
  [& args]
  (let [current-cells (atom cells)]
    (binding [*out* (.writer (System/console))]
      (println "Tervetuloa Life-pelin simulaattoriin!")
      (newline)
      (println "Ole hyvä ja syötä komento aloittaaksesi. (Paina h nähdäksesi komennot.)")
      (while true
       (newline)
       (println "Anna komento")
       (run-command (get-key) current-cells)))))

; (-main)

