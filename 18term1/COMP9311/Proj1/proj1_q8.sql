-- Q8:
create or replace view course_count_marks
as 
select course,count(mark) as count_mark
from course_enrolments
where mark is not null
group by course;

create or replace view course_average
as
select course,avg(mark) as average_mark
from course_enrolments inner join courses on course_enrolments.course=courses.id
where course in (select course from course_count_marks where count_mark>=15)
group by course;

create or replace view course_max_mark
as
select max(average_mark)
from courses
inner join course_average on courses.id=course_average.course
;
create or replace view max_mark_course
as 
select course
from course_average
where average_mark=(select * from course_max_mark);

/* create or replace view max_mark_in_subjects
as
select subject,max(average_mark) as max_average_mark
from course_average
group by subject; */

create or replace view Q8(code,name,semester)
as
select distinct subjects.code as code,subjects.name as name,semesters.name as semester
from subjects inner join courses on subjects.id=courses.subject 
inner join semesters on semesters.id=courses.semester
where courses.id=(select course from max_mark_course)
--... SQL statements, possibly using other views/functions defined by you ...
;


