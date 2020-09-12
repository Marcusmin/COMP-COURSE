-module(client).
-export([client/1]).

deal_with_string(Line) -> 
    NewLine = string:strip(Line, right, 10),
    case list_to_atom(NewLine) of 
        stop -> ["stop", ""];
        OtherWise ->OtherWise, string:split(NewLine, " ")
    end.
client(Server_Pid)->
    io:format("Usage:insert/delete/lookup/stop [address1] [address2]~n"),
    Line = io:get_line(">"),
    % remove tailling newline & split
    [Operation,Address] = deal_with_string(Line),
    io:format("~w ~w ~n", [list_to_atom(Operation), Address]),
    case
        list_to_atom(Operation) of
        stop->
            io:format("send stop~n"),
            send(Server_Pid, list_to_atom(Operation), self()),
            receive
                ok->
                    io:format("server stop~n")
            after 100 ->
                io:format("no response ~n")
            end;
        insert -> 
            [Address1, Address2] = string:split(Address, " "),
            send(Server_Pid, list_to_atom(Operation), {list_to_atom(Address1), list_to_atom(Address2)}),
            receive
                ok->
                    io:format("ok~n");
                error->
                    io:format("error~n")
            after 100 ->
                io:format("no response~n")
            end;
        delete ->
            send(Server_Pid, list_to_atom(Operation), list_to_atom(Address)),
            receive
                ok ->
                    io:format("ok ~n");
                error -> 
                    io:format("error~n")
            after 100->
                io:format("no response ~n")
            end;
        lookup -> 
            send(Server_Pid, list_to_atom(Operation), list_to_atom(Address)),
            receive
                {ok, Data} ->
                    {ok, Value} = Data,
                    io:format("~w ~w ~n", [list_to_atom(Address), Value]);
                error -> 
                    io:format("not found~n")
            after 100->
                io:format("no response~n")
            end
    end.

send(Server_Pid, Operation, Client_pid) when Operation==stop ->
    Server_Pid! {Operation, Client_pid};
send(Server_Pid, Operation, Data) ->
    Server_Pid! {Operation, Data, self()}.