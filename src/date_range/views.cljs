(ns date-range.views
  (:require-macros
   [re-com.core          :refer [handler-fn at reflect-current-component]])
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [re-com.box :refer [line border]]
   [re-com.core :as re-com :refer [at v-box h-box title p box gap button]]
   [re-com.validate :refer [date-like? css-style? html-attr? parts?] :refer-macros [validate-args-macro]]
   [date-range.styles :as styles]
   [date-range.events :as events]
   [date-range.routes :as routes]
   [date-range.subs :as subs]
   [re-com.util          :refer [deref-or-value now->utc]]
   [cljs-time.format     :refer [parse unparse formatters formatter]]
   [re-com.validate :as validate]
   [cljs-time.core :as cljs-time]
   [goog.string :as gstring :refer [format]]))

(defn dec-month  [date-time] (cljs-time/minus date-time (cljs-time/months 1)))
(defn plus-month [date-time] (cljs-time/plus date-time (cljs-time/months 1)))
(defn dec-day    [date-time] (cljs-time/minus date-time (cljs-time/days 1)))
(defn plus-day   [date-time] (cljs-time/plus date-time (cljs-time/days 1)))
(defn dec-year   [date-time] (cljs-time/minus date-time (cljs-time/years 1)))
(defn plus-year  [date-time] (cljs-time/plus date-time (cljs-time/years 1)))

(defn prev-month-icon []
  [:svg
   {:height "24"
    :viewBox "0 0 24 24"
    :width "24"}
   [:path {:d "M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12l4.58-4.59z"}]])

(defn prev-year-icon []
  [:svg
   {:height "24"
    :viewBox "0 0 24 24"
    :width "24"}
   [:g
    [:path {:d "m 16.793529,7.4382353 -1.41,-1.41 -5.9999996,5.9999997 5.9999996,6 1.41,-1.41 -4.58,-4.59 z"}]
    [:path {:d "m 10.862647,7.4429412 -1.4100003,-1.41 -6,5.9999998 6,6 1.4100003,-1.41 -4.5800003,-4.59 z"}]]])

(defn next-month-icon []
  [:svg
   {:height "24"
    :viewBox "0 0 24 24"
    :width "24"}
   [:path {:d "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6-6-6z"}]])

(defn next-year-icon []
  [:svg
   {:height "24"
    :viewBox "0 0 24 24"
    :width "24"}
   [:g {:transform "translate(-1.5)"}
    [:path {:d "m 8.5882353,6 -1.41,1.41 4.5799997,4.59 -4.5799997,4.59 1.41,1.41 5.9999997,-6 z"}]
    [:path {:d "m 14.547353,5.9623529 -1.41,1.41 4.58,4.5900001 -4.58,4.59 1.41,1.41 6,-6 z"}]]])


;;class/parts/otherstuff needs to be added
(defn prev-year-nav [current-month-atom]
  (let [prev-year (dec-year (deref-or-value current-month-atom))]
    [box :src (at)
     :class "nav-button"
     :attr {:on-click #(reset! current-month-atom prev-year)}
     :child [prev-year-icon]]))

(defn prev-month-nav [current-month-atom]
  (let [prev-month (dec-month (deref-or-value current-month-atom))]
    [box :src (at)
     :class "nav-button"
     :attr {:on-click #(reset! current-month-atom prev-month)}
     :child [prev-month-icon]]))

(defn next-year-nav [current-month-atom]
  (let [next-year (plus-year (deref-or-value current-month-atom))]
    [box :src (at)
     :class "nav-button"
     :attr {:on-click #(reset! current-month-atom next-year)}
     :child [next-year-icon]]))

(defn next-month-nav [current-month-atom]
  (let [next-month (plus-month (deref-or-value current-month-atom))]
    [box :src (at)
     :class "nav-button"
     :attr {:on-click #(reset! current-month-atom next-month)}
     :child [next-month-icon]]))

(defn prev-nav [current-month-atom parts]
  [h-box :src (at)
   :width "210px"
   :justify :end
   :class (get-in parts [:prev-nav :class])
   :style (get-in parts [:prev-nav :style])
   :children [[prev-year-nav current-month-atom]
              [line]
              [prev-month-nav current-month-atom]
              [h-box
               :size "auto"
               :justify :center
               :children [[box
                           :src (at)
                           :style {:font-weight "700" :font-size 17}
                           :child (str (unparse (formatter "MMMM") (deref-or-value current-month-atom)))]]]
              [box
               :align-self :end
               :justify :end
               :style {:font-weight "630"}
               :width "49px"
               :child (str (unparse (formatter "YYYY") (deref-or-value current-month-atom)))]]])

(defn next-nav [current-month-atom]
  [h-box :src (at)
   :width "210px"
   :justify :end
   :children [[box
               :align-self :end
               :justify :start
               :style {:font-weight "630"}
               :width "49px"
               :child (str (unparse (formatter "YYYY") (plus-month (deref-or-value current-month-atom))))]
              [h-box
               :size "auto"
               :justify :center
               :children [[box
                           :src (at)
                           :style {:font-weight "700" :font-size 17}
                           :child (str (unparse (formatter "MMMM") (plus-month (deref-or-value current-month-atom))))]]]
              [next-month-nav current-month-atom]
              [line]
              [next-year-nav current-month-atom]]])

(defn main-div-with 
  "Main container to pass class, style and attributes"
  [hbox hide-border? class style attr] ;missing parts for now
  [h-box
   :children [[border
               :src (at)
               ;:class parts stuff
               ;style
               ;attr
               :radius "5px"
               :size "none"
               :border (when hide-border? "none")
               :child [:div
                       (merge {:class class
                               :style (merge {:font-size "13px"
                                              :position "static"}
                                             style)}
                              attr)
                       hbox]]]])

(defn date-disabled? 
  "Checks various things to see if a date had been disabled."
  [date [minimum maximum disabled? selectable-fn]]
  (let [too-early? (when minimum (cljs-time/before? date (deref-or-value minimum)))
        too-late? (when maximum (cljs-time/after? date (deref-or-value maximum)))
        de-selected? (when selectable-fn (not (selectable-fn date)))]
    (or too-early? too-late? de-selected? disabled?)))

(defn create-interval 
  "inclusively creates a vector of date-formats from start to end."
  [start end]
  (let [first (deref-or-value start)
        last (deref-or-value end)]
    (loop [cur first result []]
      (if (cljs-time/after? cur last)
        result
        (recur (plus-day cur) (conj result cur))))))

(defn interval-valid? 
  "Returns true if all days are NOT disabled in some way."
  [start end disabled-data]
  (let [interval (create-interval start end)]
    (->> interval
         (map #(date-disabled? % disabled-data))
         (some identity)
         not)))

(defn td-click-handler
  "Depending on the stage of the selection and if the new selected date is before the old start date, do different things"
  [day [fsm start-date end-date] on-change check-interval? disabled-data]
  (if
   (and (= @fsm "pick-end")                                                                   ;if we're picking and end date
        (cljs-time/before? @start-date day)
        (if check-interval? (interval-valid? start-date day disabled-data) true))
    (do
      (reset! fsm "pick-start")
      (reset! end-date day)                          ;update the internal end-date value
      (on-change {:start @start-date :end day}))     ;run the on-change function

    (do                               ;if we're picking a start date
      (reset! start-date day)
      (reset! end-date day)           ;set the end-date to the same date for view reasons
      (reset! fsm "pick-end"))))      ;we are next picking an end date

(defn class-for-td
  "Given a date, and the values in the internal model, determine which css class the :td should have"
  [day start-date end-date temp-end disabled? selectable-fn minimum maximum show-today?]
  (cond
    (and (not= day "") (cljs-time/before? day @end-date) (cljs-time/after? day @start-date)) "interval-date"
    (when minimum (cljs-time/before? day (deref-or-value minimum))) "disabled-td"
    (when maximum (cljs-time/after? day (deref-or-value maximum))) "disabled-td"
    disabled? "disabled-td"
    (when selectable-fn (not (selectable-fn day))) "disabled-td"
    (cljs-time/equal? day @start-date) "selected-date"
    (cljs-time/equal? day @end-date) "selected-date"
    (and (not= day "") (cljs-time/equal? @end-date @start-date) (cljs-time/before? day (plus-day @temp-end)) (cljs-time/after? day @start-date)) "temp-date" ;changed to fix flashing
    (and show-today? (cljs-time/equal? day (now->utc))) "today"
    :default "default-class"))

(defn create-day-td
  "Create table data elements with reactive classes and on click/hover handlers"
  [day [fsm start-date end-date temp-end] {:keys [on-change disabled? selectable-fn minimum maximum show-today? check-interval?] :as args}]
  (let [disabled-data (vector minimum maximum disabled? selectable-fn)]
    (if (= day "") [:td ""]
        (let [correct-class (class-for-td day start-date end-date temp-end disabled? selectable-fn minimum maximum show-today?)
              clickable? (not (date-disabled? day disabled-data))]
          (into [:td]
                (vector {:style {:padding "5px" :color "#2c2d2eb0" :width "30px"}
                         :class correct-class
                         :on-click #(when clickable? (td-click-handler day [fsm start-date end-date] on-change check-interval? disabled-data))
                         :on-mouse-over #(reset! temp-end day)}
                        (str (cljs-time/day day))))))))

(defn create-week-tr
  "Given a list of days, create a table row with each :td referring to a different day"
  [days-list atoms args]
  (into [:tr]
        (for [day days-list]
          [create-day-td day atoms args])))

(defn parse-date-from-ints
  "Given 3 ints, parse them as a useable date-format e.g. 11 2 2021"
  [d m y]
  (parse (formatter "ddMMYYYY") (str (format "%02d" d) (format "%02d" m) (str y))))

(defn empty-days-count
  "Returns the number of empty date tiles at the start of the month based on the first day of the month and the chosen week start day, monday = 1 sunday = 7"
  [chosen start]
  (let [chosen (if chosen chosen 1)] ;default week start of monday
    (if (> chosen start)
      (- 7 (- chosen start))
      (- start chosen))))

(defn days-for-month
  "Produces a partitioned list of date-formats with all the days in the given month, with leading empty strings to align with the days of the week"
  [date-from-month start-of-week]
  (let [month (cljs-time/month date-from-month)
        year (cljs-time/year date-from-month)
        last-day-of-month (cljs-time/day (cljs-time/last-day-of-the-month date-from-month))
        first-day-val (cljs-time/day-of-week (cljs-time/first-day-of-the-month date-from-month)) ;; 1 = mon  7 = sun
        day-ints (range 1 (inc last-day-of-month)) ;; e.g. (1 2 3 ... 31)
        days (map #(parse-date-from-ints % month year) day-ints) ;; turn into real date-times
        with-lead-emptys (flatten (cons (repeat (empty-days-count start-of-week first-day-val) "") days))] ;; for padding the table
    (partition-all 7 with-lead-emptys))) ;; split into lists of 7 to be passed to create-week-tr

(def days-vec [[:td "M"] [:td "Tu"] [:td "W"] [:td "Th"] [:td "F"] [:td "Sa"] [:td "Su"]]) ;for cycling and display depending on start-of-week

(defn create-table
  "Given the result from days-for-month for a given month, create the :tbody using the relevant :tr and :td functions above"
  [date atoms {:keys [start-of-week] :as args}]
  (let [partitioned-days (days-for-month date start-of-week)
        date-rows (for [x partitioned-days]
                    [create-week-tr x atoms args])
        table-row-weekdays (into [:tr] (take 7 (drop (dec start-of-week) (cycle days-vec))))
        with-weekdays-row (into [:tbody table-row-weekdays])
        with-dates (into with-weekdays-row date-rows)]
    [:table
     with-dates]))

(defn model-changed?
  "takes two date ranges and checks if they are different"
  [old latest]
  (not (and
        (cljs-time/equal? (:start old) (:start latest))
        (cljs-time/equal? (:end old) (:end latest)))))

(defn model? [{:keys [start end]}]
  (and (date-like? start) (date-like? end)))

;for validation and demo
(def daterange-parts-desc
  [{:name :wrapper               :level 0  :class ""   :impl "[date-range]"  :notes "Outer wrapper of the date-range picker."}
   {:name :border                :level 1  :class ""   :impl "[border]"      :notes "The border."}
   {:type :legacy                :level 2  :class ""   :impl "[:div]"        :notes "The div container"}
   {:name :horizontal-container  :level 3  :class ""   :impl "[h-box]"       :notes "To display hozitonally."}

   {:name :left-container        :level 4  :class ""   :impl "[v-box]"       :notes "To contain the left side of the display."}
   {:name :prev-nav              :level 5  :class ""   :impl "[h-box]"       :notes "Contains navigation buttons and month/year."}
   {:name :prev-year             :level 6  :class ""   :impl "[box]"         :notes "Previous year button."}
   {:name :prev-year-icon        :level 7  :class ""   :impl "[:svg]"        :notes "Previous year icon."}
   {:name :prev-month            :level 6  :class ""   :impl "[box]"         :notes "Previous month button."}
   {:name :prev-month-icon       :level 7  :class ""   :impl "[:svg]"        :notes "Previous month icon."}
   {:name :left-month-title      :level 6  :class ""   :impl "[box]"         :notes "Month title for the left side."}
   {:name :left-year-title       :level 6  :class ""   :impl "[box]"         :notes "Year title for the right side."}
   {:name :left-table            :level 5  :class ""   :impl "[:table]"      :notes "Left table."}
   {:type :legacy                :level 6  :class ""   :impl "[:tr]"         :notes "Row containing day titles."}
   {:name :day                   :level 7  :class ""   :impl "[:td]"         :notes "Monday."} ;... uh only here to show users they can edit the days seperately?
   {:name :day                   :level 7  :class ""   :impl "[:td]"         :notes "Tuesday."}
   {:name :day                   :level 7  :class ""   :impl "[:td]"         :notes "Wednesday."}
   {:name :day                   :level 7  :class ""   :impl "[:td]"         :notes "Thursday."}
   {:name :day                   :level 7  :class ""   :impl "[:td]"         :notes "Friday."}
   {:name :day                   :level 7  :class ""   :impl "[:td]"         :notes "Saturday."}
   {:name :day                   :level 7  :class ""   :impl "[:td]"         :notes "Sunday."}

   {:name :right-container       :level 4  :class ""   :impl "[v-box]"       :notes "To contain the right side of the display."}
   {:name :next-nav              :level 5  :class ""   :impl "[h-box]"       :notes "Contains navigation buttons and month/year."}])
;consider also, genereic parts for:::  both titles, both years, all buttons, both tables, all dates 

(def daterange-parts
  (set (map :name daterange-parts-desc)))

(def daterange-args-desc
  "used to validate the arguments supplied by the user"
  [{:name :model              :required true                    :type "A map with with :start and :end values that satisfy DateTimeProtocol | r/atom"   :validate-fn model?}
   {:name :on-change          :required true                    :type "satisfies DateTimeProtocol -> nil"                                               :validate-fn fn?}
   {:name :disabled?          :required false  :default false   :type "boolean | atom"}
   {:name :selectable-fn      :required false                   :type "function"                                                                        :validate-fn fn?}
   {:name :show-today?        :required false  :default false   :type "boolean"}
   {:name :minimum            :required false                   :type "satisfies DateTimeProtocol | r/atom"                                             :validate-fn date-like?}
   {:name :maximum            :required false                   :type "satisfies DateTimeProtocol | r/atom"                                             :validate-fn date-like?}
   {:name :check-interval?    :required false  :default false   :type "boolean"}
   {:name :start-of-week      :required false  :default 1       :type "int"                                                                             :validate-fn int?}
   {:name :hide-border?       :required false                   :type "boolean"}
   {:name :class              :required false                   :type "string"                                                                          :validate-fn string?}
   {:name :style              :required false                   :type "CSS style map"                                                                   :validate-fn css-style?}
   {:name :attr               :required false                   :type "HTML attribute map"                                                              :validate-fn html-attr?}
   {:name :parts              :required false                   :type "map"                                                                             :validate-fn (parts? daterange-parts)}])

(defn date-range
  "Tracks the external model, but takes inputs into an internal model. The given on-change function is only called after a full selection has been made"
  [& {:keys [model] :as args}]
  (or
   (validate-args-macro daterange-args-desc args)
   (let [current-month (r/atom (now->utc))
         fsm (r/atom "pick-start")
         start-date (r/atom (:start (deref-or-value model)))
         end-date (r/atom (:end (deref-or-value model)))
         temp-end (r/atom (now->utc))] ;for :on-hover css functionality
     (fn render-fn
       [& {:keys [model hide-border? class style attr parts] :as args}]
       (or
        (validate-args-macro daterange-args-desc args) ;re validate args each time they change
        (let [latest-external-model (deref-or-value model)
              internal-model-refernce {:start @start-date :end @end-date}]
          (when (and (model-changed? latest-external-model internal-model-refernce) (= @fsm "pick-start"))
            (reset! start-date (:start latest-external-model))
            (reset! end-date (:end latest-external-model)))
          [main-div-with
           [h-box :src (at)
            :gap "60px"
            :padding "15px"
            ;:height "267px"
            :children [[v-box :src (at)
                        :align :center
                        :gap "15px"
                        ;:width "210px"
                        :children [[prev-nav current-month parts]
                                   [create-table @current-month [fsm start-date end-date temp-end] args]]]
                       [v-box :src (at)
                        :align :center
                        :gap "15px"
                        :children [[next-nav current-month]
                                   [create-table (plus-month @current-month) [fsm start-date end-date temp-end] args]]]]]
           hide-border?
           class
           style
           attr]))))))

(def test-model (r/atom {:start (cljs-time/plus (now->utc) (cljs-time/days 5)) :end (cljs-time/plus (now->utc) (cljs-time/days 10))}))

(defn home-panel []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :align :center
   :children [[date-range
               :model test-model
               :on-change #(reset! test-model %)
               :start-of-week 1
               :disabled? false
               :selectable-fn #(not= (mod (cljs-time/day %) 18) 0)
               ;:minimum (parse-date-from-ints 11 7 2021)
               ;:maximum (parse-date-from-ints 20 9 2021)
               :show-today? true
               :style {:background-color "white"}
               :hide-border? false
               ;:check-interval? true
               ;:parts {:prev-nav {:style {:background-color "lightblue"}}}
               ]]])


(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-title []
  [re-com/title
   :src   (at)
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink
   :src      (at)
   :label    "go to Home Page"
   :on-click #(re-frame/dispatch [::events/navigate :home])])

(defn about-panel []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :children [[about-title]
              [link-to-home-page]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :src      (at)
     :height   "100%"
     :children [(routes/panels @active-panel)]]))
