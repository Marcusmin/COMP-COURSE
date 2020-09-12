-- Q5: 
create or replace view Q5a(num)
as
select count(id)
from students
where id in 
(
	select id
	from students
	where id in
		(select student
		from program_enrolments
		where program_enrolments.id in 
			(select partof
			from stream_enrolments
			where stream in
					(select id
					from streams
					where name='Chemistry')
					)and program_enrolments.semester in (select id from semesters where year=2011 and term='S1')
			)
	intersect
	select id 
	from students
	where stype='local'
)
--... SQL statements, possibly using other views/functions defined by you ...
;
-- Q5: 
create or replace view Q5b(num)
as
select count(distinct id)
from students
where id in
	(select distinct id
	from students
	where stype='intl'
	INTERSECT
	select distinct student
	from program_enrolments
	where semester=167 and program in
		(select id
		from programs
		where offeredby=(select id from orgunits where name like '%Computer Science and Engineering%')
		)
	)
--... SQL statements, possibly using other views/functions defined by you ...
;
