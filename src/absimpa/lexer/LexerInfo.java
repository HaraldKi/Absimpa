package absimpa.lexer;

public interface LexerInfo<C extends Enum<C>> {
  String getRegex();
  C eofCode();
}
