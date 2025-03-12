package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * This file contains the MerlinTokenType enum, representing the different kind of token types available in Merlin.
 * Tokens themselves are represented by MerlinToken.
 */

class MerlinToken (val type: MerlinTokenType, val lexeme: String, val literal: MerlinType, val line: Int) {
    override fun toString(): String {
        return "$line ${type.name} $lexeme"
    }
}

enum class MerlinTokenType {
    // Single character tokens
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_BRACKET,
    RIGHT_BRACKET,
    COMMA,
    COLON,
    SEMICOLON,
    DOT,
    MINUS,
    PLUS,
    SLASH,
    STAR,
    MODULO,

    // One and two character tokens
    DOT_DOT,
    EXCLAMATION,
    EXCLAMATION_EQUAL,
    EQUAL,
    EQUAL_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER_GREATER,

    // Literals
    IDENTIFIER,
    STRING,
    NUMBER,

    // Keywords
    AND,
    ELIF,
    ELSE,
    FALSE,
    FUNCTION,
    FOR,
    IF,
    IN,
    INIT,
    NONE,
    OBJECT,
    OR,
    RETURN,
    TRUE,
    WHILE,

    EOF
}