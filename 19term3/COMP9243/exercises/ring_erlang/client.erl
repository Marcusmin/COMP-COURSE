-module(client).
-export([client/3]).

client(Server, Operation, Data)->
    case
        Operation of
        stop->
            io:format("send stop~n"),
            send(Server, Operation, self()),
            receive
                ok->
                    io:format("server stop~n")
            after 100 ->
                io:format("no response ~n")
            end;
        insert -> 
            send(Server, list_to_atom(Operation), Data),
            receive
                ok->
                    io:format("ok~n");
                error->
                    io:format("error~n")
            after 100 ->
                io:format("no response~n")
            end;
        delete ->
            send(Server, list_to_atom(Operation), Data),
            receive
                ok ->
                    io:format("ok ~n");
                error -> 
                    io:format("error~n")
            after 10000->
                io:format("no response ~n")
            end;
        lookup -> 
            send(Server, Operation, Data),
            receive
                {ok, Data} ->
                    io:format("receive~n"),
                    {ok, Value} = Data,
                    io:format("~w ~w ~n", [Data, Value]);
                error -> 
                    io:format("not found~n");
            % after 100->
            %     io:format("no response~n")
                Other ->
                    {ok, Value} = Other,
                    io:format("unexpected here ~w ~n", [Value])
            end
    end.

send(Server, Operation, Client) when Operation==stop ->
    Server! {Operation, Client};
send(Server_Pid, Operation, Data) ->
    Server_Pid! {Operation, Data, self()}.