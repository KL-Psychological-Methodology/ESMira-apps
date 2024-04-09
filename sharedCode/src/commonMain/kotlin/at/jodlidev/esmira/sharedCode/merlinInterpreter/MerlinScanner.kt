package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * The Scanner is responsible for taking a raw source code string input and turning it into a stream of Tokens.
 * In some cases the scanner can throw an error, either when encountering a symbol not used in the language, or when a string is not terminated.
 */

class MerlinScanner (private val source: String) {
    private val tokens = mutableListOf<MerlinToken>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<MerlinToken> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(MerlinToken(MerlinTokenType.EOF, "", MerlinNone, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(MerlinTokenType.LEFT_PAREN)
            ')' -> addToken(MerlinTokenType.RIGHT_PAREN)
            '{' -> addToken(MerlinTokenType.LEFT_BRACE)
            '}' -> addToken(MerlinTokenType.RIGHT_BRACE)
            '[' -> addToken(MerlinTokenType.LEFT_BRACKET)
            ']' -> addToken(MerlinTokenType.RIGHT_BRACKET)
            ',' -> addToken(MerlinTokenType.COMMA)
            ':' -> addToken(MerlinTokenType.COLON)
            ';' -> addToken(MerlinTokenType.SEMICOLON)
            '-' -> addToken(MerlinTokenType.MINUS)
            '+' -> addToken(MerlinTokenType.PLUS)
            '*' -> addToken(MerlinTokenType.STAR)
            '.' -> addToken(if (match('.')) MerlinTokenType.DOT_DOT else MerlinTokenType.DOT)
            '!' -> addToken(if (match('=')) MerlinTokenType.EXCLAMATION_EQUAL else MerlinTokenType.EXCLAMATION)
            '=' -> addToken(if (match('=')) MerlinTokenType.EQUAL_EQUAL else MerlinTokenType.EQUAL)
            '<' -> addToken(if (match('=')) MerlinTokenType.LESS_EQUAL else MerlinTokenType.LESS)
            '>' -> addToken(
                if (match('='))
                    MerlinTokenType.GREATER_EQUAL
                else if (match('>'))
                    MerlinTokenType.GREATER_GREATER
                else
                    MerlinTokenType.GREATER
            )
            '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) advance()
            } else {
                addToken(MerlinTokenType.SLASH)
            }
            '\\' -> {
                while (!match('\n') && !isAtEnd()) advance()
                line++
            }
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '"' -> string()
            else -> {
                if (c.isDigit()) {
                    number()
                } else if (c.isLetter() || c == '_') {
                    identifier()
                } else {
                    throw MerlinScanningError(c, line, "Unexpected character.")
                }
            }
        }
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: MerlinTokenType) {
        addToken(type, MerlinNone)
    }

    private fun addToken(type: MerlinTokenType, literal: MerlinType) {
        val text = source.substring(start, current)
        tokens.add(MerlinToken(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return 0.toChar()
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return 0.toChar()
        return source[current + 1]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            throw MerlinScanningError(peek(), line, "Unterminated String")
        }

        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(MerlinTokenType.STRING, MerlinString(value))
    }

    private fun number() {
        while (peek().isDigit()) advance()

        if(peek() == '.' && peekNext().isDigit()) {
            advance()
            while (peek().isDigit()) advance()
        }

        val value = source.substring(start, current).toDouble()
        addToken(MerlinTokenType.NUMBER, MerlinNumber(value))
    }

    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') advance()

        val text = source.substring(start, current)
        val type = keywords.getOrElse(text) { MerlinTokenType.IDENTIFIER }
        addToken(type)
    }

    companion object {
        val keywords = mapOf(
            "and" to MerlinTokenType.AND,
            "elif" to MerlinTokenType.ELIF,
            "else" to MerlinTokenType.ELSE,
            "false" to MerlinTokenType.FALSE,
            "function" to MerlinTokenType.FUNCTION,
            "for" to MerlinTokenType.FOR,
            "if" to MerlinTokenType.IF,
            "in" to MerlinTokenType.IN,
            "init" to MerlinTokenType.INIT,
            "none" to MerlinTokenType.NONE,
            "object" to MerlinTokenType.OBJECT,
            "or" to MerlinTokenType.OR,
            "return" to MerlinTokenType.RETURN,
            "true" to MerlinTokenType.TRUE,
            "while" to MerlinTokenType.WHILE,
        )
    }
}