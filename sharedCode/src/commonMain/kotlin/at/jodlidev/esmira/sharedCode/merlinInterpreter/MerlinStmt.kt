package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * This file contains all classes necessary to represent Statements in Merlin (i.e., all parts of the scripts causing side effects without generating a value).
 * It defines an interface for a Visitor, allowing implementing classes (i.e., the Interpreter), to handle the different statement types.
 */

abstract class MerlinStmt {
    interface Visitor<R> {
        fun visitAssignStmt(stmt: Assign): R
        fun visitArraySetStmt(stmt: ArraySet): R
        fun visitBlockStmt(stmt: Block): R
        fun visitExpressionStmt(stmt: Expression): R
        fun visitForStmt(stmt: For): R
        fun visitFunctionStmt(stmt: Function): R
        fun visitIfStmt(stmt: If): R
        fun visitInitStmt(stmt: Init): R
        fun visitObjectSetStmt(expr: ObjectSet): R
        fun visitReturnStmt(stmt: Return): R
        fun visitWhileStmt(stmt: While): R
    }

    abstract fun<R> accept(visitor: Visitor<R>): R

    class ArraySet (val bracket: MerlinToken, val arr: MerlinExpr, val index: MerlinExpr, val value: MerlinExpr): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitArraySetStmt(this)
        }
    }
    class Assign (val name: MerlinToken, val value: MerlinExpr): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignStmt(this)
        }
    }
    class Block (val statements: List<MerlinStmt>): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }
    class Expression (val expression: MerlinExpr): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExpressionStmt(this)
        }
    }
    class For (val varName: MerlinToken, val iterable: MerlinExpr, val body: MerlinStmt): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitForStmt(this)
        }
    }
    class Function (val name: MerlinToken, val params: List<MerlinToken>, val body: List<MerlinStmt>): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFunctionStmt(this)
        }
    }
    class If (val branches: List<Pair<MerlinExpr, MerlinStmt>>, val elseBranch: MerlinStmt?): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }
    }
    class Init (val statements: List<MerlinStmt>): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitInitStmt(this)
        }
    }
    class ObjectSet (val obj: MerlinExpr, val name: MerlinToken, val value: MerlinExpr): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitObjectSetStmt(this)
        }
    }
    class Return (val keyword: MerlinToken, val value: MerlinExpr?): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitReturnStmt(this)
        }
    }
    class While (val condition: MerlinExpr, val body: MerlinStmt): MerlinStmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitWhileStmt(this)
        }
    }
}