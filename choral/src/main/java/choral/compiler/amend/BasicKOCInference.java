package choral.compiler.amend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.EnumConstant;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ClassModifier;
import choral.ast.body.ConstructorDefinition;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.Member.HigherMethod;
import choral.types.World;
import choral.utils.Pair;

/**
 * Knowledge of choice inference.
 * Iterates through a program and at every if-statement and switch-statement
 * sends a selection to every role but itself.
 * <p>
 * retruns a Selections object with all the selections to insert. the Data 
 * inference module needs to insert them.
 */
public class BasicKOCInference {

    Enum enumerator = null;
    Map< Statement, List<List<Expression>> > selections = new HashMap<>();
    
    public BasicKOCInference(){}
    
    public Selections inferKOC( CompilationUnit cu ){
        
        for( Class cls : cu.classes() ){
            List< World > classWorlds = cls.worldParameters().stream().map( world -> (World)world.typeAnnotation().get() ).toList();

			for( ConstructorDefinition constructor : cls.constructors() ){
				new VisitStatement( classWorlds, constructor.signature().typeAnnotation().get().channels() ).visit(constructor.body());
			}
			
			for( ClassMethodDefinition method : cls.methods() ){
				if( method.body().isPresent() ){
					new VisitStatement( classWorlds, method.signature().typeAnnotation().get().channels() ).visit(method.body().get());
				}
			}
		}

        return new Selections(selections, enumerator);
    }


	private class VisitStatement extends AbstractChoralVisitor< Void >{
		
        List< World > allWorlds;
        List< Pair< String, GroundInterface > > methodChannels;

		public VisitStatement( List< World > allWorlds, List< Pair< String, GroundInterface > > channels ){
            this.allWorlds = allWorlds;
            this.methodChannels = channels;
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
			List< ? extends World > senders = ((GroundDataType)n.condition().typeAnnotation().get()).worldArguments();
            if( senders.size() != 1 ){
                System.out.println( "Found " + senders.size() + " roles, expected 1" );
                return null; // TODO throw some error
            }
            World sender = (World)senders.get(0);
            System.out.println( "Sender: " + sender );
            System.out.println( "AllWorlds: " + allWorlds );

            List<World> recipients = allWorlds.stream().filter( world -> !world.equals(sender) ).toList();
            if( recipients.size() > 0 ){
                Pair<List <Expression>, List<Expression>> selectionsPair = inferIfSelection( sender, recipients );
                List<Expression> ifSelections = selectionsPair.left();
                List<Expression> elseSelections = selectionsPair.right();
                selections.put( n, List.of( ifSelections, elseSelections ) );
            } 
            
            visitContinutation(n.ifBranch());
            visitContinutation(n.elseBranch());
            return visitContinutation( n.continuation() );
		}

		@Override // not supported
		public Void visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Void visit( TryCatchStatement n ) {
			visit( n.body() ); 
            n.catches(); // TODO this should be visited as well
            return visitContinutation( n.continuation() );
		}

		@Override
		public Void visit( ReturnStatement n ) {
			return visitContinutation( n.continuation() );
		}

		/** 
		 * Visits the continuation if there is one 
		 */
		private Void visitContinutation( Statement continutation ){
			return continutation == null ? null : visit(continutation);
		}

        /**
         * Creates selections for an if-statement
         */
        private Pair<List<Expression>, List<Expression>> inferIfSelection( World sender, List<World> recipients ){
            
            List<Expression> ifSelections = new ArrayList<>();
            List<Expression> elseSelections = new ArrayList<>();
            for( World recipient : recipients ){
                SelectionMethod selectionMethod = findSelectionMethod( sender, recipient );
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

        private SelectionMethod findSelectionMethod( World sender, World recipient ){
            for( Pair<String, GroundInterface> channelPair : methodChannels ){
            
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
            System.out.println( "No viable selection method was found for roles " + sender + " and " + recipient );
            return null; // TODO throw exception
        }

        /**
         * Returns an enumerator with the specified amount of cases. If no such 
         * enumerator exists, one is created.
         */
        private Enum getEnum( int numCases ){
            // Checks that an enumerator with enough cases has previously been created
            if( enumerator == null || enumerator.cases().size() < numCases ){
                // If not, creates one
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
                    null);
            }
            return enumerator;
        }
	}

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

        public ScopedExpression createSelectionExpression( Enum enumerator, EnumConstant enumCons ){
			
            TypeExpression typeExpression = new TypeExpression( 
                enumerator.name(), 
                Collections.emptyList(), // This needs to be "higher kinded" and can thus not have a worldargument
                Collections.emptyList());

            TypeExpression argScope = new TypeExpression(
                enumerator.name(), 
                List.of( new WorldArgument( new Name(sender.identifier() )) ), 
                Collections.emptyList());

            ScopedExpression argument = new ScopedExpression( // looks something like Enum@Sender.CHOICE
                new StaticAccessExpression( // Enum@Sender
                    argScope), 
                new FieldAccessExpression( // CHOICE
                    enumCons.name()));
            
			final List<Expression> arguments = List.of( argument );
			final Name name = new Name( selectionMethod.identifier() );
			final List<TypeExpression> typeArguments = List.of( typeExpression );
			
			MethodCallExpression scopedExpression = new MethodCallExpression(name, arguments, typeArguments); // TODO add position
			
			// The comMethod is a method inside its channel, so we need to make the channel its scope
			FieldAccessExpression scope = new FieldAccessExpression(new Name(channelIdentifier)); // TODO add position
			
			// Something like channel.< Type >com( Expression )
			return new ScopedExpression(scope, scopedExpression);
        }

    }

}
