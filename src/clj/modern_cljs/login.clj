(ns modern-cljs.login)

(def ^:dynamic *re-email*
  #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$")

(def ^:dynamic *re-password*
  #"^(?=.*\d).{4,8}$")

(declare validate-email validate-password)

(defn authenticate-user [email password]
  (if (or (empty? email) (empty? password))
    (str "Please complete the form")
    (if (and (validate-email email)
             (validate-password password))
      (str email " and " password
           " passed the formal validation, but you still have to be authenticated"))))

(defn validate-email [email]
  (if  (re-matches *re-email* email)
    true))

(defn validate-password [password]
  (if  (re-matches *re-password* password)
    true))
