-module(control).
-export([graphToNetwork/1, extendNetwork/4]).
graphToNetwork(Graph)->
  NameToPidTab = ets:new(graph_dict, [set]),
  InEdgeTab = ets:new(no_in_edges, [set]),
  count_incoming_edges(InEdgeTab, Graph),
  [Node|Rest] = Graph,
  {NodeName, _Edges} = Node,
  First = router:start(NodeName),
  ets:insert(NameToPidTab, {NodeName, First}),
  buildNetwork(Rest, NameToPidTab),
  init_nodes(Graph, NameToPidTab, InEdgeTab),
%%  io:format("Table ~w", [ets:match(NameToPidTab, '$1')]),
  receive_reply(length(Graph)),
  First.

buildNetwork(Graph, Tab) -> %start all nodes
  case Graph of
    [] ->
      ok;
    [Node|Rest] ->
      {NodeName, _Edges} = Node,
      Pid = router:start(NodeName),
      ets:insert(Tab, {NodeName, Pid}),
      buildNetwork(Rest,Tab)
  end.

receive_reply(N) when N == 1->
  receive
    {committed, _Pid, 0} ->
      ok;
    {abort, _Pid, 0} ->
      ok
  end;
receive_reply(N) ->
  receive
    {committed, _Pid, 0} ->
      receive_reply(N-1);
    {abort, _Pid, 0} ->
      receive_reply(N-1)
  end.

extendNetwork(RootPid, SeqNum, From, {NodeName, Edges})->
  RootPid ! {control, self(), self(), SeqNum,
    fun(Name, Table) ->
      case Name of
        From -> % init
          NewNodePid = router:start(NodeName),
          NewNodePid ! {control, self(), self(), 0,  init_extend_node(Edges)},
          ets:insert(Table, {NodeName, NewNodePid}), % insert new entry
          case sets:is_element(self(), sets:from_list([Pid|| {Pid, _} <- Edges])) of
            true -> % inc NoInEdges
              [{_, Count}] = ets:lookup(Table, '$NoInEdges'),
              ets:insert(Table, {'$NoInEdges', Count+1});
            false -> ok
          end,
          [];
        _Else ->
          [{_, FromPid}] = ets:lookup(Table, From),
          ets:insert(Table, {NodeName, FromPid}),
          case sets:is_element(self(),sets:from_list([Pid|| {Pid,_} <-Edges])) of
            true ->
              [{_, Count}] = ets:lookup(Table, '$NoInEdges'),
              ets:insert(Table, {'$NoInEdges', Count+1});
            false -> ok
          end,
          []
      end
    end
  },
  receive
    {committed, RootPid, SeqNum} ->
      true;
    {abort, RootPid, SeqNum} ->
      false
  end.

init_extend_node(Edges) ->
  fun(_Name, Table)->
    lists:foreach(fun({Pid, NodeNames}) ->
      lists:foreach(fun(NodeName) -> ets:insert(Table, {NodeName,Pid}) end, NodeNames)
    end, Edges),
    []
  end.

init_nodes(Graph, Tab, InEdgeTab) ->
  case Graph of
    [] -> ok;
    [Node|Rest] ->
      {NodeName, Edges} = Node,
      [{NodeName, NodePid}] = ets:lookup(Tab, NodeName),
      [{_,NoInEdges}] = ets:lookup(InEdgeTab, NodeName),
      init_node(NodePid, Edges, Tab, NoInEdges),
      init_nodes(Rest, Tab, InEdgeTab)
  end.



init_node(NodePid, Edges, Tab, NoInEdges) ->
  NodePid ! {control, self(), self(), 0,
    fun(RouterName, Table)
      ->
      ets:insert(Table, {'$NoInEdges', NoInEdges}),
      insert_edges(RouterName, Table, Edges, Tab),
      []
    end
    }.

insert_edges(RouterName,Table, Edges, NameToPidTab) ->
  case Edges of
    [] ->
      ok;
    [Node|Rest]->
      {Dest, Names} = Node,
      insert_tab(RouterName, Table, Dest, Names, NameToPidTab),
      insert_edges(RouterName, Table, Rest, NameToPidTab)
  end.

insert_tab(RouterName, Table, Dest, Names, NameToPidTab) ->
  case Names of
    [] ->
      ok;
    [Node|Rest] ->
      [Obj] = ets:lookup(NameToPidTab, Dest),
      {Dest, Pid} = Obj,
      ets:insert(Table, {Node, Pid}),
      insert_tab(RouterName,Table, Dest, Rest, NameToPidTab)
  end.

count_incoming_edges(InEdges, Graph) ->
  case Graph of
    [] -> ok;
    [Node|Rest]->
      {_NodeName, Edges} = Node,
      DestNode = [Dest|| {Dest, _} <- Edges],
%%      io:format("~w ~n", [DestNode]),
      lists:foreach(fun(El)->
        Result = ets:lookup(InEdges, El),
        case Result of
          [] -> ets:insert(InEdges, {El, 1});
          [{_, Count}] -> ets:insert(InEdges, {El, Count+1})
        end end, DestNode),
      count_incoming_edges(InEdges, Rest)
  end.

