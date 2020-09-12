%%%-------------------------------------------------------------------
%%% @author keyangli
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 14. Nov. 2019 8:44 pm
%%%-------------------------------------------------------------------
-module(testAbort).
-author("keyangli").

%% API
-export([runTest/0]).
circularNetwork3 () ->
  [{red  , [{white, [white, blue]}]},
    {white, [{blue , [red, blue]}]},
    {blue , [{red  , [red, white]}]}
  ].
runTest () ->
  io:format ("*** Starting router network...~n"),
  CGraph = circularNetwork3 (),
  RedPid = control:graphToNetwork (CGraph),
  networkTest:verifyNetwork (RedPid, CGraph),

  {WhitePid, _} = networkTest:probeNetwork (RedPid, white),
  {BluePid , _} = networkTest:probeNetwork (RedPid, blue ),
  if (WhitePid == undef) or (BluePid == undef) ->
    io:format ("*** ERROR: Corrupt network!~n");
    true -> true
  end,

  io:format ("*** Reversing routing cycle...~n"),
  RedPid ! {control, self (), self (), 1,
    fun (Name, Table) ->
      case Name of
        red   -> ets:insert (Table, [{white, BluePid },
          {blue , BluePid }]);
        white -> ets:insert (Table, [{red  , RedPid  },
          {blue , RedPid  }]);
        blue  -> ets:insert (Table, [{red  , WhitePid},
          {white, WhitePid}])
      end,
      abort
    end},
  receive
    {committed, RedPid, 1} -> io:format ("*** ...done.~n");
    {abort    , RedPid, 1} ->
      io:format ("*** ERROR: Re-configuration failed!~n")
  after 10000              ->
    io:format ("*** ERROR: Re-configuration timed out!~n")
  end,
  networkTest:verifyNetwork (RedPid, circularNetwork3 ()).