-- :name add-message :! :n
insert into guestbook
(guest, message)
values (:guest, :message)

-- :name get-messages :? :*
select * from guestbook
