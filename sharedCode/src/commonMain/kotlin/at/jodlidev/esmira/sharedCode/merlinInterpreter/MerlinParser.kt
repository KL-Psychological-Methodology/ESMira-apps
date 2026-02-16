package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * The Parser is responsible for taking a list of Tokens and turning them into an Abstract Syntax Tree, consisting of a mixture of Statements and Expressions.
 * The MerlinParser is implemented as a Recursive Descent Parser, closely modelled after the jlox parser by Robert Nystrom: https://craftinginterpreters.com/
 */

class MerlinParser (private val tokens: List<MerlinToken>) {
    private var current = 0

    fun parse(): List<MerlinStmt> {
        val statements = ArrayList<MerlinStmt>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    private fun declaration(): MerlinStmt {
        if (match(MerlinTokenType.FUNCTION)) return function()
        return statement()
    }

    private fun function(): MerlinStmt.Function {
        val name = consume(MerlinTokenType.IDENTIFIER, "Expect function name.")
        consume(MerlinTokenType.LEFT_PAREN, "Expect '(' after function name.")
        val parameters = ArrayList<MerlinToken>()
        if (!check(MerlinTokenType.RIGHT_PAREN)) {
            do {
                parameters.add(consume(MerlinTokenType.IDENTIFIER, "Expect parameter name."))
            } while (match(MerlinTokenType.COMMA))
        }
        consume(MerlinTokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consume(MerlinTokenType.LEFT_BRACE, "Expect '{' before function body.")
        val body = block()
        return MerlinStmt.Function(name, parameters, body)
    }

    private fun block(): List<MerlinStmt> {
        val statements = ArrayList<MerlinStmt>()

        while (!check(MerlinTokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(MerlinTokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun statement(): MerlinStmt {
        if (match(MerlinTokenType.FOR)) return forStatement()
        if (match(MerlinTokenType.IF)) return ifStatement()
        if (match(MerlinTokenType.RETURN)) return returnStatement()
        if (match(MerlinTokenType.WHILE)) return whileStatement()
        if (match(MerlinTokenType.INIT)) return initStatement()
        if (match(MerlinTokenType.LEFT_BRACE)) return MerlinStmt.Block(block())

        return assignmentStatement()
    }

    private fun forStatement(): MerlinStmt {
        consume(MerlinTokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        val variable = consume(MerlinTokenType.IDENTIFIER, "Expect variable name.")
        consume(MerlinTokenType.IN, "Expect 'in' after variable name in 'for'.")
        val iterable = expression()
        consume(MerlinTokenType.RIGHT_PAREN, "Expect ')' after 'for' range.")
        val body = statement()

        return MerlinStmt.For(variable, iterable, body)
    }

    private fun ifStatement(): MerlinStmt {
        val branches = mutableListOf<Pair<MerlinExpr, MerlinStmt>>()
        do {
            consume(MerlinTokenType.LEFT_PAREN, "Expect '(' after 'if'.")
            val condition = expression()
            consume(MerlinTokenType.RIGHT_PAREN, "Expect ')' after if condition.")
            val thenBranch = statement()
            branches.add(Pair(condition, thenBranch))
        } while (match(MerlinTokenType.ELIF))
        var elseBranch: MerlinStmt? = null
        if(match(MerlinTokenType.ELSE)) {
            elseBranch = statement()
        }

        return MerlinStmt.If(branches, elseBranch)
    }

    private fun returnStatement(): MerlinStmt {
        val keyword = previous()
        var value: MerlinExpr? = null
        if (!check(MerlinTokenType.SEMICOLON)) value = expression()
        consume(MerlinTokenType.SEMICOLON, "Expect ';' after return value.")
        return MerlinStmt.Return(keyword, value)
    }

    private fun whileStatement(): MerlinStmt {
        consume(MerlinTokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(MerlinTokenType.RIGHT_PAREN, "Expect ')' after while condition.")
        val body = statement()

        return MerlinStmt.While(condition, body)
    }

    private fun assignmentStatement(): MerlinStmt {
        val expr = expression()

        if (match(
                MerlinTokenType.EQUAL,
                MerlinTokenType.PLUS_EQUAL,
                MerlinTokenType.MINUS_EQUAL,
                MerlinTokenType.STAR_EQUAL,
                MerlinTokenType.SLASH_EQUAL,
                MerlinTokenType.MODULO_EQUAL
            )) {
            val assignmentOperator = previous()
            var value = expression()

            if (assignmentOperator.type != MerlinTokenType.EQUAL) {
               val arithmeticType = when(assignmentOperator.type) {
                   MerlinTokenType.PLUS_EQUAL -> MerlinTokenType.PLUS
                   MerlinTokenType.MINUS_EQUAL -> MerlinTokenType.MINUS
                   MerlinTokenType.STAR_EQUAL -> MerlinTokenType.STAR
                   MerlinTokenType.SLASH_EQUAL -> MerlinTokenType.SLASH
                   MerlinTokenType.MODULO_EQUAL -> MerlinTokenType.MODULO
                   else -> throw MerlinParseError(assignmentOperator, "Error: Used, compound assignment operator not implemented (this should be unreachable)")
               }
               val arithmeticOperator = MerlinToken(arithmeticType, assignmentOperator.lexeme, assignmentOperator.literal, assignmentOperator.line)
               value = MerlinExpr.Binary(expr, arithmeticOperator, value)
            }

            return when (expr) {
                is MerlinExpr.Variable -> {
                    val name = expr.name
                    consume(MerlinTokenType.SEMICOLON, "Expect ';' after assign statement.")
                    MerlinStmt.Assign(name, value)
                }
                is MerlinExpr.ObjectGet -> {
                    consume(MerlinTokenType.SEMICOLON, "Expect ';' after assign statement.")
                    MerlinStmt.ObjectSet(expr.obj, expr.name, value)
                }
                is MerlinExpr.ArrayGet -> {
                    consume(MerlinTokenType.SEMICOLON, "Expect ';' after assign statement.")
                    MerlinStmt.ArraySet(expr.bracket, expr.arr, expr.index, value)
                }
                else -> throw MerlinParseError(assignmentOperator, "Invalid assignment target.")
            }
        }

        consume(MerlinTokenType.SEMICOLON, "Expect ';' after expression.")
        return MerlinStmt.Expression(expr)
    }
    private fun initStatement(): MerlinStmt {
        val statements = ArrayList<MerlinStmt>()
        consume(MerlinTokenType.LEFT_BRACE, "Expect '{' after 'init'.")

        while (!check(MerlinTokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(MerlinTokenType.RIGHT_BRACE, "Expect '}' after init block.")
        return MerlinStmt.Init(statements)
    }

    private fun expression(): MerlinExpr {
        return or()
    }

    private fun or(): MerlinExpr {
        var expr = and()

        while (match(MerlinTokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = MerlinExpr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): MerlinExpr {
        var expr = equality()

        while (match(MerlinTokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = MerlinExpr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun equality(): MerlinExpr {
        var expr = comparison()

        while (match(MerlinTokenType.EXCLAMATION_EQUAL, MerlinTokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = MerlinExpr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): MerlinExpr {
        var expr = term()

        while (match(MerlinTokenType.GREATER, MerlinTokenType.GREATER_EQUAL, MerlinTokenType.LESS, MerlinTokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = MerlinExpr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): MerlinExpr {
        var expr = factor()

        while (match(MerlinTokenType.MINUS, MerlinTokenType.PLUS, MerlinTokenType.DOT_DOT)) {
            val operator = previous()
            val right = factor()
            expr = MerlinExpr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): MerlinExpr {
        var expr = sequence()

        while (match(MerlinTokenType.SLASH, MerlinTokenType.STAR, MerlinTokenType.MODULO)) {
            val operator = previous()
            val right = sequence()
            expr = MerlinExpr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun sequence(): MerlinExpr {
        val expr = unary()

        if(match(MerlinTokenType.COLON)) {
            val colon = previous()
            val sequenceEnd = unary()
            var sequenceStep: MerlinExpr? = null
            if(match(MerlinTokenType.COLON)) sequenceStep = unary()

            return MerlinExpr.MerlinSequence(expr, colon, sequenceEnd, sequenceStep)
        }

        return expr
    }

    private fun unary(): MerlinExpr {
        if(match(MerlinTokenType.EXCLAMATION, MerlinTokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return MerlinExpr.Unary(operator, right)
        }

        return callChain()
    }

    private fun callChain(): MerlinExpr {
        var expr = call()

        while (match(MerlinTokenType.GREATER_GREATER)) {
            val nextFunc = call()
            if (nextFunc !is MerlinExpr.Call) {
                throw MerlinParseError(previous(), "Expected function call after '>>'.")
            }
            // Create a new call and prepend the previous call to the arguments.
            expr = MerlinExpr.Call(nextFunc.callee, nextFunc.paren, listOf(expr) + nextFunc.arguments)
        }

        return expr
    }

    private fun call(): MerlinExpr {
        var expr = value()

        while (true) {
            if (match(MerlinTokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if (match(MerlinTokenType.DOT)) {
                val name = consume(MerlinTokenType.IDENTIFIER, "Expect property name after '.'.")
                expr = MerlinExpr.ObjectGet(expr, name)
            } else if (match(MerlinTokenType.LEFT_BRACKET)) {
                val bracket = previous()
                val index = expression()
                consume(MerlinTokenType.RIGHT_BRACKET, "Expect ']' after access index.")
                expr = MerlinExpr.ArrayGet(bracket, expr, index)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: MerlinExpr): MerlinExpr {
        if (callee !is MerlinExpr.Variable) {
            throw  MerlinParseError(previous(), "Invalid identifier for function call.")
        }
        val arguments = ArrayList<MerlinExpr>()
        if(!check(MerlinTokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression())
            } while (match(MerlinTokenType.COMMA))
        }
        val paren = consume(MerlinTokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return MerlinExpr.Call(callee.name, paren, arguments)
    }

    private fun value(): MerlinExpr {
        if (match(MerlinTokenType.LEFT_BRACKET)) {
            val elements = ArrayList<MerlinExpr>()
            if (!check(MerlinTokenType.RIGHT_BRACKET)) {
                do {
                    elements.add(expression())
                } while (match(MerlinTokenType.COMMA))
            }
            val bracket = consume(MerlinTokenType.RIGHT_BRACKET, "Expect ']' after array.")

            return MerlinExpr.MerlinArrayExpr(bracket, elements)
        }
        return primary()
    }

    private fun primary(): MerlinExpr {
        if (match(MerlinTokenType.FALSE)) return MerlinExpr.Literal(MerlinNumber(0.0))
        if (match(MerlinTokenType.TRUE)) return MerlinExpr.Literal(MerlinNumber(1.0))
        if (match(MerlinTokenType.NONE)) return MerlinExpr.Literal(MerlinNone)
        if (match(MerlinTokenType.OBJECT)) return MerlinExpr.MerlinObject(previous())

        if (match(MerlinTokenType.NUMBER, MerlinTokenType.STRING)) {
            return MerlinExpr.Literal(previous().literal)
        }

        if (match(MerlinTokenType.IDENTIFIER)) {
            return MerlinExpr.Variable(previous())
        }

        if (match(MerlinTokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(MerlinTokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return MerlinExpr.Grouping(expr)
        }

        throw MerlinParseError(peek(), "Expect expression.")
    }

    private fun advance(): MerlinToken {
        if (!isAtEnd()) current ++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == MerlinTokenType.EOF
    }

    private fun peek(): MerlinToken {
        return tokens[current]
    }

    private fun previous(): MerlinToken {
        return tokens[current - 1]
    }

    private fun match(vararg types: MerlinTokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: MerlinTokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun consume(type: MerlinTokenType, message: String): MerlinToken {
        if (check(type)) return advance()
        throw(MerlinParseError(peek(), message))
    }
}