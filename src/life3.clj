(ns life3)

(def cells [[0 0 0 0 0]
            [0 0 1 1 0]
            [0 1 1 0 0]
            [0 0 1 0 0]
            [0 0 0 0 0]])

(def block [[0 0 0 0]
            [0 1 1 0]
            [0 1 1 0]
            [0 0 0 0]])

(defn print-cell-rows
  "Prints cell rows (as vectors)"
  [cells]
  (doseq [row cells]
    (println row)))

(defn print-cells
  [cells]
  (doseq [row cells]
    (doseq [cell row]
      (print (str cell " ")))
    (println)))

(defn empty-cells
  [x y]
  (vec
    (for [i (range y)]
      (vec
        (for [j (range x)] 0)))))

(defn set-cell-alive
  [cells index]
  (assoc-in cells index 1))

(defn create-cells
  [x y & indices]
  (reduce
    set-cell-alive
    (empty-cells x y)
    indices))

(def blinker (create-cells 5 5 [1 2] [2 2] [3 2]))

(def single (create-cells 5 5 [2 2]))

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
                (< y (count cells))
                (< x (count (cells y)))
                (or (not= i x) (not= j y)))]
    (get-cell cells x y)))

(defn sum
  [coll]
  (apply + coll))

(defn count-alive-neighbours
  [cells i j]
  (sum (get-neighbours cells i j)))

(defn make-count-rule
  [min max]
  (fn [cells i j]
    (and (>= (count-alive-neighbours cells i j) min)
         (<= (count-alive-neighbours cells i j) max))))

(def should-birth? (make-count-rule 3 3))

(def should-survive? (make-count-rule 2 3))

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
    (for [i (range (count cells))]
      (vec (for [j (range (count (cells i)))]
        (transform-cell cells i j))))))

(defn get-generation
  [cells gen]
  (first
    (drop gen (iterate transform-cells cells))))

(defn get-random-empty-cell
  [cells]
  (loop [i (rand-int (count cells))
         j (rand-int (count (cells 0)))]
    (if (is-dead-cell? cells i j)
      [i j]
      (recur (rand-int (count cells))
             (rand-int (count (cells 0)))))))

(defn random-cells*
  [cell-count cells]
  (if (= cell-count 0)
    cells
    (recur
      (dec cell-count)
      (set-cell-alive cells (get-random-empty-cell cells)))))

(defn random-cells
  [x y percent]
  (random-cells*
    (int (/ (* x y percent) 100)) (empty-cells x y)))

