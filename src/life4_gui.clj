(ns life4-gui
  (:use life4)
  (:gen-class)
  (:import
    (javax.swing JFrame JPanel JTextArea
      JButton JMenuBar JMenu JMenuItem
      JFileChooser JSlider JLabel JSpinner
      SpinnerNumberModel)
    (java.awt GridLayout FlowLayout
      Dimension Rectangle)
    (java.awt.event ActionListener
      MouseListener)
    (java.util Hashtable)))

; not in use anymore (see life3-gui for uses)
(defn get-current-cells
  [current-cells]
  (with-out-str (print-cells @current-cells)))

(defn next-generation
  [current-cells generations]
  (swap!
    current-cells
    (fn [cells] (get-generation cells generations))))

(defn next-gen
  [current-cells hops]
  (next-generation current-cells (.getNumber hops)))

(defn create-random-cells
  [current-cells random-percentage]
  (reset!
    current-cells
    (random-cells 5 5 (.getNumber random-percentage))))

(defn restore-cells
  [current-cells]
  (reset! current-cells cells))

(defn get-delay
  [speed]
  (* 20 (.getValue speed)))

(defn simulate
  [current-cells hops speed cells-field]
  (fn []
    (while true
      (next-gen current-cells hops)
      (.repaint cells-field)
      (Thread/sleep (get-delay speed)))))

(defn start-simulation
  [current-cells hops speed cells-field]
  (let [simulation
        (Thread. (simulate current-cells hops speed cells-field))]
    (.start simulation)
    simulation))

(defn stop-simulation
  [simulation]
  (.stop @simulation)
  (reset! simulation nil))

(defn started?
  [simulation]
  (boolean @simulation))

(defn start-stop-simulation
  [current-cells hops speed cells-field simulation simulate-button]
  (if (started? simulation)
    (do
      (stop-simulation simulation)
      (.setText simulate-button "Simuloi"))
    (do
      (reset! simulation (start-simulation current-cells hops speed cells-field))
      (.setText simulate-button "Pysäytä"))))

(defn load-cells
  [frame current-cells]
  (let [fc (JFileChooser.)
        retval (.showOpenDialog fc frame)]
    (when (== retval JFileChooser/APPROVE_OPTION)
      (reset! current-cells (load-cells-from-file (.getSelectedFile fc))))))

(defn save-cells
  [frame current-cells]
  (let [fc (JFileChooser.)
        retval (.showSaveDialog fc frame)]
    (when (== retval JFileChooser/APPROVE_OPTION)
      (save-cells-to-file (.getSelectedFile fc) @current-cells))))

(defn add-menuitem
  [frame menu text action current-cells cells-field]
  (let [menuitem (JMenuItem. text)]
    (.addActionListener menuitem
      (proxy [ActionListener] []
        (actionPerformed [event]
          (action frame current-cells)
          (.repaint cells-field))))
    (.add menu menuitem)))

(defn add-button-action
  [button action current-cells cells-field & action-params]
  (.addActionListener button
    (proxy [ActionListener] []
      (actionPerformed [event]
        (apply action current-cells action-params)
        (.repaint cells-field)))))

(defn add-components
  [panel & components]
  (doseq [component components]
    (.add panel component)))

(defn mouse-xy->cells-index
  [x y]
  [(int (/ x 20)) (int (/ y 20))])

(defn add-mouse-action
  [cells-field current-cells]
  (.addMouseListener cells-field
    (proxy [MouseListener] []
      (mouseClicked [event]
        (let [[i j]
              (mouse-xy->cells-index
                (.getX event) (.getY event))]
          (swap!
            current-cells
            (fn [cells] (swap-cell cells i j))))
        (.repaint cells-field))
      (mouseEntered [event] nil)
      (mouseExited [event] nil)
      (mousePressed [event] nil)
      (mouseReleased [event] nil))))

(defn drawrect
  [g x y w h]
  (.draw g (Rectangle. x y w h)))

(defn fillrect
  [g x y w h]
  (.fill g (Rectangle. x y w h)))

(defn paint-background
  [g obj]
  (.setColor g (.getBackground obj))
  (.fillRect g 0 0 (.getWidth obj) (.getHeight obj))
  (.setColor g (.getForeground obj)))

(defn paint-cell
  [g current-cells x y]
  (let [start-x (+ (* x 20) 2)
        start-y (+ (* y 20) 2)]
    (drawrect g start-x start-y 18 18)
    (when (is-alive-cell? @current-cells x y)
      (fillrect g start-x start-y 18 18))))

(defn draw-cells
  [current-cells]
  (proxy [JPanel] []
    (getPreferredSize [] (Dimension. 110 110))
    (paintComponent [g]
      (paint-background g this)
      (drawrect g 0 0 102 102)
      (doseq [x (range 0 5) y (range 0 5)]
        (paint-cell g current-cells x y)))))

(defn create-menubar
  [frame current-cells cells-field]
  (let [menubar (JMenuBar.)
        menu (JMenu. "Tiedosto")]
    (add-menuitem frame menu "Lataa ruudukko..." load-cells current-cells cells-field)
    (add-menuitem frame menu "Tallenna ruudukko..." save-cells current-cells cells-field)
    (.add menubar menu)
    menubar))

(defn create-buttons
  [current-cells cells-field]
  (let [hops (SpinnerNumberModel. 1 1 50 1)
        random-percentage (SpinnerNumberModel. 25 0 100 1)
        simulation (atom nil)
        next-button (JButton. "Seuraava sukupolvi")
        random-button (JButton. "Satunnainen ruudukko")
        reset-button (JButton. "Palauta ruudukko")
        simulate-button (JButton. "Simuloi")
        hops-label (JLabel. "Hyppyjä:")
        hops-spinner (JSpinner. hops)
        hops-panel (JPanel. (FlowLayout. FlowLayout/LEADING))
        random-percentage-label (JLabel. "Täyttöaste %:")
        random-percentage-spinner (JSpinner. random-percentage)
        random-percentage-panel (JPanel. (FlowLayout. FlowLayout/LEADING))
        simulation-speed-label (JLabel. "Nopeus:")
        simulation-speed-label-panel (JPanel. (FlowLayout. FlowLayout/LEADING))
        simulation-speed (JSlider. 1 200)
        buttons (JPanel.)]
    (add-button-action next-button
      next-gen current-cells cells-field hops)
    (add-button-action random-button
      create-random-cells current-cells cells-field random-percentage)
    (add-button-action simulate-button
      start-stop-simulation current-cells cells-field hops simulation-speed cells-field simulation simulate-button)
    (add-button-action reset-button
      restore-cells current-cells cells-field)
    (add-mouse-action cells-field current-cells)
    (add-components hops-panel hops-label hops-spinner)
    (add-components random-percentage-panel random-percentage-label random-percentage-spinner)
    (.add simulation-speed-label-panel simulation-speed-label)
    (doto simulation-speed
      (.setInverted true)
      (.setLabelTable (Hashtable. {(int 1) (JLabel. "Nopea"),
                                   (int 200) (JLabel. "Hidas")}))
      (.setPaintLabels true))
    (doto buttons
      (.setLayout (GridLayout. 4 2 3 3))
      (add-components next-button     hops-panel
                      random-button   random-percentage-panel
                      simulate-button simulation-speed-label-panel
                      reset-button    simulation-speed))
    buttons))

(defn create-frame
  []
  (let [current-cells (atom cells)
        frame (JFrame. "Life-simulaattori")
        cells-field (draw-cells current-cells)]
    (doto frame
      (.setJMenuBar (create-menubar frame current-cells cells-field))
      (.setLayout (FlowLayout.))
      (add-components cells-field (create-buttons current-cells cells-field))
      (.setSize 535 200)
      (.setVisible true))))

(defn -main
  [& args]
  (create-frame))