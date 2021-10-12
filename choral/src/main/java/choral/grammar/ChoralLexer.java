// Generated from Choral.g4 by ANTLR 4.5.3
package choral.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ChoralLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, CLASS=7, ELSE=8, SWITCH=9, 
		CASE=10, DEFAULT=11, EXTENDS=12, IF=13, IMPLEMENTS=14, PACKAGE=15, IMPORT=16, 
		INTERFACE=17, ENUM=18, NEW=19, RETURN=20, SUPER=21, THIS=22, TRY=23, CATCH=24, 
		NULL=25, VOID=26, AMPERSAND=27, IntegerLiteral=28, FloatingPointLiteral=29, 
		BooleanLiteral=30, CharacterLiteral=31, StringLiteral=32, LPAREN=33, RPAREN=34, 
		LBRACE=35, RBRACE=36, LBRACK=37, RBRACK=38, LANGLE=39, RANGLE=40, SEMI=41, 
		COMMA=42, DOT=43, STAR=44, ASSIGN=45, GT_EQUAL=46, LT_EQUAL=47, NOT=48, 
		COLON=49, EQUAL=50, NOTEQUAL=51, AND=52, OR=53, INC=54, DEC=55, ADD=56, 
		SUB=57, DIV=58, BITOR=59, MOD=60, ADD_ASSIGN=61, SUB_ASSIGN=62, MUL_ASSIGN=63, 
		DIV_ASSIGN=64, AND_ASSIGN=65, OR_ASSIGN=66, MOD_ASSIGN=67, CHAIN_ACCESS=68, 
		RIGHT_ARROW=69, Identifier=70, AT=71, ELLIPSIS=72, WS=73, COMMENT=74, 
		LINE_COMMENT=75;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "CLASS", "ELSE", "SWITCH", 
		"CASE", "DEFAULT", "EXTENDS", "IF", "IMPLEMENTS", "PACKAGE", "IMPORT", 
		"INTERFACE", "ENUM", "NEW", "RETURN", "SUPER", "THIS", "TRY", "CATCH", 
		"NULL", "VOID", "AMPERSAND", "IntegerLiteral", "DecimalIntegerLiteral", 
		"DecimalNumeral", "Digits", "Digit", "NonZeroDigit", "DigitsAndUnderscores", 
		"DigitOrUnderscore", "Underscores", "HexDigit", "OctalDigit", "FloatingPointLiteral", 
		"DecimalFloatingPointLiteral", "ExponentPart", "ExponentIndicator", "SignedInteger", 
		"Sign", "FloatTypeSuffix", "BooleanLiteral", "CharacterLiteral", "SingleCharacter", 
		"StringLiteral", "StringCharacters", "StringCharacter", "EscapeSequence", 
		"OctalEscape", "ZeroToThree", "UnicodeEscape", "LPAREN", "RPAREN", "LBRACE", 
		"RBRACE", "LBRACK", "RBRACK", "LANGLE", "RANGLE", "SEMI", "COMMA", "DOT", 
		"STAR", "ASSIGN", "GT_EQUAL", "LT_EQUAL", "NOT", "COLON", "EQUAL", "NOTEQUAL", 
		"AND", "OR", "INC", "DEC", "ADD", "SUB", "DIV", "BITOR", "MOD", "ADD_ASSIGN", 
		"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", "MOD_ASSIGN", 
		"CHAIN_ACCESS", "RIGHT_ARROW", "Identifier", "JavaLetter", "JavaLetterOrDigit", 
		"AT", "ELLIPSIS", "WS", "COMMENT", "LINE_COMMENT"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'public'", "'protected'", "'private'", "'abstract'", "'static'", 
		"'final'", "'class'", "'else'", "'switch'", "'case'", "'default'", "'extends'", 
		"'if'", "'implements'", "'package'", "'import'", "'interface'", "'enum'", 
		"'new'", "'return'", "'super'", "'this'", "'try'", "'catch'", "'null'", 
		"'void'", "'&'", null, null, null, null, null, "'('", "')'", "'{'", "'}'", 
		"'['", "']'", "'<'", "'>'", "';'", "','", "'.'", "'*'", "'='", "'>='", 
		"'<='", "'!'", "':'", "'=='", "'!='", "'&&'", "'||'", "'++'", "'--'", 
		"'+'", "'-'", "'/'", "'|'", "'%'", "'+='", "'-='", "'*='", "'/='", "'&='", 
		"'|='", "'%='", "'::'", "'->'", null, "'@'", "'...'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "CLASS", "ELSE", "SWITCH", "CASE", 
		"DEFAULT", "EXTENDS", "IF", "IMPLEMENTS", "PACKAGE", "IMPORT", "INTERFACE", 
		"ENUM", "NEW", "RETURN", "SUPER", "THIS", "TRY", "CATCH", "NULL", "VOID", 
		"AMPERSAND", "IntegerLiteral", "FloatingPointLiteral", "BooleanLiteral", 
		"CharacterLiteral", "StringLiteral", "LPAREN", "RPAREN", "LBRACE", "RBRACE", 
		"LBRACK", "RBRACK", "LANGLE", "RANGLE", "SEMI", "COMMA", "DOT", "STAR", 
		"ASSIGN", "GT_EQUAL", "LT_EQUAL", "NOT", "COLON", "EQUAL", "NOTEQUAL", 
		"AND", "OR", "INC", "DEC", "ADD", "SUB", "DIV", "BITOR", "MOD", "ADD_ASSIGN", 
		"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", "MOD_ASSIGN", 
		"CHAIN_ACCESS", "RIGHT_ARROW", "Identifier", "AT", "ELLIPSIS", "WS", "COMMENT", 
		"LINE_COMMENT"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public ChoralLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Choral.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 93:
			return JavaLetter_sempred((RuleContext)_localctx, predIndex);
		case 94:
			return JavaLetterOrDigit_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean JavaLetter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return Character.isJavaIdentifierStart(_input.LA(-1));
		case 1:
			return Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)));
		}
		return true;
	}
	private boolean JavaLetterOrDigit_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return Character.isJavaIdentifierPart(_input.LA(-1));
		case 3:
			return Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)));
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2M\u02b6\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3"+
		"\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3\27\3\27"+
		"\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\32"+
		"\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\35\3\35\3\36"+
		"\5\36\u017d\n\36\3\36\3\36\3\37\3\37\3\37\5\37\u0184\n\37\3\37\3\37\3"+
		"\37\5\37\u0189\n\37\5\37\u018b\n\37\3 \3 \5 \u018f\n \3 \5 \u0192\n \3"+
		"!\3!\5!\u0196\n!\3\"\3\"\3#\6#\u019b\n#\r#\16#\u019c\3$\3$\5$\u01a1\n"+
		"$\3%\6%\u01a4\n%\r%\16%\u01a5\3&\3&\3\'\3\'\3(\3(\3)\3)\3)\5)\u01b1\n"+
		")\3)\5)\u01b4\n)\3)\5)\u01b7\n)\3)\3)\3)\5)\u01bc\n)\3)\5)\u01bf\n)\3"+
		")\3)\3)\5)\u01c4\n)\3)\3)\3)\5)\u01c9\n)\3*\3*\3*\3+\3+\3,\5,\u01d1\n"+
		",\3,\3,\3-\3-\3.\3.\3/\3/\3/\3/\3/\3/\3/\3/\3/\5/\u01e2\n/\3\60\3\60\3"+
		"\60\3\60\3\60\3\60\3\60\3\60\5\60\u01ec\n\60\3\61\3\61\3\62\3\62\5\62"+
		"\u01f2\n\62\3\62\3\62\3\63\6\63\u01f7\n\63\r\63\16\63\u01f8\3\64\3\64"+
		"\5\64\u01fd\n\64\3\65\3\65\3\65\3\65\5\65\u0203\n\65\3\66\3\66\3\66\3"+
		"\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\5\66\u0210\n\66\3\67\3\67\38\3"+
		"8\68\u0216\n8\r8\168\u0217\38\38\38\38\38\39\39\3:\3:\3;\3;\3<\3<\3=\3"+
		"=\3>\3>\3?\3?\3@\3@\3A\3A\3B\3B\3C\3C\3D\3D\3E\3E\3F\3F\3F\3G\3G\3G\3"+
		"H\3H\3I\3I\3J\3J\3J\3K\3K\3K\3L\3L\3L\3M\3M\3M\3N\3N\3N\3O\3O\3O\3P\3"+
		"P\3Q\3Q\3R\3R\3S\3S\3T\3T\3U\3U\3U\3V\3V\3V\3W\3W\3W\3X\3X\3X\3Y\3Y\3"+
		"Y\3Z\3Z\3Z\3[\3[\3[\3\\\3\\\3\\\3]\3]\3]\3^\3^\7^\u027c\n^\f^\16^\u027f"+
		"\13^\3_\3_\3_\3_\3_\3_\5_\u0287\n_\3`\3`\3`\3`\3`\3`\5`\u028f\n`\3a\3"+
		"a\3b\3b\3b\3b\3c\6c\u0298\nc\rc\16c\u0299\3c\3c\3d\3d\3d\3d\7d\u02a2\n"+
		"d\fd\16d\u02a5\13d\3d\3d\3d\3d\3d\3e\3e\3e\3e\7e\u02b0\ne\fe\16e\u02b3"+
		"\13e\3e\3e\3\u02a3\2f\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27"+
		"\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33"+
		"\65\34\67\359\36;\2=\2?\2A\2C\2E\2G\2I\2K\2M\2O\37Q\2S\2U\2W\2Y\2[\2]"+
		" _!a\2c\"e\2g\2i\2k\2m\2o\2q#s$u%w&y\'{(})\177*\u0081+\u0083,\u0085-\u0087"+
		".\u0089/\u008b\60\u008d\61\u008f\62\u0091\63\u0093\64\u0095\65\u0097\66"+
		"\u0099\67\u009b8\u009d9\u009f:\u00a1;\u00a3<\u00a5=\u00a7>\u00a9?\u00ab"+
		"@\u00adA\u00afB\u00b1C\u00b3D\u00b5E\u00b7F\u00b9G\u00bbH\u00bd\2\u00bf"+
		"\2\u00c1I\u00c3J\u00c5K\u00c7L\u00c9M\3\2\23\3\2\63;\5\2\62;CHch\3\2\62"+
		"9\4\2GGgg\4\2--//\6\2FFHHffhh\6\2\f\f\17\17))^^\6\2\f\f\17\17$$^^\n\2"+
		"$$))^^ddhhppttvv\3\2\62\65\6\2&&C\\aac|\4\2\2\u0081\ud802\udc01\3\2\ud802"+
		"\udc01\3\2\udc02\ue001\7\2&&\62;C\\aac|\5\2\13\f\16\17\"\"\4\2\f\f\17"+
		"\17\u02c2\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2O\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2c\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2"+
		"\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177\3\2\2\2"+
		"\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085\3\2\2\2\2\u0087\3\2\2\2\2\u0089"+
		"\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091\3\2\2"+
		"\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2\2\2\u0099\3\2\2\2\2\u009b"+
		"\3\2\2\2\2\u009d\3\2\2\2\2\u009f\3\2\2\2\2\u00a1\3\2\2\2\2\u00a3\3\2\2"+
		"\2\2\u00a5\3\2\2\2\2\u00a7\3\2\2\2\2\u00a9\3\2\2\2\2\u00ab\3\2\2\2\2\u00ad"+
		"\3\2\2\2\2\u00af\3\2\2\2\2\u00b1\3\2\2\2\2\u00b3\3\2\2\2\2\u00b5\3\2\2"+
		"\2\2\u00b7\3\2\2\2\2\u00b9\3\2\2\2\2\u00bb\3\2\2\2\2\u00c1\3\2\2\2\2\u00c3"+
		"\3\2\2\2\2\u00c5\3\2\2\2\2\u00c7\3\2\2\2\2\u00c9\3\2\2\2\3\u00cb\3\2\2"+
		"\2\5\u00d2\3\2\2\2\7\u00dc\3\2\2\2\t\u00e4\3\2\2\2\13\u00ed\3\2\2\2\r"+
		"\u00f4\3\2\2\2\17\u00fa\3\2\2\2\21\u0100\3\2\2\2\23\u0105\3\2\2\2\25\u010c"+
		"\3\2\2\2\27\u0111\3\2\2\2\31\u0119\3\2\2\2\33\u0121\3\2\2\2\35\u0124\3"+
		"\2\2\2\37\u012f\3\2\2\2!\u0137\3\2\2\2#\u013e\3\2\2\2%\u0148\3\2\2\2\'"+
		"\u014d\3\2\2\2)\u0151\3\2\2\2+\u0158\3\2\2\2-\u015e\3\2\2\2/\u0163\3\2"+
		"\2\2\61\u0167\3\2\2\2\63\u016d\3\2\2\2\65\u0172\3\2\2\2\67\u0177\3\2\2"+
		"\29\u0179\3\2\2\2;\u017c\3\2\2\2=\u018a\3\2\2\2?\u018c\3\2\2\2A\u0195"+
		"\3\2\2\2C\u0197\3\2\2\2E\u019a\3\2\2\2G\u01a0\3\2\2\2I\u01a3\3\2\2\2K"+
		"\u01a7\3\2\2\2M\u01a9\3\2\2\2O\u01ab\3\2\2\2Q\u01c8\3\2\2\2S\u01ca\3\2"+
		"\2\2U\u01cd\3\2\2\2W\u01d0\3\2\2\2Y\u01d4\3\2\2\2[\u01d6\3\2\2\2]\u01e1"+
		"\3\2\2\2_\u01eb\3\2\2\2a\u01ed\3\2\2\2c\u01ef\3\2\2\2e\u01f6\3\2\2\2g"+
		"\u01fc\3\2\2\2i\u0202\3\2\2\2k\u020f\3\2\2\2m\u0211\3\2\2\2o\u0213\3\2"+
		"\2\2q\u021e\3\2\2\2s\u0220\3\2\2\2u\u0222\3\2\2\2w\u0224\3\2\2\2y\u0226"+
		"\3\2\2\2{\u0228\3\2\2\2}\u022a\3\2\2\2\177\u022c\3\2\2\2\u0081\u022e\3"+
		"\2\2\2\u0083\u0230\3\2\2\2\u0085\u0232\3\2\2\2\u0087\u0234\3\2\2\2\u0089"+
		"\u0236\3\2\2\2\u008b\u0238\3\2\2\2\u008d\u023b\3\2\2\2\u008f\u023e\3\2"+
		"\2\2\u0091\u0240\3\2\2\2\u0093\u0242\3\2\2\2\u0095\u0245\3\2\2\2\u0097"+
		"\u0248\3\2\2\2\u0099\u024b\3\2\2\2\u009b\u024e\3\2\2\2\u009d\u0251\3\2"+
		"\2\2\u009f\u0254\3\2\2\2\u00a1\u0256\3\2\2\2\u00a3\u0258\3\2\2\2\u00a5"+
		"\u025a\3\2\2\2\u00a7\u025c\3\2\2\2\u00a9\u025e\3\2\2\2\u00ab\u0261\3\2"+
		"\2\2\u00ad\u0264\3\2\2\2\u00af\u0267\3\2\2\2\u00b1\u026a\3\2\2\2\u00b3"+
		"\u026d\3\2\2\2\u00b5\u0270\3\2\2\2\u00b7\u0273\3\2\2\2\u00b9\u0276\3\2"+
		"\2\2\u00bb\u0279\3\2\2\2\u00bd\u0286\3\2\2\2\u00bf\u028e\3\2\2\2\u00c1"+
		"\u0290\3\2\2\2\u00c3\u0292\3\2\2\2\u00c5\u0297\3\2\2\2\u00c7\u029d\3\2"+
		"\2\2\u00c9\u02ab\3\2\2\2\u00cb\u00cc\7r\2\2\u00cc\u00cd\7w\2\2\u00cd\u00ce"+
		"\7d\2\2\u00ce\u00cf\7n\2\2\u00cf\u00d0\7k\2\2\u00d0\u00d1\7e\2\2\u00d1"+
		"\4\3\2\2\2\u00d2\u00d3\7r\2\2\u00d3\u00d4\7t\2\2\u00d4\u00d5\7q\2\2\u00d5"+
		"\u00d6\7v\2\2\u00d6\u00d7\7g\2\2\u00d7\u00d8\7e\2\2\u00d8\u00d9\7v\2\2"+
		"\u00d9\u00da\7g\2\2\u00da\u00db\7f\2\2\u00db\6\3\2\2\2\u00dc\u00dd\7r"+
		"\2\2\u00dd\u00de\7t\2\2\u00de\u00df\7k\2\2\u00df\u00e0\7x\2\2\u00e0\u00e1"+
		"\7c\2\2\u00e1\u00e2\7v\2\2\u00e2\u00e3\7g\2\2\u00e3\b\3\2\2\2\u00e4\u00e5"+
		"\7c\2\2\u00e5\u00e6\7d\2\2\u00e6\u00e7\7u\2\2\u00e7\u00e8\7v\2\2\u00e8"+
		"\u00e9\7t\2\2\u00e9\u00ea\7c\2\2\u00ea\u00eb\7e\2\2\u00eb\u00ec\7v\2\2"+
		"\u00ec\n\3\2\2\2\u00ed\u00ee\7u\2\2\u00ee\u00ef\7v\2\2\u00ef\u00f0\7c"+
		"\2\2\u00f0\u00f1\7v\2\2\u00f1\u00f2\7k\2\2\u00f2\u00f3\7e\2\2\u00f3\f"+
		"\3\2\2\2\u00f4\u00f5\7h\2\2\u00f5\u00f6\7k\2\2\u00f6\u00f7\7p\2\2\u00f7"+
		"\u00f8\7c\2\2\u00f8\u00f9\7n\2\2\u00f9\16\3\2\2\2\u00fa\u00fb\7e\2\2\u00fb"+
		"\u00fc\7n\2\2\u00fc\u00fd\7c\2\2\u00fd\u00fe\7u\2\2\u00fe\u00ff\7u\2\2"+
		"\u00ff\20\3\2\2\2\u0100\u0101\7g\2\2\u0101\u0102\7n\2\2\u0102\u0103\7"+
		"u\2\2\u0103\u0104\7g\2\2\u0104\22\3\2\2\2\u0105\u0106\7u\2\2\u0106\u0107"+
		"\7y\2\2\u0107\u0108\7k\2\2\u0108\u0109\7v\2\2\u0109\u010a\7e\2\2\u010a"+
		"\u010b\7j\2\2\u010b\24\3\2\2\2\u010c\u010d\7e\2\2\u010d\u010e\7c\2\2\u010e"+
		"\u010f\7u\2\2\u010f\u0110\7g\2\2\u0110\26\3\2\2\2\u0111\u0112\7f\2\2\u0112"+
		"\u0113\7g\2\2\u0113\u0114\7h\2\2\u0114\u0115\7c\2\2\u0115\u0116\7w\2\2"+
		"\u0116\u0117\7n\2\2\u0117\u0118\7v\2\2\u0118\30\3\2\2\2\u0119\u011a\7"+
		"g\2\2\u011a\u011b\7z\2\2\u011b\u011c\7v\2\2\u011c\u011d\7g\2\2\u011d\u011e"+
		"\7p\2\2\u011e\u011f\7f\2\2\u011f\u0120\7u\2\2\u0120\32\3\2\2\2\u0121\u0122"+
		"\7k\2\2\u0122\u0123\7h\2\2\u0123\34\3\2\2\2\u0124\u0125\7k\2\2\u0125\u0126"+
		"\7o\2\2\u0126\u0127\7r\2\2\u0127\u0128\7n\2\2\u0128\u0129\7g\2\2\u0129"+
		"\u012a\7o\2\2\u012a\u012b\7g\2\2\u012b\u012c\7p\2\2\u012c\u012d\7v\2\2"+
		"\u012d\u012e\7u\2\2\u012e\36\3\2\2\2\u012f\u0130\7r\2\2\u0130\u0131\7"+
		"c\2\2\u0131\u0132\7e\2\2\u0132\u0133\7m\2\2\u0133\u0134\7c\2\2\u0134\u0135"+
		"\7i\2\2\u0135\u0136\7g\2\2\u0136 \3\2\2\2\u0137\u0138\7k\2\2\u0138\u0139"+
		"\7o\2\2\u0139\u013a\7r\2\2\u013a\u013b\7q\2\2\u013b\u013c\7t\2\2\u013c"+
		"\u013d\7v\2\2\u013d\"\3\2\2\2\u013e\u013f\7k\2\2\u013f\u0140\7p\2\2\u0140"+
		"\u0141\7v\2\2\u0141\u0142\7g\2\2\u0142\u0143\7t\2\2\u0143\u0144\7h\2\2"+
		"\u0144\u0145\7c\2\2\u0145\u0146\7e\2\2\u0146\u0147\7g\2\2\u0147$\3\2\2"+
		"\2\u0148\u0149\7g\2\2\u0149\u014a\7p\2\2\u014a\u014b\7w\2\2\u014b\u014c"+
		"\7o\2\2\u014c&\3\2\2\2\u014d\u014e\7p\2\2\u014e\u014f\7g\2\2\u014f\u0150"+
		"\7y\2\2\u0150(\3\2\2\2\u0151\u0152\7t\2\2\u0152\u0153\7g\2\2\u0153\u0154"+
		"\7v\2\2\u0154\u0155\7w\2\2\u0155\u0156\7t\2\2\u0156\u0157\7p\2\2\u0157"+
		"*\3\2\2\2\u0158\u0159\7u\2\2\u0159\u015a\7w\2\2\u015a\u015b\7r\2\2\u015b"+
		"\u015c\7g\2\2\u015c\u015d\7t\2\2\u015d,\3\2\2\2\u015e\u015f\7v\2\2\u015f"+
		"\u0160\7j\2\2\u0160\u0161\7k\2\2\u0161\u0162\7u\2\2\u0162.\3\2\2\2\u0163"+
		"\u0164\7v\2\2\u0164\u0165\7t\2\2\u0165\u0166\7{\2\2\u0166\60\3\2\2\2\u0167"+
		"\u0168\7e\2\2\u0168\u0169\7c\2\2\u0169\u016a\7v\2\2\u016a\u016b\7e\2\2"+
		"\u016b\u016c\7j\2\2\u016c\62\3\2\2\2\u016d\u016e\7p\2\2\u016e\u016f\7"+
		"w\2\2\u016f\u0170\7n\2\2\u0170\u0171\7n\2\2\u0171\64\3\2\2\2\u0172\u0173"+
		"\7x\2\2\u0173\u0174\7q\2\2\u0174\u0175\7k\2\2\u0175\u0176\7f\2\2\u0176"+
		"\66\3\2\2\2\u0177\u0178\7(\2\2\u01788\3\2\2\2\u0179\u017a\5;\36\2\u017a"+
		":\3\2\2\2\u017b\u017d\5Y-\2\u017c\u017b\3\2\2\2\u017c\u017d\3\2\2\2\u017d"+
		"\u017e\3\2\2\2\u017e\u017f\5=\37\2\u017f<\3\2\2\2\u0180\u018b\7\62\2\2"+
		"\u0181\u0188\5C\"\2\u0182\u0184\5? \2\u0183\u0182\3\2\2\2\u0183\u0184"+
		"\3\2\2\2\u0184\u0189\3\2\2\2\u0185\u0186\5I%\2\u0186\u0187\5? \2\u0187"+
		"\u0189\3\2\2\2\u0188\u0183\3\2\2\2\u0188\u0185\3\2\2\2\u0189\u018b\3\2"+
		"\2\2\u018a\u0180\3\2\2\2\u018a\u0181\3\2\2\2\u018b>\3\2\2\2\u018c\u0191"+
		"\5A!\2\u018d\u018f\5E#\2\u018e\u018d\3\2\2\2\u018e\u018f\3\2\2\2\u018f"+
		"\u0190\3\2\2\2\u0190\u0192\5A!\2\u0191\u018e\3\2\2\2\u0191\u0192\3\2\2"+
		"\2\u0192@\3\2\2\2\u0193\u0196\7\62\2\2\u0194\u0196\5C\"\2\u0195\u0193"+
		"\3\2\2\2\u0195\u0194\3\2\2\2\u0196B\3\2\2\2\u0197\u0198\t\2\2\2\u0198"+
		"D\3\2\2\2\u0199\u019b\5G$\2\u019a\u0199\3\2\2\2\u019b\u019c\3\2\2\2\u019c"+
		"\u019a\3\2\2\2\u019c\u019d\3\2\2\2\u019dF\3\2\2\2\u019e\u01a1\5A!\2\u019f"+
		"\u01a1\7a\2\2\u01a0\u019e\3\2\2\2\u01a0\u019f\3\2\2\2\u01a1H\3\2\2\2\u01a2"+
		"\u01a4\7a\2\2\u01a3\u01a2\3\2\2\2\u01a4\u01a5\3\2\2\2\u01a5\u01a3\3\2"+
		"\2\2\u01a5\u01a6\3\2\2\2\u01a6J\3\2\2\2\u01a7\u01a8\t\3\2\2\u01a8L\3\2"+
		"\2\2\u01a9\u01aa\t\4\2\2\u01aaN\3\2\2\2\u01ab\u01ac\5Q)\2\u01acP\3\2\2"+
		"\2\u01ad\u01ae\5? \2\u01ae\u01b0\7\60\2\2\u01af\u01b1\5? \2\u01b0\u01af"+
		"\3\2\2\2\u01b0\u01b1\3\2\2\2\u01b1\u01b3\3\2\2\2\u01b2\u01b4\5S*\2\u01b3"+
		"\u01b2\3\2\2\2\u01b3\u01b4\3\2\2\2\u01b4\u01b6\3\2\2\2\u01b5\u01b7\5["+
		".\2\u01b6\u01b5\3\2\2\2\u01b6\u01b7\3\2\2\2\u01b7\u01c9\3\2\2\2\u01b8"+
		"\u01b9\7\60\2\2\u01b9\u01bb\5? \2\u01ba\u01bc\5S*\2\u01bb\u01ba\3\2\2"+
		"\2\u01bb\u01bc\3\2\2\2\u01bc\u01be\3\2\2\2\u01bd\u01bf\5[.\2\u01be\u01bd"+
		"\3\2\2\2\u01be\u01bf\3\2\2\2\u01bf\u01c9\3\2\2\2\u01c0\u01c1\5? \2\u01c1"+
		"\u01c3\5S*\2\u01c2\u01c4\5[.\2\u01c3\u01c2\3\2\2\2\u01c3\u01c4\3\2\2\2"+
		"\u01c4\u01c9\3\2\2\2\u01c5\u01c6\5? \2\u01c6\u01c7\5[.\2\u01c7\u01c9\3"+
		"\2\2\2\u01c8\u01ad\3\2\2\2\u01c8\u01b8\3\2\2\2\u01c8\u01c0\3\2\2\2\u01c8"+
		"\u01c5\3\2\2\2\u01c9R\3\2\2\2\u01ca\u01cb\5U+\2\u01cb\u01cc\5W,\2\u01cc"+
		"T\3\2\2\2\u01cd\u01ce\t\5\2\2\u01ceV\3\2\2\2\u01cf\u01d1\5Y-\2\u01d0\u01cf"+
		"\3\2\2\2\u01d0\u01d1\3\2\2\2\u01d1\u01d2\3\2\2\2\u01d2\u01d3\5? \2\u01d3"+
		"X\3\2\2\2\u01d4\u01d5\t\6\2\2\u01d5Z\3\2\2\2\u01d6\u01d7\t\7\2\2\u01d7"+
		"\\\3\2\2\2\u01d8\u01d9\7v\2\2\u01d9\u01da\7t\2\2\u01da\u01db\7w\2\2\u01db"+
		"\u01e2\7g\2\2\u01dc\u01dd\7h\2\2\u01dd\u01de\7c\2\2\u01de\u01df\7n\2\2"+
		"\u01df\u01e0\7u\2\2\u01e0\u01e2\7g\2\2\u01e1\u01d8\3\2\2\2\u01e1\u01dc"+
		"\3\2\2\2\u01e2^\3\2\2\2\u01e3\u01e4\7)\2\2\u01e4\u01e5\5a\61\2\u01e5\u01e6"+
		"\7)\2\2\u01e6\u01ec\3\2\2\2\u01e7\u01e8\7)\2\2\u01e8\u01e9\5i\65\2\u01e9"+
		"\u01ea\7)\2\2\u01ea\u01ec\3\2\2\2\u01eb\u01e3\3\2\2\2\u01eb\u01e7\3\2"+
		"\2\2\u01ec`\3\2\2\2\u01ed\u01ee\n\b\2\2\u01eeb\3\2\2\2\u01ef\u01f1\7$"+
		"\2\2\u01f0\u01f2\5e\63\2\u01f1\u01f0\3\2\2\2\u01f1\u01f2\3\2\2\2\u01f2"+
		"\u01f3\3\2\2\2\u01f3\u01f4\7$\2\2\u01f4d\3\2\2\2\u01f5\u01f7\5g\64\2\u01f6"+
		"\u01f5\3\2\2\2\u01f7\u01f8\3\2\2\2\u01f8\u01f6\3\2\2\2\u01f8\u01f9\3\2"+
		"\2\2\u01f9f\3\2\2\2\u01fa\u01fd\n\t\2\2\u01fb\u01fd\5i\65\2\u01fc\u01fa"+
		"\3\2\2\2\u01fc\u01fb\3\2\2\2\u01fdh\3\2\2\2\u01fe\u01ff\7^\2\2\u01ff\u0203"+
		"\t\n\2\2\u0200\u0203\5k\66\2\u0201\u0203\5o8\2\u0202\u01fe\3\2\2\2\u0202"+
		"\u0200\3\2\2\2\u0202\u0201\3\2\2\2\u0203j\3\2\2\2\u0204\u0205\7^\2\2\u0205"+
		"\u0210\5M\'\2\u0206\u0207\7^\2\2\u0207\u0208\5M\'\2\u0208\u0209\5M\'\2"+
		"\u0209\u0210\3\2\2\2\u020a\u020b\7^\2\2\u020b\u020c\5m\67\2\u020c\u020d"+
		"\5M\'\2\u020d\u020e\5M\'\2\u020e\u0210\3\2\2\2\u020f\u0204\3\2\2\2\u020f"+
		"\u0206\3\2\2\2\u020f\u020a\3\2\2\2\u0210l\3\2\2\2\u0211\u0212\t\13\2\2"+
		"\u0212n\3\2\2\2\u0213\u0215\7^\2\2\u0214\u0216\7w\2\2\u0215\u0214\3\2"+
		"\2\2\u0216\u0217\3\2\2\2\u0217\u0215\3\2\2\2\u0217\u0218\3\2\2\2\u0218"+
		"\u0219\3\2\2\2\u0219\u021a\5K&\2\u021a\u021b\5K&\2\u021b\u021c\5K&\2\u021c"+
		"\u021d\5K&\2\u021dp\3\2\2\2\u021e\u021f\7*\2\2\u021fr\3\2\2\2\u0220\u0221"+
		"\7+\2\2\u0221t\3\2\2\2\u0222\u0223\7}\2\2\u0223v\3\2\2\2\u0224\u0225\7"+
		"\177\2\2\u0225x\3\2\2\2\u0226\u0227\7]\2\2\u0227z\3\2\2\2\u0228\u0229"+
		"\7_\2\2\u0229|\3\2\2\2\u022a\u022b\7>\2\2\u022b~\3\2\2\2\u022c\u022d\7"+
		"@\2\2\u022d\u0080\3\2\2\2\u022e\u022f\7=\2\2\u022f\u0082\3\2\2\2\u0230"+
		"\u0231\7.\2\2\u0231\u0084\3\2\2\2\u0232\u0233\7\60\2\2\u0233\u0086\3\2"+
		"\2\2\u0234\u0235\7,\2\2\u0235\u0088\3\2\2\2\u0236\u0237\7?\2\2\u0237\u008a"+
		"\3\2\2\2\u0238\u0239\7@\2\2\u0239\u023a\7?\2\2\u023a\u008c\3\2\2\2\u023b"+
		"\u023c\7>\2\2\u023c\u023d\7?\2\2\u023d\u008e\3\2\2\2\u023e\u023f\7#\2"+
		"\2\u023f\u0090\3\2\2\2\u0240\u0241\7<\2\2\u0241\u0092\3\2\2\2\u0242\u0243"+
		"\7?\2\2\u0243\u0244\7?\2\2\u0244\u0094\3\2\2\2\u0245\u0246\7#\2\2\u0246"+
		"\u0247\7?\2\2\u0247\u0096\3\2\2\2\u0248\u0249\7(\2\2\u0249\u024a\7(\2"+
		"\2\u024a\u0098\3\2\2\2\u024b\u024c\7~\2\2\u024c\u024d\7~\2\2\u024d\u009a"+
		"\3\2\2\2\u024e\u024f\7-\2\2\u024f\u0250\7-\2\2\u0250\u009c\3\2\2\2\u0251"+
		"\u0252\7/\2\2\u0252\u0253\7/\2\2\u0253\u009e\3\2\2\2\u0254\u0255\7-\2"+
		"\2\u0255\u00a0\3\2\2\2\u0256\u0257\7/\2\2\u0257\u00a2\3\2\2\2\u0258\u0259"+
		"\7\61\2\2\u0259\u00a4\3\2\2\2\u025a\u025b\7~\2\2\u025b\u00a6\3\2\2\2\u025c"+
		"\u025d\7\'\2\2\u025d\u00a8\3\2\2\2\u025e\u025f\7-\2\2\u025f\u0260\7?\2"+
		"\2\u0260\u00aa\3\2\2\2\u0261\u0262\7/\2\2\u0262\u0263\7?\2\2\u0263\u00ac"+
		"\3\2\2\2\u0264\u0265\7,\2\2\u0265\u0266\7?\2\2\u0266\u00ae\3\2\2\2\u0267"+
		"\u0268\7\61\2\2\u0268\u0269\7?\2\2\u0269\u00b0\3\2\2\2\u026a\u026b\7("+
		"\2\2\u026b\u026c\7?\2\2\u026c\u00b2\3\2\2\2\u026d\u026e\7~\2\2\u026e\u026f"+
		"\7?\2\2\u026f\u00b4\3\2\2\2\u0270\u0271\7\'\2\2\u0271\u0272\7?\2\2\u0272"+
		"\u00b6\3\2\2\2\u0273\u0274\7<\2\2\u0274\u0275\7<\2\2\u0275\u00b8\3\2\2"+
		"\2\u0276\u0277\7/\2\2\u0277\u0278\7@\2\2\u0278\u00ba\3\2\2\2\u0279\u027d"+
		"\5\u00bd_\2\u027a\u027c\5\u00bf`\2\u027b\u027a\3\2\2\2\u027c\u027f\3\2"+
		"\2\2\u027d\u027b\3\2\2\2\u027d\u027e\3\2\2\2\u027e\u00bc\3\2\2\2\u027f"+
		"\u027d\3\2\2\2\u0280\u0287\t\f\2\2\u0281\u0282\n\r\2\2\u0282\u0287\6_"+
		"\2\2\u0283\u0284\t\16\2\2\u0284\u0285\t\17\2\2\u0285\u0287\6_\3\2\u0286"+
		"\u0280\3\2\2\2\u0286\u0281\3\2\2\2\u0286\u0283\3\2\2\2\u0287\u00be\3\2"+
		"\2\2\u0288\u028f\t\20\2\2\u0289\u028a\n\r\2\2\u028a\u028f\6`\4\2\u028b"+
		"\u028c\t\16\2\2\u028c\u028d\t\17\2\2\u028d\u028f\6`\5\2\u028e\u0288\3"+
		"\2\2\2\u028e\u0289\3\2\2\2\u028e\u028b\3\2\2\2\u028f\u00c0\3\2\2\2\u0290"+
		"\u0291\7B\2\2\u0291\u00c2\3\2\2\2\u0292\u0293\7\60\2\2\u0293\u0294\7\60"+
		"\2\2\u0294\u0295\7\60\2\2\u0295\u00c4\3\2\2\2\u0296\u0298\t\21\2\2\u0297"+
		"\u0296\3\2\2\2\u0298\u0299\3\2\2\2\u0299\u0297\3\2\2\2\u0299\u029a\3\2"+
		"\2\2\u029a\u029b\3\2\2\2\u029b\u029c\bc\2\2\u029c\u00c6\3\2\2\2\u029d"+
		"\u029e\7\61\2\2\u029e\u029f\7,\2\2\u029f\u02a3\3\2\2\2\u02a0\u02a2\13"+
		"\2\2\2\u02a1\u02a0\3\2\2\2\u02a2\u02a5\3\2\2\2\u02a3\u02a4\3\2\2\2\u02a3"+
		"\u02a1\3\2\2\2\u02a4\u02a6\3\2\2\2\u02a5\u02a3\3\2\2\2\u02a6\u02a7\7,"+
		"\2\2\u02a7\u02a8\7\61\2\2\u02a8\u02a9\3\2\2\2\u02a9\u02aa\bd\2\2\u02aa"+
		"\u00c8\3\2\2\2\u02ab\u02ac\7\61\2\2\u02ac\u02ad\7\61\2\2\u02ad\u02b1\3"+
		"\2\2\2\u02ae\u02b0\n\22\2\2\u02af\u02ae\3\2\2\2\u02b0\u02b3\3\2\2\2\u02b1"+
		"\u02af\3\2\2\2\u02b1\u02b2\3\2\2\2\u02b2\u02b4\3\2\2\2\u02b3\u02b1\3\2"+
		"\2\2\u02b4\u02b5\be\2\2\u02b5\u00ca\3\2\2\2#\2\u017c\u0183\u0188\u018a"+
		"\u018e\u0191\u0195\u019c\u01a0\u01a5\u01b0\u01b3\u01b6\u01bb\u01be\u01c3"+
		"\u01c8\u01d0\u01e1\u01eb\u01f1\u01f8\u01fc\u0202\u020f\u0217\u027d\u0286"+
		"\u028e\u0299\u02a3\u02b1\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}