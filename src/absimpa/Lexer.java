package absimpa;


import java.util.Set;



public interface Lexer<TokenCode extends Enum<TokenCode>>{
  TokenCode next();
  TokenCode current();
  ParseException parseException(Set<TokenCode> expectedTokens);
}
