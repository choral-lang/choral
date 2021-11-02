//import com.Choral.Panel;
//import java.util.Random;

enum ABC @W {
	case A()
	case B()
	case C()
}

class ReturnChoice@( Chooser, Logger ){

	Channel@( Chooser, Logger ) c;

	ReturnChoice@( Chooser, Logger ) ReturnChoice( Channel@( Chooser, Logger ) c ){
		this.c = c;
	}

	void @Chooser run(){
		Panel@Chooser.show( m() );
	}

	Integer@Chooser m(){
		if( Random@Chooser.nextBoolean() ){
			if( Random@Chooser.nextBoolean() ){
				notify( new ABC@Chooser.A(), c );
				System@Logger.out.println( "Chose A"@Logger );
				return -1@Chooser;
			} else {
				notify( new ABC@Chooser.B(), c );
				System@Logger.out.println( "Chose B"@Logger );
			}
		} else {
			notify( new ABC@Chooser.C(), c );
			System@Logger.out.println( "Chose C"@Logger );
			return 0@Chooser;
		}
		return 1@Chooser;
	}

}
