(ns memefactory.ui.my-settings.page
  (:require [memefactory.ui.components.app-layout :refer [app-layout]]
            [district.ui.component.page :refer [page]]
            [reagent.core :as r]
            [district.ui.component.form.input :refer [with-label text-input]]
            [re-frame.core :as re-frame]
            [memefactory.ui.contract.district0x-emails :as ms-events]
            [goog.format.EmailAddress :as email-address]
            [reagent.ratom :refer [reaction]]
            [district.ui.graphql.subs :as gql]
            [district.ui.web3-accounts.subs :as accounts-subs]))


(defn valid-email? [s & [{:keys [:allow-empty?]}]]
  (let [valid? (email-address/isValidAddress s)]
    (if allow-empty?
      (or (empty? s) valid?)
      valid?)))

(defn my-settings []
  (let [public-key-sub (re-frame/subscribe [::gql/query
                                            {:queries [[:config
                                                        [[:ui [:public-key]]]]]}])
        active-account (re-frame/subscribe [::accounts-subs/active-account])
        form-data (r/atom {})
        errors (reaction {:local (cond-> {}
                                   (not (valid-email? (or (:email @form-data) "")))
                                   (assoc-in [:email :error] "Invalid email format"))})]
    (fn []

      (let [public-key (-> @public-key-sub :config :ui :public-key)
            settings (re-frame/subscribe [:memefactory.ui.subs/settings @active-account])]
        [:div.my-settings-box
         [:div.icon]
         [:h2.title "My Settings"]
         [:div.body
          [:div.form
           [with-label
            "Email"
            [text-input {:form-data form-data
                         :id :email
                         :errors errors
                         :dom-id :email}]
            {:form-data form-data
             :id :email
             :for :email}]
           (when (not-empty (:email @settings))
             [:div.alert "You already associated " (:email @settings) " with your Ethereum address"])
           [:p "Email associated with your address will be encrypted and stored on a public blockchain. Only our email server will be able to decrypt it. We'll use it to send you notifications about your activity."]]]
         [:div.footer

          (if (empty? (:local @errors))
            {:on-click #(re-frame/dispatch [::ms-events/save-settings public-key @form-data])}
            {})
          "Save"]]))))

(defmethod page :route.my-settings/index []
  [app-layout
   {:meta {:title "MemeFactory"
           :description "Description"}}
   [:div.my-settings-page
    [my-settings]]])
