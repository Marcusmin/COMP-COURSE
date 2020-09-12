-- Q7: 
create or replace view program_percentage
as
                select program,
                (cast((select count(students.id) 
                       from program_enrolments 
                       inner join students on program_enrolments.student=students.id 
                       where students.stype='intl' and program=pe.program) as float))
                       *100
                /
                (cast((select count(students.id) 
                        from program_enrolments 
                        inner join students on program_enrolments.student=students.id 
                        where program=pe.program)as float))
                as percentage 
                from program_enrolments as pe  group by pe.program
;
create or replace view Q7(code, name)
as
select code,name
from programs
where id in
           ( select program
           from program_percentage
            where percentage>50.0
            )

--... SQL statements, possibly using other views/functions defined by you ...
;
