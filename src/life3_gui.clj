(ns life3-gui
  (:use life3)
  (:gen-class)
  (:import (javax.swing JFrame JPanel JTextArea JButton)
    (java.awt.event ActionListener)
    (java.awt GridLayout FlowLayout)))

(defn get-current-cells
  [current-cells]
  (with-out-str (print-cells @current-cells)))

(defn next-gen
  [current-cells]
  (swap! current-cells transform-cells))

(defn create-random-cells
  [current-cells]
  (reset! current-cells
    (random-cells 5 5 (+ (rand-int 20) 15))))

(defn restore-cells
  [current-cells]
  (reset! current-cells cells))

(defn add-button-action
  [button action current-cells cells-field]
  (.addActionListener button
    (proxy [ActionListener] []
      (actionPerformed [event]
        (action current-cells)
        (.setText cells-field (get-current-cells current-cells))))))

(defn -main
  [& args]
  (let [current-cells (atom cells)
        frame (JFrame. "Life-simulaattori")
        cells-field (JTextArea. (get-current-cells current-cells))
        next-button (JButton. "Seuraava sukupolvi")
        random-button (JButton. "Satunnainen ruudukko")
        reset-button (JButton. "Palauta ruudukko")
        buttons (JPanel.)]
    (add-button-action next-button next-gen current-cells cells-field)
    (add-button-action random-button create-random-cells current-cells cells-field)
    (add-button-action reset-button restore-cells current-cells cells-field)
    (doto cells-field
      (.setEditable false)
      (.setCursor nil)
      (.setOpaque false))
    (doto buttons
      (.setLayout (GridLayout. 3 1 3 3))
      (.add next-button)
      (.add random-button)
      (.add reset-button))
    (doto frame
      (.setLayout (FlowLayout.))
      (.add cells-field)
      (.add buttons)
      (.setSize 240 130)
      (.setVisible true))))
