-- Q8:
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
