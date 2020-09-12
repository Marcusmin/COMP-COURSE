-- Q6:
create or replace function
	Q11(text) 
	returns text
as
$$
		select code||' '||name||' '||uoc
		from subjects
		where subjects.code=$1;
--... SQL statements, possibly using other views/functions defined by you ...
$$ language sql;