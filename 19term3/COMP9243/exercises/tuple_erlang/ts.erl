-module(ts).
-export([new_tuple_space/0, in/2, out/2]).

% create a new tuple
% which is a erlang list
new_tuple_space() -> 
    spawn(ts, repo_process, []).
% add tuple in ts
in(TS, Tuple) ->
    TS ! {in, Tuple}.

out(TS, Pattern) -> 
    TS! {out, Pattern},
    receive
        Result -> Result
    after
        100->{}
    end.

repo_process(Owner) when  ->
    TS = [],
    receive
        {in, Tuple} -> 
            NewTS = [Tuple|TS],
            repo_process(NewTS, Owner);
        {out, Tuple} -> 
            [], 
            Tuple, % avoid warning
            repo_process()
    end;

repo_process(TS) ->
    receive
        {in, Tuple} ->
            repo_process([Tuple|TS]);
        {out, Tuple} ->
            {Matched, NewTS} = pattern_match(Tuple, Pattern),

            repo_process(NewTS)
    end.

