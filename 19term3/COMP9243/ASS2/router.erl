-module(router).
-export([start/1]).
% spawn new router process named RouterName
start(RouterName) when is_atom(RouterName) ->
    Pid = spawn(fun()->init(RouterName) end),
%%    register(RouterName, Pid),
    Pid.    % pid as return value

init(RouterName) -> 
    % create ets table
    Table = ets:new(RouterName, [set]),
    SeqTable = ets:new(RouterName, [set]),
    TableCopy = ets:new(RouterName, [set]), % same config to create a table
    make_table_copy(Table, TableCopy),
    % receiving routing message
    message_recv(RouterName, Table, TableCopy, SeqTable, sets:new()).

% TODO: details
message_recv(RouterName, Table, TableCopy, SeqTable, PreSeqNums) ->
%%    Delay= 10,
    receive
        {message, Dest, _From, Pid, Trace} ->
            forward_msg(RouterName, Dest, Pid, Trace, Table),
            message_recv(RouterName, Table, TableCopy, SeqTable, PreSeqNums);
        {control, From, Pid, SeqNum, ControlFun} when SeqNum /= 0->
            case sets:is_element(SeqNum, PreSeqNums) of % if request is new
                false ->
                    ets:insert(SeqTable, {SeqNum, From}), % record where control msg comes from
                    % if node is root process
                    case Pid of From ->
                        forward_control(ControlFun, Table, Pid, SeqNum),
                        make_table_copy(Table, TableCopy),
                        Res = handle_control(ControlFun, Table, RouterName),
                        Reply = wait_reply(ets:info(TableCopy, size)-1, SeqNum), % wait all node reply except itself
                        case Res of
                            yes when Reply == yes->
                                send_do_commit(TableCopy, SeqNum),
                                do_commit(Table, TableCopy),
                                Pid ! {committed, self (), SeqNum};
                            yes when Reply == abort ->
                                send_do_abort(TableCopy, SeqNum),
                                do_abort(Table, TableCopy),
                                Pid ! {abort, self (), SeqNum};
                            abort ->
                                send_do_abort(TableCopy, SeqNum),
                                do_abort(Table, TableCopy),
                                Pid ! {abort, self (), SeqNum}
                        end,
                        clear_expired(sets:to_list(sets:add_element(SeqNum, PreSeqNums)));
                    % if common node
                        _Else->
                            forward_control(ControlFun, Table, Pid, SeqNum),
                            make_table_copy(Table, TableCopy),
                            Result = handle_control(ControlFun, Table, RouterName),
%%                            dump_table(ets:match(TableCopy, '$1'), RouterName),
                            send_reply(From, Result, SeqNum),
                            case Result of
                                abort ->
                                    do_abort(Table, TableCopy);
                                yes ->
                                    % uncertain state
%%                                    io:format("~w waiting coordinator ~n", [RouterName]),
                                    Cmd = wait_coordinator_cmd(TableCopy, SeqTable, RouterName, SeqNum),
%%                                    io:format("~w receive from coordinator ~n", [RouterName]),
                                    case Cmd of
                                        abort ->
                                            do_abort(Table, TableCopy);
                                        yes ->
                                            do_commit(Table, TableCopy)
%%                                            dump_table(ets:match(Table,'$1'), RouterName)
                                    end
                            end,
                            clear_expired(sets:to_list(sets:add_element(SeqNum, PreSeqNums)))
                    end;
            % if request is duplicated, discard it
                true ->
                    ok
            end,
            message_recv(RouterName, Table, TableCopy, SeqTable, sets:add_element(SeqNum, PreSeqNums));
        % TODO: handle init control
        {control, _From, Pid, SeqNum, ControlFun} -> % handle process initialization
            case sets:is_element(SeqNum, PreSeqNums) of
                false ->
                    Res = handle_control(ControlFun, Table, RouterName),
                    case Res of 
                        yes ->
                            Pid !{committed, self(), SeqNum};
                        abort ->
                            Pid ! {abort, self(), SeqNum}
                    end
            end,
            message_recv(RouterName, Table, TableCopy, SeqTable, sets:add_element(SeqNum, PreSeqNums));
        {dump, From} ->
            From ! {table, self(), ets:match(Table, '$1')},
            message_recv(RouterName, Table, TableCopy, SeqTable, PreSeqNums);
        stop ->
            ets:delete(Table), ets:delete(TableCopy), ets:delete(SeqTable), stop
        % These reply should only be handled during 2pc period, otherwise, they are garbage
%%        {abort, _SeqNum} ->
%%            forward_reply(SeqTable, SeqNum, abort),
%%            message_recv(RouterName, Table, TableCopy, SeqTable, PreSeqNums);
%%        {yes, _SeqNum} ->
%%            forward_reply(SeqTable, SeqNum, yes),
%%            message_recv(RouterName, Table, TableCopy, SeqTable, PreSeqNums);
%%        _Else ->
%%            message_recv(RouterName, Table, TableCopy, SeqTable, PreSeqNums)
%%    after Delay -> timeout
    end.

% forward 2pc result
forward_result(Table, Result, Exception, SeqNum) ->
    ets:safe_fixtable(Table, true),
    Dest = ets:first(Table),
    case Dest of
        '$end_of_table' ->
            ets:safe_fixtable(Table, false);
        '$NoInEdges' ->
            forward_result(Table, Result, SeqNum, Exception, Dest),
            ets:safe_fixtable(Table, false);
        Exception ->
            forward_result(Table, Result, SeqNum, Exception, Dest),
            ets:safe_fixtable(Table, false);
        _Else ->
            [NextRouter] = ets:lookup(Table, Dest),
            {Dest, NextPid} = NextRouter,
%%            io:format("First: ~w forward to ~w Pid ~w ~n", [self(),Dest, NextPid]),
            NextPid ! {Result, Dest, SeqNum},
            forward_result(Table, Result, SeqNum,Exception, Dest),
            ets:safe_fixtable(Table, false)
    end.

forward_result(Table, Result, SeqNum, Exception, PrevKey) ->
    NextDest = ets:next(Table, PrevKey),
    case NextDest of
        '$end_of_table' ->
            ok;
        '$NoInEdges' ->
            forward_result(Table, Result, SeqNum, Exception, NextDest);
        Exception ->
            forward_result(Table, Result, SeqNum, Exception, NextDest);
        _Else ->
            [{NextDest, NextRouter}] = ets:lookup(Table, NextDest),
            NextRouter ! {Result, NextDest,SeqNum},
%%            io:format("~w forward ( ~w)to ~w Pid ~w ~n", [self(),Result, NextDest, NextRouter]),
            forward_result(Table, Result, SeqNum, Exception, NextDest)
    end.





% forward message to next node
forward_msg(RouterName, Dest, Pid, Trace, Table) ->
    NewTrace = lists:append(Trace, [RouterName]), % append pid to trace list
    case Dest of
        RouterName -> % send to coordinator
            Pid!{trace, self(), NewTrace}; % send directly to controller
        _Else -> % forward to next
            [Obj] = ets:lookup(Table, Dest),
            {_, Next} = Obj,
            Next ! {message, Dest, self(), Pid, NewTrace} % forward the message to next node
    end.


% forward control message along outgoing edges
forward_control(ControlFun, Table, Pid, SeqNum) ->
    ets:safe_fixtable(Table, true),
    send_control_by_table(Table, Pid, ControlFun, SeqNum),
    ets:safe_fixtable(Table, false).

% init the routine that send control information to all node it connected
send_control_by_table(Table, Pid, ControlFun, SeqNum) ->
    Dest = ets:first(Table),
    case Dest of
        '$end_of_table' ->
            ok; % end of table
        '$NoInEdges' ->
            send_control_to_next(Table, Dest, Pid, ControlFun, SeqNum);
        _Else ->
            [{Dest, NextRouter}] = ets:lookup(Table, Dest),
            NextRouter ! {control, self(), Pid, SeqNum, ControlFun},
            send_control_to_next(Table, Dest, Pid, ControlFun, SeqNum)
    end.

% traverse the table and send control msg
send_control_to_next(Table, PrevRouter, Pid, ControlFun, SeqNum) ->
    Dest = ets:next(Table, PrevRouter),
    case Dest of
        '$end_of_table' ->
            ok; % end of table
        '$NoInEdges' ->
            send_control_to_next(Table, Dest, Pid, ControlFun, SeqNum);
        _Else ->
            [{Dest, NextRouter}] = ets:lookup(Table, Dest),
            NextRouter ! {control, self(), Pid, SeqNum, ControlFun},
            send_control_to_next(Table, Dest, Pid, ControlFun, SeqNum)
    end.

% copy table to anther table
make_table_copy(Table, TableCopy) ->
    ets:delete_all_objects(TableCopy),
    ets:safe_fixtable(Table, true),
    FirstKey = ets:first(Table),
    case FirstKey of
        '$end_of_table' ->
            ets:safe_fixtable(Table, false);
        _Else  ->
            FirstObjs = ets:lookup(Table, FirstKey),
            ets:insert(TableCopy, FirstObjs),
            make_table_copy(Table, TableCopy, FirstKey),
            ets:safe_fixtable(Table, false)
    end.

make_table_copy(Table, TableCopy, PrevKey) ->
    Key = ets:next(Table, PrevKey),
    case Key of
        '$end_of_table' ->
            ok;
        _Else ->
            Objs = ets:lookup(Table, Key),
            ets:insert(TableCopy, Objs),
            make_table_copy(Table, TableCopy, Key)
    end.




% TODO: handle ControlFunc, make a copy of Table, if commit abort, roll back
handle_control(ControlFun, Table, RouterName)->
    Children = ControlFun(RouterName, Table),
    % result of ControlFunc is abort or success
    case Children of
        abort -> % abort state
            abort;
        PidList ->
            exit_all_process(PidList),
            'yes'
    end.

exit_all_process(PidList) ->
    case PidList of
        [] -> ok;
        [Pid|Rest] ->
            exit(Pid, kill),
            exit_all_process(Rest)
    end.

% TODO There may raise bugs if this function accept the same message as message_recv/4
% Take care if node count is 0
wait_reply(NodeCount, SeqNum) when NodeCount == 1 ->
    Delay = 5,
    receive
        {abort, SeqNum} ->
            abort;
        {yes, SeqNum} ->
            yes
    after Delay ->
        abort
    end;
wait_reply(NodeCount, SeqNum) ->
    Delay = 5,
    receive
        {abort, SeqNum} ->
            abort;  % if a message abort, return immediately
        {yes, SeqNum} ->
            yes,
            wait_reply(NodeCount-1, SeqNum)
    after Delay ->
        abort
    end.

% follow the incoming edge
send_reply(From, Result, SeqNum)->
    From ! {Result, SeqNum}.

% TODO: forward reply to root process
forward_reply(SeqTable, SeqNum, Res) ->
    [{SeqNum, From}] = ets:lookup(SeqTable, SeqNum),
    From ! {Res, SeqNum}.

clear_expired(SeqList) when is_list(SeqList) ->
    case SeqList of
        [] ->
            ok;
        [SeqNum|T] ->
            receive
                {abort, SeqNum}  ->
                    ok;
                {yes, SeqNum} ->
                    ok;
                {doabort, _From, SeqNum} ->
                    ok;
                {docommit, _From, SeqNum} ->
                    ok
            after 0 ->
                ok
            end,
            clear_expired(T)
    end.       
% clear_expired(SeqSet) ->
%     case sets:is_empty(SeqSet) of
%         true ->
%             ok;
%         false ->
%             SeqList = sets:to_list(SeqSet),
%             clear_expired(SeqList)
%     end.
% rollback the table
do_abort(Table, TableCopy) ->
    ets:delete_all_objects(Table),
    make_table_copy(TableCopy, Table).
do_commit(Table, TableCopy) ->
    ets:delete_all_objects(TableCopy),
    make_table_copy(Table, TableCopy).

% send do abort to all nodes in the network
send_do_abort(Table, SeqNum) ->
    ets:safe_fixtable(Table, true),
    FirstKey = ets:first(Table),
    case FirstKey of
        '$end_of_table' ->
            ets:safe_fixtable(Table, false);
        '$NoInEdges' ->
            send_do_abort(Table, SeqNum, FirstKey),
            ets:safe_fixtable(Table, false);
        _Else ->
            [{FirstKey, FirstRouter}] = ets:lookup(Table, FirstKey),
            FirstRouter ! {doabort, FirstKey, SeqNum},
            send_do_abort(Table, SeqNum, FirstKey),
            ets:safe_fixtable(Table, false)
    end.

send_do_abort(Table, SeqNum, PrevKey) ->
    Key = ets:next(Table, PrevKey),
    case Key of
        '$end_of_table' ->
            ok;
        '$NoInEdges'->
            send_do_abort(Table, SeqNum, Key);
        _Else ->
            [{Key, Router}] = ets:lookup(Table, Key),
            Router ! {doabort, Key, SeqNum},
            send_do_abort(Table, SeqNum, Key)
    end.

% send do commit to all nodes in the network
send_do_commit(Table, SeqNum) ->
    ets:safe_fixtable(Table, true),
    FirstKey = ets:first(Table),
    case FirstKey of
        '$end_of_table' ->
            ets:safe_fixtable(Table, false);
        '$NoInEdges' ->
            send_do_commit(Table, SeqNum, FirstKey),
            ets:safe_fixtable(Table, false);
        _Else ->
            [{FirstKey, FirstObj}] = ets:lookup(Table, FirstKey),
%%            io:format(" send do commit to First ~w ~n", [FirstKey]),
            FirstObj ! {docommit, FirstKey, SeqNum},
            send_do_commit(Table, SeqNum, FirstKey),
            ets:safe_fixtable(Table, false)
    end.
send_do_commit(Table, SeqNum, PrevKey) ->
    Key = ets:next(Table, PrevKey),
    case Key of
        '$end_of_table' ->
            ok;
        '$NoInEdges' ->
            send_do_commit(Table, SeqNum, Key);
        _Else ->
            [{Key, Pid}] = ets:lookup(Table, Key),
            Pid ! {docommit, Key, SeqNum},
%%            io:format("send do commit to ~w pid ~w ~n", [Key, Pid]),
            send_do_commit(Table, SeqNum, Key)
    end.
% wait if abort or commit
wait_coordinator_cmd(Table, SeqTable, RouterName, SeqNum) ->
%%    Delay = 5,
    receive
        {docommit, From, SeqNum} ->
%%            io:format("~w receive do commit ~n", [RouterName]),
            forward_result(Table, docommit, From, SeqNum),
%%            io:format("~w forwarded result ~n", [RouterName]),
            yes;
        {doabort, From, SeqNum} ->
            forward_result(Table, doabort, From, SeqNum),
            abort;
        {abort, SeqNum} ->
            forward_reply(SeqTable, SeqNum, abort),
            wait_coordinator_cmd(Table, SeqTable, RouterName, SeqNum);
        {yes, SeqNum} ->
            forward_reply(SeqTable, SeqNum, yes),
            wait_coordinator_cmd(Table, SeqTable, RouterName, SeqNum)
%%        _Else ->
%%            io:format("unexpected cmd ~w ~n", [Else]),
%%            wait_coordinator_cmd(Table, SeqTable, RouterName, SeqNum)
%%    after Delay ->
%%        abort
    end.

%%dump_table(Table, RouterName) ->
%%    io:format("~w Table is ~w ~n", [RouterName,Table]).