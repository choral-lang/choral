package choral.compiler.amend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
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
 */
public class BasicSelectionInference {

    Enum ifEnum = null;
    Enum switchEnum = null;

    public BasicSelectionInference(){

    }
    
    public CompilationUnit inferSelections( CompilationUnit cu ){
        
        return createNewCompilationUnit(cu);
    }

    /**
	 * Creates a new {@code CompilationUnit} from the old, with amended method bodies (changed to 
	 * include selections) 
	 */
	private CompilationUnit createNewCompilationUnit( CompilationUnit old ){
		for( Enum enm : old.enums() ){
            System.out.println( "Enum: " + enm );
            System.out.println( "Worlds: " + enm.worldParameters() );
            System.out.println( "cases: " + enm.cases() );
            for( EnumConstant cons : enm.cases() ){
                System.out.println( "\t" + cons );
                System.out.println( "\t" + cons.name() );
                System.out.println( "\t" + cons.annotations() );
            }
            System.out.println( "annotations: " + enm.annotations() );
            System.out.println( "modifiers: " + enm.modifiers() );
        }
        
        List<Class> newClasses = new ArrayList<>();
		for( Class cls : old.classes() ){
            List< World > classWorlds = cls.worldParameters().stream().map( world -> (World)world.typeAnnotation().get() ).toList();

			List<ConstructorDefinition> newConstructors = new ArrayList<>();
			for( ConstructorDefinition constructor : cls.constructors() ){
				Statement newBody = new VisitStatement( classWorlds, constructor.signature().typeAnnotation().get().channels() ).visit(constructor.body());

				newConstructors.add(new ConstructorDefinition(
					constructor.signature(), 
					constructor.explicitConstructorInvocation().orElse( null ),
					newBody, 
					constructor.annotations(), 
					constructor.modifiers(), 
					constructor.position()));
			}
			
			List<ClassMethodDefinition> newMethods = new ArrayList<>();
			for( ClassMethodDefinition method : cls.methods() ){
				Statement newBody = null;
				if( method.body().isPresent() ){
					newBody = new VisitStatement( classWorlds, method.signature().typeAnnotation().get().channels() ).visit(method.body().get());
				}

				newMethods.add(new ClassMethodDefinition(
					method.signature(), 
					newBody, 
					method.annotations(), 
					method.modifiers(), 
					method.position()));
			}

			newClasses.add(new Class(
				cls.name(), 
				cls.worldParameters(), 
				cls.typeParameters(), 
				cls.extendsClass(), 
				cls.implementsInterfaces(), 
				cls.fields(), 
				newMethods, 
				newConstructors, 
				cls.annotations(), 
				cls.modifiers(), 
				cls.position()));
		}
        List<Enum> newEnums = old.enums();
        if( ifEnum != null ) newEnums.add(ifEnum);
        if( switchEnum != null ) newEnums.add(switchEnum);
		return new CompilationUnit(
			old.packageDeclaration(), 
			old.imports(), 
			old.interfaces(), 
			newClasses, 
			newEnums, 
			old.position().sourceFile());
	}


	private class VisitStatement extends AbstractChoralVisitor< Statement >{
		
        List< World > allWorlds;
        List< Pair< String, GroundInterface > > methodChannels;

		public VisitStatement( List< World > allWorlds, List< Pair< String, GroundInterface > > channels ){
            this.allWorlds = allWorlds;
            this.methodChannels = channels;
        }

		@Override
		public Statement visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Statement visit( ExpressionStatement n ) {
            return new ExpressionStatement(
                n.expression(), 
                visitContinutation(n.continuation()), 
                n.position());
		}

		@Override
		public Statement visit( VariableDeclarationStatement n ) {
			return new VariableDeclarationStatement(
                n.variables(), 
                visitContinutation(n.continuation()), 
                n.position());
		}

		@Override
		public Statement visit( NilStatement n ) {
			return new NilStatement(n.position());
		}

		@Override
		public Statement visit( BlockStatement n ) {
			return new BlockStatement(
                visit( n.enclosedStatement() ), 
                visitContinutation(n.continuation()), 
                n.position());
		}

		@Override
		public Statement visit( IfStatement n ) {
			List< ? extends World > senders = ((GroundDataType)n.condition().typeAnnotation().get()).worldArguments();
            if( senders.size() != 1 ){
                System.out.println( "Found " + senders.size() + " roles, expected 1" );
                return null; // TODO throw some error
            }
            World sender = (World)senders.get(0);
            System.out.println( "Sender: " + sender );
            System.out.println( "AllWorlds: " + allWorlds );
            
            Statement newIfBranch;
            Statement newElseBranch;

            List<World> recipients = allWorlds.stream().filter( world -> !world.equals(sender) ).toList();
            if( recipients.size() > 0 ){
                Pair<List <Expression>, List<Expression>> selections = inferIfSelection( sender, recipients );
                List<Expression> ifSelections = selections.left();
                List<Expression> elseSelections = selections.right();
                Statement firstIfSelection = new ExpressionStatement(ifSelections.remove(0), visitContinutation(n.ifBranch()));
                newIfBranch = chainSelections( firstIfSelection, ifSelections );
                
                Statement firstElseSelection = new ExpressionStatement(elseSelections.remove(0), visitContinutation(n.elseBranch()));
                newElseBranch = chainSelections( firstElseSelection, elseSelections );
            } else{
                newIfBranch = visit(n.ifBranch());
                newElseBranch = visit(n.elseBranch());
            }
            
            

            return new IfStatement(
                n.condition(), 
                newIfBranch, 
                newElseBranch, 
                visitContinutation(n.continuation()));
		}

		@Override // not supported
		public Statement visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Statement visit( TryCatchStatement n ) {
			return new TryCatchStatement(
                visit( n.body() ), 
                n.catches(), // TODO this should be visited as well
                visitContinutation(n.continuation()), 
                n.position());
		}

		@Override
		public Statement visit( ReturnStatement n ) {
			return new ReturnStatement(
                n.returnExpression(), 
                visitContinutation(n.continuation()), 
                n.position());
		}

		/** 
		 * Visits the continuation if there is one 
		 */
		private Statement visitContinutation( Statement continutation ){
			return continutation == null ? null : visit(continutation);
		}

        private Pair<List<Expression>, List<Expression>> inferIfSelection( World sender, List<World> recipients ){
            
            List<Expression> ifSelections = new ArrayList<>();
            List<Expression> elseSelections = new ArrayList<>();
            for( World recipient : recipients ){
                SelectionMethod selectionMethod = findSelectionMethod( sender, recipient );
                Enum ifEnum = getIfEnum();
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

        private Enum getIfEnum(){
            if( ifEnum == null ){
                List<EnumConstant> cases = new ArrayList<>();
                cases.add( new EnumConstant(new Name( "IFBRANCH" ), Collections.emptyList(), null) );
                cases.add( new EnumConstant(new Name( "ELSEBRANCH" ), Collections.emptyList(), null) );
                
                ifEnum = new Enum(
                    new Name( "IfEnum" ), 
                    new FormalWorldParameter( new Name( "R" ) ), // TODO How to name?? 
                    cases, 
                    Collections.emptyList(), 
                    EnumSet.noneOf( ClassModifier.class ), 
                    null);
            }
            return ifEnum;
        }

        private Statement chainSelections( Statement statement, List<Expression> remainingSelections ){
            if( remainingSelections.size() == 0 ){
                return statement;
            }
            Expression selection = remainingSelections.remove(0);
            ExpressionStatement selectionStatement = new ExpressionStatement(
                selection, 
                statement);
            return chainSelections(selectionStatement, remainingSelections);
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
