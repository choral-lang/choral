include "Main.mzn";

% --== Variable ==--
array[roles, 0..in_size+1] of var int: runtimes;

% --== Helper functions ==--
function var int: get_runtime_after_if( roles: role, int: s ) = 
  sum( 
    [ runtimes[role, blocks[if_blocks[if_, block], 2]] | 
    if_ in NUM_IFS,
    block in 1..2
    where
      s == blocks[if_blocks[if_, 2], 2] + 1 ] 
  );

function bool: comes_after_if(int: s) = 
  exists( 
    [ if(s == blocks[if_blocks[if_, 2], 2] + 1) then true else false endif | 
          if_ in NUM_IFS ] 
  );

function int: get_prev(int: s) = 
  if(
    exists( 
      [
      if(s == blocks[if_blocks[if_, branch], 1] + 1) 
        then true 
        else false
      endif | 
      if_ in NUM_IFS,
      branch in 1..2 ] 
    )
  )
    then
      sum(
        [ 
        if(s == blocks[if_blocks[if_, branch], 1] + 1) 
          then blocks[ if_blocks[if_, 1], 1 ] 
          else 0 
        endif | 
        if_ in NUM_IFS,
        branch in 1..2 ]
      )
    else
      s-1
  endif;

function var int: get_base_runtime(roles: role, int: s) =
  if( comes_after_if(s) )
    then 
      get_runtime_after_if(role, s)
    else
      runtimes[role, get_prev(s)]
  endif;

% --== Constraints ==--
constraint forall( r in roles ) ( runtimes[r, 0] = 0 ); % first column is all zeros
constraint 
forall( role in roles )(
  forall( s in IN_SIZE )(
    (runtimes[role, s] =
      max(
        [ get_base_runtime(role, s) ] ++
        [ if(out_prog_idx[d] == s /\ dep_to[d] == role) % previous runtime of roles who send messages to this role at this statement
            then get_base_runtime(dep_from[d], s)+1 
            else 0 
          endif |
          d in DEPS
        ] ++
        [ if(selections[if_, branch, role] == s) % previous runtime of roles that send selections to this role at this statement
            then get_base_runtime(if_roles[if_], s)+1 
            else 0 
          endif |
          if_ in NUM_IFS, 
          branch in 1..2  
        ]
      ) +
      if(statements_roles[s] == r(role)) 
        then statements_runtimes[s] 
        else 0 
      endif + % if this statement is executed by this role
      sum( % all the messages sent from this role at this statement
        [ 1 | 
        k in DEPS 
        where 
          dep_from[k] == role /\ 
          out_prog_idx[k] == s /\ 
          forall( k2 in DEPS[1]..k-1 where dep_from[k2] == role /\ out_prog_idx[k2] == s )
            ( dep_to[k] != dep_to[k2] )
        ] 
      ) +
      sum( % all the selections sent from this role at this statement
        [ 1 |
        if_ in NUM_IFS,
        branch in 1..2,
        other in roles
        where
          if_roles[if_] == role /\
          selections[if_, branch, other] == s
        ]
      )
    )  
  )
);

constraint forall( role in roles )(
  runtimes[role, in_size+1] = get_base_runtime(role, in_size+1)
);

solve minimize max( [ runtimes[r, in_size+1] | r in roles ] );
output 
[ "Selections: \n" ] ++
[ concat([ concat(
  [ "[" ] ++
  [ concat(
    [ if(r==roles[1]) then " " else "" endif ] ++
    ["\(selections[i, branch, r])"] ++ 
    [if(r < roles[card(roles)]) then ", " else " " endif]
    ) | r in roles ] ++
  [ "]" ]
) | branch in 1..2 ] ++ ["\n"]) | i in NUM_IFS] ++

[ "Output list: \n" ]++
[ "[ \(out_prog_idx[DEPS[1]])" ] ++ 
[ ", \(out_prog_idx[i])" | i in DEPS where i > DEPS[1] ] ++ 
[ " ]\n" ] ++

[ "Output program: \n" ]++
[ concat(
  ["-  : \(if_roles[if_]) --(select)-> \(role)\n" | if_ in NUM_IFS, role in roles 
    where 
      (fix(selections[if_, 1, role]) = i \/
      fix(selections[if_, 2, role]) = i)]
  ) ++
  concat(
    ["-  : \(dep_from[j]) --( \(dependencies[j]) )-> \(dep_to[j])\n" | j in DEPS 
      where fix(out_prog_idx[j]) = i ]
  ) ++ 
  concat(
    ["\(i) " ++
      if(i<10) then " " else "" endif] ++ 
    [": \(statements[i])\n"]
  ) | i in IN_SIZE ] ++

[ "max runtime: \(max( [ runtimes[r, in_size+1] | r in roles ] ))\n" ] ++
[ concat( 
  ["___"] ++ 
  ["\(i)__" ++
    if(i<10) then "_" else "" endif | i in index_set_2of2(runtimes)] ++ 
  ["\n"] ) ] ++
[ concat( 
  ["\(i): "] ++ 
  ["\(runtimes[i,j]), " ++ 
  if(fix(runtimes[i,j]) < 10) then " " else "" endif | j in index_set_2of2(runtimes) ] ++ 
  [ "\n" ] )  | i in index_set_1of2(runtimes)];
















