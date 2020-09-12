--Q1:

drop type if exists RoomRecord cascade;
create type RoomRecord as (valid_room_number integer, bigger_room_number integer);

create or replace view nb_of_stu_enrolled_in_course(c_id, nb_of_student)
as
select Course_enrolments.course, count(Course_enrolments.student)
from Course_enrolments
group by Course_enrolments.course
;

create or replace function Q1(course_id integer)
    returns RoomRecord
as $$
declare
	r record
	students_on_waitlist integer := 0;
	students_enrolled integer := 0;
	total_students integer := 0;
	is_valid_course boolean := 'f';
	valid_course_id integer;
	nb_of_valid_room integer := 0;
	nb_of_bigger_room integer := 0;
	INVALID_COURSEID EXCEPTION;
BEGIN
	for valid_course_id in select id from Courses
	loop
		if 	(course_id = valid_course_id) then
			is_valid_course := 't';
			student_on_waitlist := select count(student) from Course_enrolment_waitlist group by course having course = course_id;
			students_enrolled := select nb_of_student from nb_of_stu_enrolled_in_course where c_id = course_id;
			total_students := student_on_waitlist + students_enrolled
		end if;
	end loop;
	if (is_valid_course <> 't') then
		raise INVALID_COURSEID;
	end if;
	for r in select * from Rooms
	loop
		if (r.capacity >= students_enrolled) then
            nb_of_valid_room := nb_of_valid_room + 1
        end if
        if (r.capacity >= total_students) then
            nb_of_bigger_room = nb_of_bigger_room + 1
	end loop;
    return nb_of_valid_room, nb_of_bigger_room
EXCEPTION
	when INVALID_COURSEID then
		raise notice 'INVALID_COURSEID';
end;
--... SQL statements, possibly using other views/functions defined by you ...
$$ language plpgsql;


--Q2:

drop type if exists TeachingRecord cascade;
create type TeachingRecord as (cid integer, term char(4), code char(8), name text, uoc integer, average_mark integer, highest_mark integer, median_mark integer, totalEnrols integer);

create or replace function Q2(staff_id integer)
	returns setof TeachingRecord
as $$
--... SQL statements, possibly using other views/functions defined by you ...
$$ language plpgsql;


--Q3:

drop type if exists CourseRecord cascade;
create type CourseRecord as (unswid integer, student_name text, course_records text);

create or replace function Q3(org_id integer, num_courses integer, min_score integer)
  returns setof CourseRecord
as $$
--... SQL statements, possibly using other views/functions defined by you ...
$$ language plpgsql;
