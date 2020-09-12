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

