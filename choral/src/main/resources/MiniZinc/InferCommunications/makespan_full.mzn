include "globals.mzn";

% --=== Parameters ===--
% Values:
int: in_size; % the number of input statements
int: num_deps; % the nubmer of dependencies
int: out_size = num_deps + in_size;
int: num_blocks;
int: num_ifs;

% Ranges:
set of int: IN_SIZE = 1..in_size;
set of int: DEPS = in_size+1..out_size;
set of int: OUT_SIZE = 1..out_size;
set of int: NUM_BLOCKS = 1..num_blocks;
set of int: NUM_IFS = 1..num_ifs;

% Enums:
enum roles;
enum roles_andNull = r(roles) ++ {NULL};

% Lists:
array[IN_SIZE] of string: statements; % a list of all the original statements
array[IN_SIZE] of roles_andNull: statements_roles; % a list of the roles of the originals statements
array[IN_SIZE] of int: statements_runtimes = [1 | i in IN_SIZE]; % a list of runtimes for each statement 
                                                                  % (default if 1 for every statement)
array[NUM_BLOCKS, 1..3] of int: blocks; 
array[IN_SIZE] of int: statements_blocks;
array[NUM_IFS, 1..2] of NUM_BLOCKS: if_blocks;
array[NUM_IFS] of roles: if_roles;
array[DEPS] of string: dependencies;  % a list of all the dependency expressions
array[DEPS] of roles: dep_to; % a list of all the dependency recipient roles
array[DEPS] of roles: dep_from; % a list of all the dependency sender roles
array[DEPS] of int: dep_def_at; % a list of the statements in which the dependency expressions were originally defined
array[int, 1..2] of int: dep_used_at; % a list of all the uses of dependencies

% --=== Variables ===--
array[DEPS] of var IN_SIZE: out_prog_idx; % a list specifying the statement at which a communication is to be inserted
array[NUM_IFS, 1..2, roles] of var opt IN_SIZE: selections; % a list specifying the statement at which a selection is to be inserted

% --== Functions ==--
function var bool: in_statement_block( DEPS: dep, OUT_SIZE: used_at ) = 
  if(used_at in IN_SIZE)
    then 
      in_block(dep, statements_blocks[used_at])
    else % used in dependency (nested dependency)
      in_block(dep, statements_blocks[out_prog_idx[used_at]])
  endif ;

function var bool: in_block( DEPS: dep, var NUM_BLOCKS: block ) =
  (
    statements_blocks[out_prog_idx[dep]] ==  block % dependency is placed inside target block
  ) \/ ( % the target block is completely within the block of the dependency
    blocks[block, 1] > blocks[ statements_blocks[out_prog_idx[dep]], 1 ] /\
    blocks[block, 1] <= blocks[ statements_blocks[out_prog_idx[dep]], 2 ] /\
    blocks[block, 2] > blocks[ statements_blocks[out_prog_idx[dep]], 1 ] /\
    blocks[block, 2] <= blocks[ statements_blocks[out_prog_idx[dep]], 2 ] 
  );


% --=== Constraints ===--
constraint forall( x in DEPS ) ( % makes sure communmications inserted after dependency definition
  out_prog_idx[ x ] > dep_def_at[ x ]
);

constraint forall( x in index_set_1of2(dep_used_at) ) ( % makes sure communications are inserted before every use of that dependency
  if(dep_used_at[x,2] in IN_SIZE)
    then out_prog_idx[dep_used_at[x,1]] <= dep_used_at[x,2] % used in statement
    else out_prog_idx[dep_used_at[x,1]] < out_prog_idx[dep_used_at[x,2]] % used in dependency (nested dependency)
  endif 
);

constraint forall( x in index_set_1of2(dep_used_at) )( % makes sure each communication is in the correct block
  in_statement_block(dep_used_at[x, 1], dep_used_at[x, 2])
);

constraint forall( if_ in NUM_IFS ) (
  forall( branch in 1..2 ) (
    selections[if_, branch, if_roles[if_]] = <>
  )
);

constraint forall( if_ in NUM_IFS ) (
  forall( branch in 1..2 )(
    forall( role in roles where role != if_roles[if_]) (
      if( exists(
        [ true | s in IN_SIZE
        where
          (
            statements_blocks[s] == if_blocks[if_, 1] \/
            statements_blocks[s] == if_blocks[if_, 2] 
          ) /\
          statements_roles[s] == r(role)
        ]) 
      )
        then statements_blocks[selections[if_, branch, role]] = if_blocks[if_, branch]
        else selections[if_, branch, role] = <>
      endif
    )
  )
);

constraint forall( if_ in NUM_IFS ) (
  forall( role in roles )(
    forall( branch in 1..2 )(
      forall( stm in IN_SIZE where
        statements_blocks[stm] == if_blocks[if_, branch] /\
        statements_roles[stm] == r(role)
      )(
        selections[if_, branch, role] <= stm
      )
    )
  )
);






















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
[ " ]\n" ] /*++

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
  [ "\n" ] )  | i in index_set_1of2(runtimes)]*/;


















