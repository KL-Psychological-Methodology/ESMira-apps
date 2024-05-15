package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * This file contains all classes necessary to represent Expressions in Merlin (i.e., all parts of the script generating a value).
 * It defines an interface for a Visitor, allowing implementing classes (i.e., the Interpreter), to handle the different statement types.
 */

abstract class MerlinExpr {
    interface Visitor<R> {
        fun visitMerlinArrayExpr(expr: MerlinArrayExpr): R
        fun visitArrayGetExpr(expr: ArrayGet): R
        fun visitBinaryExpr(expr: Binary): R
        fun visitCallExpr(expr: Call): R
        fun visitGroupingExpr(expr: Grouping): R
        fun visitLiteralExpr(expr: Literal): R
        fun visitLogicalExpr(expr: Logical): R
        fun visitMerlinObjectExpr(expr: MerlinObject): R
        fun visitObjectGetExpr(expr: ObjectGet): R
        fun visitSequenceExpr(expr: MerlinSequence): R
        fun visitUnaryExpr(expr: Unary): R
        fun visitVariableExpr(expr: Variable): R
    }

    abstract fun<R> accept(visitor: Visitor<R>): R

    class MerlinArrayExpr (val bracket: MerlinToken, val elements: List<MerlinExpr>): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitMerlinArrayExpr(this)
        }
    }
    class ArrayGet (val bracket: MerlinToken, val arr: MerlinExpr, val index: MerlinExpr): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitArrayGetExpr(this)
        }
    }
    class Binary (val left: MerlinExpr, val operator: MerlinToken, val right: MerlinExpr): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }
    class Call (val callee: MerlinToken, val paren: MerlinToken, val arguments: List<MerlinExpr>): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitCallExpr(this)
        }
    }
    class ObjectGet (val obj: MerlinExpr, val name: MerlinToken): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitObjectGetExpr(this)
        }
    }
    class Grouping(val expression: MerlinExpr): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }
    class Literal (val value: MerlinType): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }
    class Logical (val left: MerlinExpr, val operator: MerlinToken, val right: MerlinExpr): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicalExpr(this)
        }
    }
    class MerlinObject (val keyword: MerlinToken): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitMerlinObjectExpr(this)
        }
    }
    class MerlinSequence (val start: MerlinExpr, val colon: MerlinToken, val end: MerlinExpr, val step: MerlinExpr?): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitSequenceExpr(this)
        }
    }

    class Unary (val operator: MerlinToken, val right: MerlinExpr): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }
    class Variable (val name: MerlinToken): MerlinExpr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }
}