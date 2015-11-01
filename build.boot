(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[adzerk/boot-cljs "1.7.48-6"]])

(require '[adzerk.boot-cljs :refer [cljs]])
