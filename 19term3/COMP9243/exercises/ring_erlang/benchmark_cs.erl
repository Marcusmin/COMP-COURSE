-module(benchmark_cs).
-export([init/0]).

init()->
    Server = spawn(server, server, []),
    create_client(100, Server).

create_client(1, Server)->
    spawn(client, client, [Server, lookup, jack9]);
create_client(Number, Server) ->
    C = spawn(client, client, [Server, lookup, jack9]),
    io:format("~w ~n", [C]),
    create_client(Number-1, Server).
