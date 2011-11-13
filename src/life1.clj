(ns life1)

(def cells [[0 0 0 0 0]
            [0 0 1 1 0]
            [0 1 1 0 0]
            [0 0 1 0 0]
            [0 0 0 0 0]])

(defn print-cell-rows
  "Prints cell rows (as vectors)"
  [cells]
  (doseq [row cells]
    (println row)))

(defn get-cell
  [cells i j]
  ((cells i) j))

(defn is-dead-cell?
  [cells i j]
  (= (get-cell cells i j) 0))

(def is-alive-cell? (complement is-dead-cell?))

(defn get-neighbours
  "Returns all neighbours of a cell as a list"
  [cells i j]
  (for [x (range (dec i) (inc (inc i)))
        y (range (dec j) (inc (inc j)))
        :when (and
                (>= x 0)
                (>= y 0)
                (<= x 4)
                (<= y 4)
      (or (not= i x) (not= j y)))]
    (get-cell cells x y)))

(defn sum
  [coll]
  (apply + coll))

(defn count-alive-neighbours
  [cells i j]
  (sum (get-neighbours cells i j)))

(defn should-birth?
  [cells i j]
  (= (count-alive-neighbours cells i j) 3))

(defn should-survive?
  [cells i j]
  (let [cn (count-alive-neighbours cells i j)]
    (or (= cn 2)
        (= cn 3))))

(defn transform-cell
  [cells i j]
  (if (is-alive-cell? cells i j)
    (if (should-survive? cells i j)
      1
      0)
    (if (should-birth? cells i j)
      1
      0)))

(defn transform-cells
  [cells]
  (vec
    (for [i (range 5)]
      (vec (for [j (range 5)]
        (transform-cell cells i j))))))

