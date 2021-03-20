(ns user
  (:require [wing.repl :as repl]
            [clojure.java.io :as io]))

(comment
  (repl/sync-libs!)
  (io/resource "config.edn")
  )
