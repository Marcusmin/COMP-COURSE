-- Q3: 
create or replace view Q3(unswid, name)
as
select unswid,name
from people 
where id in(
	select staff
	from course_staff
	where course in (
			select course
			from course_enrolments
			where student in (
					select id
					from people 
					where given='Stefan' and family='Bilek'
					)
			) 
	)
--... SQL statements, possibly using other views/functions defined by you ...
;
