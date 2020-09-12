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
