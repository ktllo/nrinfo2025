-- This SQL will return a list of train that will run today
-- Note that you MUST build the schedule cache before using this query.

select
    s.train_uid as uid,
    s.stp_indicator as stpi,
    s.operator as toc,
    s.train_category as cat,
    ot.tps_description as origin,
    s.departure_time as depart,
    dt.tps_description as destination,
    s.arrival_time as arrive,
    concat('/schedule/',s.train_uid,'/', date_format(sm.schedule_date, '%Y-%m-%d')) as url
from
    schedule_map sm
        join schedule s on sm.schedule_uuid = s.schedule_uuid
        join tiploc ot on s.origin = ot.tiploc_code
        join tiploc dt on s.destination = dt.tiploc_code
where
    sm.schedule_date = CURDATE()
order by 6,8,5,7