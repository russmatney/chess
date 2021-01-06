(ns chess.core
  (:require
   [clojure.string :as string]
   [clj-http.client :as client]))

(defonce lichess-username (atom nil))
(defonce lichess-token (atom nil))

(comment
  (reset! lichess-username "russmatney")
  (reset! lichess-token "") ;; paste and set token here
  )

(def lichess-api-base "https://lichess.org/api")
(def lichess-api-account (str lichess-api-base "/account"))
(def lichess-api-account-playing (str lichess-api-account "/playing"))
(def lichess-api-user (str lichess-api-base "/user"))
(def lichess-api-user-activity (str lichess-api-user "/" @lichess-username
                                    "/activity"))
(def lichess-api-puzzle-activity (str lichess-api-user
                                      "/puzzle-activity?max=5"))
(def lichess-api-games-user (str lichess-api-base "/games/user/"
                                 @lichess-username
                                 "?max=3&pgnInJson=true&opening=true"))

(comment
  (def --acct
    (client/get
      lichess-api-account
      {:headers {:authorization (str "Bearer " @lichess-token)}
       :as      :json}))

  ;; (def --puzzle-activity
  ;;   (client/get
  ;;     lichess-api-puzzle-activity
  ;;     {:headers {:authorization (str "Bearer " @lichess-token)}
  ;;      :as      :json}))

  (def --account-current-games
    (client/get lichess-api-account-playing
                {:headers {:authorization (str "Bearer " @lichess-token)}
                 :as      :json}))

  (def --user-games
    (client/get lichess-api-games-user
                {:headers
                 {:accept        "application/json"
                  :authorization (str "Bearer " @lichess-token)}}))

  (def --user-activity-json
    (client/get lichess-api-user-activity
                {:headers {:authorization (str "Bearer " @lichess-token)}
                 :as      :json})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Lichess Import
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def lichess-api-import (str lichess-api-base "/import"))

(comment

  (def bulk-pgns
    (-> "/home/russ/Downloads/chess_com_games_2021-01-06.pgn"
        slurp
        (string/split #"\n\n")
        (->>
          (partition 2 2)
          (map (partial string/join "\n\n")))))

  (count bulk-pgns)
  (->> bulk-pgns
       (map (comp first string/split-lines))
       (into #{})
       )

  (def some-pgn
    (-> bulk-pgns first))

  (client/post
    lichess-api-import
    {:headers     {:authorization (str "Bearer " @lichess-token)
                   :accept        "application/x-www-form-urlencoded"}
     :form-params {:pgn some-pgn}})

  ;; rate limit is 200/hour with oauth!
  (doall
    (->> bulk-pgns
         (map
           (fn [pgn]
             (client/post
               lichess-api-import
               {:headers     {:authorization (str "Bearer " @lichess-token)
                              :accept        "application/x-www-form-urlencoded"}
                :form-params {:pgn pgn}})))))

  (def res *1)

  (->> res
       (map :status)
       (into #{})
       )


  )
