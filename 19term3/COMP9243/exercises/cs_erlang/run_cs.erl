-module(run_cs).
-export([run_cs/0]).
run_cs()->
    % create a single server
    Server_Pid = spawn(server, server, []),
    io:format("create server ~w ~n", [Server_Pid]).