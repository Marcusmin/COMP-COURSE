-- Q10:
create or replace view S1_courses
as 
select courses.id as S1_courses,subjects.code as S1_code, subjects.name as S1_name,semesters.year as S1_year
from courses
inner join subjects on courses.subject=subjects.id
inner join semesters on courses.semester=semesters.id
where subjects.code like 'COMP93%'
and semesters.term ='S1'
and semesters.year>=2003
and semesters.year<=2012
and courses.id in (select course from course_enrolments where mark>=0)
;
create or replace view S2_courses
as 
select courses.id as S2_courses_id, subjects.code as S2_code, subjects.name as S2_name,semesters.year as S2_year
from courses
inner join subjects on courses.subject=subjects.id
inner join semesters on courses.semester=semesters.id
inner join course_enrolments on course_enrolments.course=courses.id
where subjects.code like 'COMP93%'
and semesters.term ='S2'
and semesters.year>=2003
and semesters.year<=2012
and courses.id in (select course from course_enrolments where mark>=0)
;
create or replace view code_year_S1_S2
as
select distinct S2_code as code, S1_name as name, S2_year as year, S1_courses as S1, S2_courses_id as S2
from S2_courses
inner join S1_courses on S1_code=S2_code and S1_year=S2_year and S1_name=S2_name
;

create or replace function id2rate(course_id integer)
returns numeric(4,2)
as
$$
select(
cast(
    (select(
        cast((select count(course_enrolments.student)
        from course_enrolments
        where course_enrolments.course=course_id
        and course_enrolments.mark>=85) as numeric(4,2))
        /
        cast((select count(course_enrolments.student)
        from course_enrolments
        where course_enrolments.course=course_id
        and course_enrolments.mark>=0) as numeric(4,2))
        )
        ) as numeric(4,2)
)
)
$$
language sql
;

create or replace view Q10(code, name, year, s1_HD_rate, s2_HD_rate)
as
select code as code, name as name, substring(cast(year as text),3,2) as year,(select id2rate(S1)) as S1, (select id2rate(S2)) as S2
from code_year_S1_S2
where code in (select code from code_year_S1_S2 group by code having count(code)=10)
--... SQL statements, possibly using other views/functions defined by you ...
;