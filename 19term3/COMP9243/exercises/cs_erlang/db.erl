-module(db).
-export([load/1, store/2]).

load(DB)->
    {State, Info} = file:read_file_info(DB),Info,
    case State of
        ok ->
            {ok, Fd} = file:open(DB, [read]);
        error ->
            {ok, Fd} = file:open(DB, [write, read])
    end,
    Lines = get_all_lines(Fd),
    process_lines(Lines, #{}).


process_lines([], Map) ->
    Map;
process_lines([Line|Rest],Map)->
    [Name,Address] = string:split(string:strip(Line, right, 10), " "),
    New_Map = Map#{
        list_to_atom(Name)=>list_to_atom(Address)
    },
    process_lines(Rest,New_Map).

get_all_lines(Fd) -> 
    Line = file:read_line(Fd),
    case Line of
        eof ->
            [];
        {ok, Data}->
            [Data|get_all_lines(Fd)];
        {error, Reaason} ->
            io:format("error: ~w ~n", [Reaason]),
            []
    end.

store(Pairs, DB) ->
    {ok, Fd} = file:open(DB, [write]),
    Iter = maps:iterator(Pairs),
    write_to_db(Iter, Fd).

write_to_db(Iter, Fd) -> 
    NextRecord = maps:next(Iter),
    case NextRecord == none of 
        true -> done;
        false -> 
            {K, V, I} = NextRecord,
            io:format(Fd, "~w ~w~n", [K, V]),
            write_to_db(I, Fd)
    end.


