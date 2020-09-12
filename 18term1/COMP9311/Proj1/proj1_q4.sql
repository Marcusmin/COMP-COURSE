-- Q4:
create or replace view Q4(unswid, name)
as
select unswid,name
from courses,course_enrolments,people
where subject=(select id from subjects where code='COMP3331') and courses.id=course_enrolments.course and people.id=course_enrolments.student
except
select unswid,name
from courses,course_enrolments,people
where subject=(select id from subject where code-'COMP3231') and courses.id=course_enrolments.course and people.id=course_enrolments.student
--... SQL statements, possibly using other views/functions defined by you ...
;
