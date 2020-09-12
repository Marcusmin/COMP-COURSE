-module(server).
-export([server/0]).
init()->db:load("DB").
% receive(
%   {insert/delete], {a1, a2}, clientid}
%   {lookup, a1, clientid}
%   {stop, clientid}
%)
% send(
%   {ok/delete, data}
% )

server()->
    Address_DB = init(),
    io:format("server id is ~w ~n", [self()]),
    receive
        % operation is an atom, Address is a {atom, atom}
        {Operation, Address, Client_pid} when (Operation == insert orelse Operation == delete) ->
            New_Addr_DB = do_job(Operation, Address, Address_DB),
            Client_pid! ok,
            server(New_Addr_DB);
        {Operation, Address, Client_pid} when Operation == lookup ->
            Data = do_job(Operation, Address, Address_DB),
            if 
                Data == false ->
                    io:format("not found~n"),
                    Client_pid! error;
                true ->
                    io:format("found send feedback ~w ~n", [Client_pid]),
                    Client_pid! {ok, Data}
            end,
            server(Address_DB);
        {Operation, Address, Client_pid}->
            io:format("operation ~w invalid~n", [Operation]),
            io:format("~w operation fail ~n", [Address]),
            Client_pid! error,
            server(Address_DB);
        {stop, Client_pid} ->
            db:store(Address_DB,"DB"),
            Client_pid!ok,
            io:format("server stop~n")
    end.

server(Address_DB)->
    receive
        {Operation, Address, Client_pid} when (Operation == insert orelse Operation == delete) ->
            New_Addr_DB = do_job(Operation, Address, Address_DB),
            Client_pid! ok,
            server(New_Addr_DB);
        {Operation, Address, Client_pid} when Operation == lookup ->
            Data = do_job(Operation, Address, Address_DB),
            if 
                Data == error ->
                    Client_pid! error;
                true ->
                    Client_pid! {ok, Data}
            end,
            server(Address_DB);
        {Operation, Address, Client_pid}->
            io:format("operation ~w invalid~n", [Operation]),
            io:format("~w operation fail ~n", [Address]),
            Client_pid! error,
            server(Address_DB);
        {stop, Client_pid}->
            db:store(Address_DB, "DB"),
            Client_pid! ok,
            io:format("stop~n")
    end.

% insert address to db
do_job(insert, {Address1, Address2}, Address_DB)->
    io:format("server insert ~w ~w ~n", [Address1, Address2]),
    Address_DB#{
        Address1 => Address2
    };

do_job(lookup, Address1, Address_DB)->
    io:format("server look up ~w~n",[Address1]),
    maps:find(Address1, Address_DB);

do_job(delete, Address1, Address_DB)->
    io:format("server delete ~w~n", [Address1]),
    maps:remove(Address1, Address_DB).

