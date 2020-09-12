-- COMP9311 18s1 Project 1
--
-- MyMyUNSW Solution Template


-- Q1: 
create or replace view Q1(unswid, name)
as
select people.unswid as unswid,people.name as name
from people
where people.id in (
    select distinct id
    from students
    where stype='intl' and id in(
                            select distinct student
                            from course_enrolments
                            where mark>=85
                            group by student
                            having count(course)>20
                                )
)
--... SQL statements, possibly using other views/functions defined by you ...
;



-- Q2: 
create or replace view Q2(unswid, name)
as
select rooms.unswid, rooms.longname
from rooms
where building=(select id from buildings where name='Computer Science Building') and rtype=(select id from Room_types where description='Meeting Room') and capacity>=20
--... SQL statements, possibly using other views/functions defined by you ...
;



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




-- Q4:
create or replace view Q4(unswid, name)
as
select unswid,name
from courses,course_enrolments,people
where subject=(select id from subjects where code='COMP3331') and courses.id=course_enrolments.course and people.id=course_enrolments.student
except
select unswid,name
from courses,course_enrolments,people
where subject=(select id from subjects where code='COMP3231') and courses.id=course_enrolments.course and people.id=course_enrolments.student
--... SQL statements, possibly using other views/functions defined by you ...
;




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
	where semester=(select id from semesters where year=2011 and term='S1')
    and program in
		(select id
		from programs
		where offeredby=(select id from orgunits where name like '%Computer Science and Engineering%')
		)
	)
--... SQL statements, possibly using other views/functions defined by you ...
;



-- Q6:
create or replace function
	Q6(course_code char(8)) 
	returns text
as
$$
		select code||' '||name||' '||uoc
		from subjects
		where subjects.code=course_code;
--... SQL statements, possibly using other views/functions defined by you ...
$$ language sql;


-- Q7: 
create or replace view Q7(code, name)
as
select code,name
from programs
where id in
           (select program 
           from(
                select program,
                       (cast((select count(students.id) 
                              from program_enrolments 
                              inner join students on program_enrolments.student=students.id 
                              where students.stype='intl' 
                              and 
                              program=pe.program) as float))*100
                        /
                        (cast((select count(students.id) 
                               from program_enrolments 
                               inner join students on program_enrolments.student=students.id 
                               where program=pe.program)as float))
                               as percentage 
                               from program_enrolments as pe  
            group by pe.program) 
            as foo where percentage>50.0
            )

--... SQL statements, possibly using other views/functions defined by you ...
;




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






-- Q9:
create or replace view count_subject
as
select staff,count(distinct subjects.code) as  count_subject
from courses
inner join course_staff on courses.id=course_staff.course
inner join subjects on subjects.id=courses.subject
group by course_staff.staff
;

create or replace view Q9(name, school, email, starting, num_subjects)
as
select distinct people.name as name, OrgUnits.longname as school, people.email as email, Affiliations.starting as starting,count_subject as num_subjects
from Affiliations 
inner join people on people.id=Affiliations.staff
inner join OrgUnits on OrgUnits.id=Affiliations.OrgUnit
inner join count_subject on count_subject.staff=Affiliations.staff
where 
Affiliations.role=(select id from staff_roles where name='Head of School') 
and OrgUnits.utype=(select id from OrgUnit_types where name='School') 
and Affiliations.ending is null 
and Affiliations.isPrimary='t'
--... SQL statements, possibly using other views/functions defined by you ...
;



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
select code as code, name as name, substring(cast(year as text),3,2) as year,(select id2rate(S1)) as s1_HD_rate, (select id2rate(S2)) as s2_HD_rate
from code_year_S1_S2
where code in (select code from code_year_S1_S2 group by code having count(code)=10)
--... SQL statements, possibly using other views/functions defined by you ...
;