package choral.compiler.amend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.*;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.merge.StatementsMerger;
import choral.compiler.soloist.StatementsProjector;
import choral.exceptions.ChoralCompoundException;
import choral.exceptions.CommunicationInferenceException;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.Member.HigherMethod;
import choral.types.World;
import choral.utils.Pair;

/**
 * Knowledge of choice inference.
 * Iterates through a program and at every IfStatement and SwitchStatement sends a selection to every 
 * role but itself, that takes part in at least one branch in that Statement.
 * Returns a Selections object with all the selections to insert.
 */
public class BasicKOCInference {

    Enum enumerator = null;
    Map< Statement, List<List<Expression>> > selections = new HashMap<>();
    Position position;
    
    public BasicKOCInference(){}
    
    public Selections inferKOC( CompilationUnit cu ){
        position = cu.position();

		// Build the selection map by visiting all constructors and methods in all classes
        for( Class cls : cu.classes() ){
			for( ConstructorDefinition constructor : cls.constructors() ){
				new VisitStatement( constructor ).visit(constructor.body());
			}
			for( ClassMethodDefinition method : cls.methods() ){
				if( method.body().isPresent() ){
					new VisitStatement( method ).visit(method.body().get());
				}
			}
		}

        return new Selections(selections, enumerator);
    }

    /**
     * Iterates through Statements and ignores everything but IfStatements and SwitchStatements. 
     * Examines which worlds need selections for each of the IfStatements and SwitchStatements, and 
     * fills the selections map with these selections.
     * <p>
     * Also creates an enumerator of size equal to number of cases in the largest SwitchStatement, or 
     * size two if only IfStatements are found, unless an enumerator of sufficient size has already 
     * been created.
     * <p>
     * If no selections are needed, the selections map remains unchanged and no enumerator will be 
     * created.
     */
	private class VisitStatement extends AbstractChoralVisitor< Void >{
		
        List< Pair< String, GroundInterface > > channels;

		public VisitStatement( ConstructorDefinition ctor ){
			channels = ctor.signature().typeAnnotation().get().channels();
        }

		public VisitStatement( MethodDefinition method ){
			channels = method.signature().typeAnnotation().get().channels();
		}

		@Override
		public Void visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Void visit( ExpressionStatement n ) {
            return visitContinutation( n.continuation() );
		}

		@Override
		public Void visit( VariableDeclarationStatement n ) {
			return visitContinutation( n.continuation() );
		}

		@Override
		public Void visit( NilStatement n ) {
			return null;
		}

		@Override
		public Void visit( BlockStatement n ) {
			visit( n.enclosedStatement() ); 
            return visitContinutation( n.continuation() );
		}

		@Override
		public Void visit( IfStatement n ) {
			var senders = ((GroundDataType)n.condition().typeAnnotation().get()).worldArguments();
            if( senders.size() != 1 ){
                throw new CommunicationInferenceException("Found " + senders.size() + " roles, expected 1");
            }
            World sender = senders.get(0);

            List<World> recipients = getParticipants(sender, List.of(n.ifBranch(), n.elseBranch()));
            if( !recipients.isEmpty() ){
                var selectionsPair = inferIfSelection( sender, recipients );
                var ifSelections = selectionsPair.left();
                var elseSelections = selectionsPair.right();
                selections.put( n, List.of( ifSelections, elseSelections ) );
            } 
            
            visitContinutation(n.ifBranch());
            visitContinutation(n.elseBranch());
            return visitContinutation( n.continuation() );
		}

		@Override
		public Void visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\t" +
					"Statement at " + n.position().toString());
		}

		@Override
		public Void visit( TryCatchStatement n ) {
			visit( n.body() );
			for ( Pair< VariableDeclaration, Statement > catchBlock : n.catches() ) {
				visit( catchBlock.right() );
			}
            return visitContinutation( n.continuation() );
		}

		@Override
		public Void visit( ReturnStatement n ) {
			return visitContinutation( n.continuation() );
		}

		/** Visits the continuation if there is one */
		private Void visitContinutation( Statement continutation ){
			return continutation == null ? null : visit(continutation);
		}

		/**
		 * Retrieves a list of all the worlds that partake in at least one Statement, excluding the
		 * sender.
		 */
		private List<World> getParticipants( World sender, List<Statement> statements ){
			Set<World> participants = new HashSet<>();
			for( Statement statement : statements ){
				participants.addAll( new GetStatementParticipants().getParticipants(statement) );
			}
			participants.remove(sender);

			return getNeedsKOC(participants.stream().sorted().toList(), statements);
		}

		/**
		 * takes a list of worlds and statements and returns the worlds that need knowledge
		 * of choice based on the projection of the statements on the participants
		 */
		private List<World> getNeedsKOC( List<World> participants, List<Statement> statements ){
			List<World> needsKOC = new ArrayList<>();

			for( World participant : participants ){
				// System.out.println( "Looking at world: " + participant );

				// If either the projector or the merger throws an error, then the current participant
				// needs KOC. Otherwise, the participant does not need KOC
				try {
					// create projections of the statements on the current participant
					List< Statement > projectedStatements = new ArrayList<>(statements).stream().map(
							statement ->
									StatementsProjector.visit( new WorldArgument( new Name(participant.identifier() )), statement ) ).toList();

					// merge the projected statements
					StatementsMerger.merge(projectedStatements);

					// System.out.println( "World " + participant + " does not need KOC" );
					// for( Statement statement : projectedStatements ){
					// 	System.out.println( new PrettyPrinterVisitor().visit(statement) );
					// }

				} catch ( Exception e ){ // since not all implementations of merge() throws MergeException, we match on all exceptions
					// System.out.println( "World " + participant + " needs KOC" );
					needsKOC.add(participant);
				}
			}

			return needsKOC;
		}

        /** Creates selections for an if-statement */
        private Pair<List<Expression>, List<Expression>> inferIfSelection( World sender, List<World> recipients ){
            
            List<SelectionMethod> selectionList = findSelectionMethods( sender, recipients );

            List<Expression> ifSelections = new ArrayList<>();
            List<Expression> elseSelections = new ArrayList<>();
            for( SelectionMethod selectionMethod : selectionList ){
                Enum ifEnum = getEnum( 2 );
                // create selections for if branch
                ScopedExpression ifSelectionExpression = selectionMethod.createSelectionExpression( ifEnum, ifEnum.cases().get(0) );
                ifSelections.add( ifSelectionExpression );
                // create selections for else branch
                ScopedExpression elseSelectionExpression = selectionMethod.createSelectionExpression( ifEnum, ifEnum.cases().get(1) );
                elseSelections.add( elseSelectionExpression );
            }


            return new Pair<>(ifSelections, elseSelections);
        }

        /**
         * Finds selection methods for a sender and a list of recipients. If the sender cannot directly
         * reach each recipient, the reachable recipients are considered as senders. 
         * <p>
         * Throws an error if at least one recipient cannot be reached. 
         */
        private List<SelectionMethod> findSelectionMethods( World initialSender, List<World> recipientsList ){
            List<SelectionMethod> selectionList = new ArrayList<>();
            List<World> senders = new ArrayList<>();
            List<World> recipients = new ArrayList<>(recipientsList); // because I want a modifiable copy
            senders.add(initialSender);

            while( !recipients.isEmpty() && !senders.isEmpty() ){
                World sender = senders.remove(0); // the current sender to consider
                for( World recipient : recipients ){
                    // Tries to reach all recipients
                    SelectionMethod selectionMethod = findSelectionMethod(sender, recipient);
                    if( selectionMethod != null ){
                        // If a recipient is reachable, it becomes a new potential sender
                        senders.add( recipient );
                        selectionList.add( selectionMethod );
                    }
                }
                // Remove all the recipients that have already been reached
                recipients.removeAll( senders );
            }

            if( !recipients.isEmpty() ){
                List<CommunicationInferenceException> exceptions = new ArrayList<>();
                for( World recipient : recipients ){
                    exceptions.add( new CommunicationInferenceException("No viable selection method was found for " + recipient + " with sender " + initialSender) );
                }
                throw new ChoralCompoundException(exceptions);
            }

            return selectionList;
        }

        /**
         * Iterates through the chanels to find a suitable selection method between the sender and 
         * recipient. Returns null if no viable selection method was found.
         */
        private SelectionMethod findSelectionMethod( World sender, World recipient ){
            for( Pair<String, GroundInterface> channelPair : channels ){
            
                Optional<? extends HigherMethod> selectMethodOptional = 
                    channelPair.right().methods()
                        .filter( method ->
                            method.identifier().equals("select") && // it is a selection method (only checked through name)
                            method.innerCallable().signature().parameters().get(0).type().worldArguments().equals(List.of(sender)) && // its parameter's worlds are equal to our sender
                            method.innerCallable().returnType() instanceof GroundDataType && // probably redundant check, returntype should not be able to be void
                            ((GroundDataType)method.innerCallable().returnType()).worldArguments().get(0).equals(recipient) ) // its returntype's world is equal to our recipient
                        .findAny();
            
                if( selectMethodOptional.isPresent() ){
                    return new SelectionMethod( channelPair.left(), channelPair.right(), selectMethodOptional.get(), sender );
                }
            }
            // no viable selectionmethod was found
            return null;
        }

        /**
         * Returns an enumerator with the specified amount of cases. If no such enumerator exists, one 
         * is created.
         */
        private Enum getEnum( int numCases ){
            // Checks if an enumerator with enough cases has previously been created
            if( enumerator == null || enumerator.cases().size() < numCases ){
                // If not, creates one, and overwrites the current enumerator
                List<EnumConstant> cases = new ArrayList<>();
                for( int i = 0; i < numCases; i++ ){
                    cases.add( new EnumConstant(new Name( "CASE" + i ), Collections.emptyList(), null) );
                }
                
                enumerator = new Enum(
                    new Name( "KOCEnum" ), 
                    new FormalWorldParameter( new Name( "R" ) ), 
                    cases, 
                    Collections.emptyList(), 
                    EnumSet.noneOf( ClassModifier.class ), 
                    position);
            }
            return enumerator;
        }


	}

    /**
     * A class to represent a selection method
     */
    private class SelectionMethod{
        private String channelIdentifier;
        private GroundInterface channel;
        private HigherMethod selectionMethod;
        private World sender;

        public SelectionMethod( 
            String channelIdentifier,
            GroundInterface channel,
            HigherMethod selectionMethod,
            World sender 
        ){
            this.channelIdentifier = channelIdentifier;
            this.channel = channel;
            this.selectionMethod = selectionMethod;
            this.sender = sender;
        }

        public String channelIdentifier(){
            return channelIdentifier;
        }

        public GroundInterface channel(){
            return channel;
        }

        public HigherMethod selectionMethod(){
            return selectionMethod;
        }

        public World sender(){
            return sender;
        }

        /**
         * Creates a selections expression from this SelectionMethod object on the enumerator given as 
         * input.
         */
        public ScopedExpression createSelectionExpression( Enum enumerator, EnumConstant enumCons ){
			
            TypeExpression typeExpression = new TypeExpression( 
                enumerator.name(), 
                Collections.emptyList(), // This needs to be "higher kinded" and can thus not have a worldargument
                Collections.emptyList(),
                position); // TODO proper position

            TypeExpression argScope = new TypeExpression(
                enumerator.name(), 
                List.of( new WorldArgument( new Name(sender.identifier() )) ), // this needs a worldargument
                Collections.emptyList(),
                position); // TODO proper position

            ScopedExpression argument = new ScopedExpression( // looks something like Enum@Sender.CHOICE
                new StaticAccessExpression( // Enum@Sender
                    argScope,
                    position), // TODO proper position
                new FieldAccessExpression( // CHOICE
                    enumCons.name(),
                    position), // TODO proper position
                position); // TODO proper position
            
			final List<Expression> arguments = List.of( argument );
			final Name name = new Name( selectionMethod.identifier() );
			final List<TypeExpression> typeArguments = List.of( typeExpression );
			
			MethodCallExpression scopedExpression = new MethodCallExpression(name, arguments, typeArguments, position); // TODO proper position
			
			// The selection method is a method inside its channel, so we need to make the channel its scope
			FieldAccessExpression scope = new FieldAccessExpression(new Name(channelIdentifier), position); // TODO proper position
			
			// Something like channel.< enumerator >select( enumerator@sender.enumCons )
			return new ScopedExpression(scope, scopedExpression, position);
        }

    }

    /**
     * Iterates through Statements and uses GetExpressionParticipants to get participants of any 
     * Expressions of the Statements. 
     * <p>
     * Use it be calling getParticipants() on some statement.
     */
    private class GetStatementParticipants extends AbstractChoralVisitor< Void >{
		
        Set< World > participants = new HashSet<>();

		public GetStatementParticipants(){}

        /** The main method of this class */
        public Set< World > getParticipants( Statement statement ){
            visit( statement );

            return participants;
        }

		@Override
		public Void visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Void visit( ExpressionStatement n ) {
            Set<World> expressionParticipants = new GetExpressionParticipants().GetParticipants(n.expression());
            participants.addAll( expressionParticipants );
            
            return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( VariableDeclarationStatement n ) {
			for( VariableDeclaration vd : n.variables() ){
                visitVariableDeclaration(vd);
            }
            return visitContinutation(n.continuation()); 
		}

		@Override
		public Void visit( NilStatement n ) {
			return visitContinutation(n.continuation()); 
		}

		@Override
		public Void visit( BlockStatement n ) {
			visit(n.enclosedStatement());
            
            return visitContinutation(n.continuation()); 
		}

		@Override
		public Void visit( IfStatement n ) {
            Set<World> conditionParticipants = new GetExpressionParticipants().GetParticipants(n.condition());
            participants.addAll(conditionParticipants);
            visit(n.ifBranch());
            visit(n.elseBranch());

			return visitContinutation(n.continuation()); 
		}

		@Override // Not supported
		public Void visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Void visit( TryCatchStatement n ) {
			throw new UnsupportedOperationException("TryCatchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Void visit( ReturnStatement n ) {
            Set<World> returnParticipants = new GetExpressionParticipants().GetParticipants(n.returnExpression());
            participants.addAll(returnParticipants);

			return visitContinutation(n.continuation());
		}

		/** 
		 * Visits the continuation if there is one 
		 */
		private Void visitContinutation( Statement continutation ){
			return continutation == null ? null : visit(continutation);
		}

        /**
		 * If there is an initializer, visits its expression and collects participants.
		 */
		private Void visitVariableDeclaration( VariableDeclaration vd ){
            // TODO look at vd's type's worldarguments
			
			if( !vd.initializer().isEmpty() ){
                Set<World> initializerParticipants = new GetExpressionParticipants().GetParticipants(vd.initializer().get());
                participants.addAll(initializerParticipants);
            }
            
            return null;
		}

	}

    /**
     * Iterates through Expressions to collect participants. 
     * <p>
     * Use it be calling getParticipants() on some Expression.
     */
    private class GetExpressionParticipants extends AbstractChoralVisitor< Void >{
        
        private Set<World> participants = new HashSet<>();

        public GetExpressionParticipants(){}

        /** The main method of this class */
        public Set<World> GetParticipants( Expression expression ){
            
            visit(expression);
            return participants;
        }

		@Override
		public Void visit( Expression n ) {
			return n.accept( this );
		}

		@Override
		public Void visit( ScopedExpression n ) {
            // probably don't need to look at the ScopedExpression's worlds, since we will 
            // look at its scope and scopedExpression
			visit( n.scope() );
            return visit( n.scopedExpression() );
		}

		@Override
		public Void visit( FieldAccessExpression n ) {
			GroundDataType nType = (GroundDataType)n.typeAnnotation().get(); // assuming that a field cannot be void
            participants.addAll(nType.worldArguments());
            return null;
		}

		@Override
		public Void visit( MethodCallExpression n ) {		
            if( !n.methodAnnotation().get().returnType().isVoid() ){
                GroundDataType returnType = (GroundDataType)n.methodAnnotation().get().returnType();
                participants.addAll(returnType.worldArguments());
            }

            for( Expression argument : n.arguments() ){
                visit(argument);
            }

            return null;
		}
		
		@Override
		public Void visit( AssignExpression n ) {
			visit(n.target());
            return visit(n.value());
		}

		@Override
		public Void visit( BinaryExpression n ) {
			visit(n.left());
            return visit(n.right());
		}

		@Override
		public Void visit( EnclosedExpression n ) {
			return visit(n.nestedExpression());
		}
		
		@Override
		public Void visit( StaticAccessExpression n ) {
			if( !n.typeAnnotation().get().isVoid() ){ // I think this might be able to be void
                GroundDataType staticAccessType = (GroundDataType)n.typeAnnotation().get();
                participants.addAll(staticAccessType.worldArguments());
            }
            return null;
		}

		@Override
		public Void visit( ClassInstantiationExpression n ) {
			// not sure how to get the class's worlds TODO fix this

            for( Expression argument : n.arguments() ){
                visit(argument);
            }

            return null;
		}

		@Override
		public Void visit( NotExpression n ) {
			return visit(n.expression());
		}

		@Override // not supported
		public Void visit( ThisExpression n ) {
			throw new UnsupportedOperationException("ThisExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Void visit( SuperExpression n ) {
			throw new UnsupportedOperationException("SuperExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override
		public Void visit( NullExpression n ) {
            return null;
		}

		public Void visit( LiteralExpression.BooleanLiteralExpression n ) {
            participants.add(n.world().typeAnnotation().get()); // TODO check worldargumetns for everything above this
			return null;
		}

		public Void visit( LiteralExpression.IntegerLiteralExpression n ) {
			participants.add(n.world().typeAnnotation().get());
			return null;
		}

		public Void visit( LiteralExpression.DoubleLiteralExpression n ) {
			participants.add(n.world().typeAnnotation().get());
			return null;
		}

		public Void visit( LiteralExpression.StringLiteralExpression n ) {
			participants.add(n.world().typeAnnotation().get());
			return null;
		}

		@Override // not supported
		public Void visit( TypeExpression n ) {
			throw new UnsupportedOperationException("TypeExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Void visit( BlankExpression n ){
			throw new UnsupportedOperationException("BlankExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Void visit( EnumCaseInstantiationExpression n ){
			throw new UnsupportedOperationException("EnumCaseInstantiationExpression not supported\n\tExpression at " + n.position().toString());
		}
    }
}
