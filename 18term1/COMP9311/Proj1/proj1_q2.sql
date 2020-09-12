-- Q2: 
create or replace view Q2(unswid, name)
as
select rooms.unswid, rooms.longname
from rooms
where building=(select id from buildings where name='Computer Science Building') and rtype=(select id from Room_types where description='Meeting Room') and capacity>=20
--... SQL statements, possibly using other views/functions defined by you ...
;
